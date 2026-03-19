(ns com.github.ebaptistella.interceptors.validation
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.interface.http.response :as response]
            [com.github.ebaptistella.interceptors.components :as interceptors.components]
            [io.pedestal.interceptor :as interceptor]
            [schema.core :as s]))

(s/defn ^:private json-content-type? [request]
  (when-let [ct (or (get-in request [:headers "content-type"])
                    (get-in request [:headers "Content-Type"]))]
    (string/starts-with? (string/lower-case ct) "application/json")))

(def json-body
  (interceptor/interceptor
   {:name ::json-body
    :enter (fn [ctx]
             (let [request (:request ctx)
                   body    (:body request)]
               (if (and body (json-content-type? request))
                 (try
                   (let [body-str (cond (string? body) body
                                        (instance? java.io.InputStream body) (slurp body)
                                        :else (str body))
                         parsed   (when (seq body-str) (json/parse-string body-str true))]
                     (if parsed (assoc-in ctx [:request :json-params] parsed) ctx))
                   (catch Exception _
                     (assoc ctx :response {:status  400
                                           :headers {"Content-Type" "application/json"}
                                           :body    (json/generate-string {:error "Invalid JSON"})})))
                 ctx)))}))

(def json-response
  (interceptor/interceptor
   {:name ::json-response
    :leave (fn [ctx]
             (if-not (:response ctx)
               ctx
               (let [{:keys [body status headers]} (:response ctx)
                     ct        (get (or headers {}) "Content-Type")
                     ser-body  (cond
                                 (and (nil? body) (= status 204)) nil
                                 (string? body) body
                                 (or (map? body) (sequential? body) (set? body)) (json/generate-string body)
                                 (some? body) (json/generate-string {:value (str body)})
                                 :else nil)]
                 (assoc ctx :response
                        (cond-> (:response ctx)
                          (not ct) (assoc-in [:headers "Content-Type"] "application/json")
                          (some? ser-body) (assoc :body ser-body)
                          (nil? ser-body) (dissoc :body))))))}))

(s/defn validate-request-body
  "Creates an interceptor to validate request body against a schema."
  ([schema] (validate-request-body schema :validated-wire))
  ([schema target-key]
   (interceptor/interceptor
    {:name ::validate-request-body
     :enter (fn [ctx]
              (let [request  (:request ctx)
                    req-body (:json-params request)
                    log      (logger/bound (interceptors.components/get-component request :logger))]
                (if (nil? req-body)
                  (do (logger/log-call log :warn "[Validation] Body required for %s %s"
                                       (name (:request-method request)) (:uri request))
                      (assoc ctx :response (response/bad-request "Request body is required")))
                  (try
                    (assoc-in ctx [:request target-key] (s/validate schema req-body))
                    (catch clojure.lang.ExceptionInfo e
                      (assoc ctx :response (response/bad-request (or (.getMessage e) "Invalid request data"))))
                    (catch Exception e
                      (assoc ctx :response (response/bad-request (str "Validation error: " (.getMessage e)))))))))})))

(s/defn ^:private not-found-message? [msg]
  (when msg
    (let [lower (string/lower-case msg)]
      (or (string/includes? lower "not found")
          (string/includes? lower "does not exist")))))

(def error-handler-interceptor
  (interceptor/interceptor
   {:name ::error-handler
    :error (fn [ctx ex]
             (let [request (:request ctx)
                   log     (logger/bound (interceptors.components/get-component request :logger))
                   msg     (.getMessage ex)
                   res     (cond
                             (instance? clojure.lang.ExceptionInfo ex)
                             (if (not-found-message? msg) (response/not-found msg) (response/bad-request msg))
                             (instance? NumberFormatException ex)
                             (response/bad-request "Invalid parameter format")
                             :else
                             (response/internal-server-error "Internal server error"))]
               (logger/log-call log :error "[Error Handler] %s %s | %s"
                                (name (:request-method request)) (:uri request) msg)
               (assoc ctx :response res)))}))
