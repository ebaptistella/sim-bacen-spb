(ns com.github.ebaptistella.interceptors.logging
  (:require [cheshire.core :as json]
            [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.interceptors.components :as interceptors.components]
            [io.pedestal.interceptor :as interceptor]
            [schema.core :as s]))

(s/defn ^:private sanitize-body [body max-size]
  (cond
    (nil? body) nil
    (string? body) (if (> (count body) max-size) (str (subs body 0 max-size) "... [truncated]") body)
    (map? body) (let [s (json/generate-string (dissoc body :password :token :secret))]
                  (if (> (count s) max-size) (str (subs s 0 max-size) "... [truncated]") (dissoc body :password :token :secret)))
    (instance? java.io.InputStream body) "[InputStream]"
    :else (let [s (str body)] (if (> (count s) max-size) (str (subs s 0 max-size) "... [truncated]") s))))

(s/defn ^:private log-request [logger-comp request]
  (when logger-comp
    (let [log (logger/bound logger-comp)]
      (logger/log-call log :info "[Request] %s %s | Headers: %s | Body: %s"
                       (name (:request-method request))
                       (:uri request)
                       (select-keys (:headers request) ["content-type" "authorization" "user-agent"])
                       (sanitize-body (or (:json-params request) (:body request)) 500)))))

(s/defn ^:private log-response [logger-comp response request]
  (when logger-comp
    (let [log (logger/bound logger-comp)]
      (logger/log-call log :info "[Response] %s %s | Status: %s | Body: %s"
                       (name (:request-method request))
                       (:uri request)
                       (:status response)
                       (sanitize-body (:body response) 500)))))

(def logging-interceptor
  (interceptor/interceptor
   {:name ::logging
    :enter (fn [ctx]
             (log-request (interceptors.components/get-component (:request ctx) :logger) (:request ctx))
             ctx)
    :leave (fn [ctx]
             (log-response (interceptors.components/get-component (:request ctx) :logger) (:response ctx) (:request ctx))
             ctx)}))
