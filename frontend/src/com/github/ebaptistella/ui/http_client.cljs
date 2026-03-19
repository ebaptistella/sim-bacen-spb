(ns com.github.ebaptistella.ui.http-client
  "HTTP client for API communication (External Layer). Returns raw response/wire; no domain logic."
  (:require [clojure.string :as str]))

;; Base URL for API. Empty string = same origin. Set js/window.API_BASE_URL to override.
(def ^:dynamic *api-base*
  (or (when (and js/window (.-API_BASE_URL js/window))
        (str (.-API_BASE_URL js/window)))
      ""))

(defn- get-api-url
  [path]
  (let [base (str/replace *api-base* #"/$" "")
        p    (if (str/starts-with? path "/") path (str "/" path))]
    (str base p)))

(defn- json-response?
  [response]
  (let [ct (-> response .-headers (.get "content-type") (or ""))]
    (or (.includes ct "application/json")
        (.includes ct "text/json"))))

(defn- success-status?
  [status]
  (and (>= status 200) (< status 300)))

(defn- parse-body
  [response]
  (if (json-response? response)
    (.then (.json response) #(js->clj % :keywordize-keys true))
    (.then (.text response) (fn [_] nil))))

(defn request-raw
  "Makes an HTTP request. Returns a promise that resolves to {:ok bool :status int :data data}.
   :data is parsed JSON (keywordized) or nil. Caller/adapter interprets :data."
  [method path opts]
  (let [url     (get-api-url path)
        options (merge {:method  (name method)
                        :headers {"Content-Type" "application/json"}}
                       (when (:body opts)
                         {:body (if (string? (:body opts))
                                  (:body opts)
                                  (js/JSON.stringify (clj->js (:body opts))))}))]
    (-> (js/fetch url (clj->js options))
        (.then (fn [response]
                 (let [status (.-status response)
                       ok?    (success-status? status)]
                   (-> (parse-body response)
                       (.then (fn [data]
                                (clj->js {:ok ok? :status status :data (or data {})})))
                       (.catch (fn [_]
                                 (js/Promise.resolve (clj->js {:ok false :status status :data nil}))))))))
        (.then (fn [v] (js->clj v :keywordize-keys true)))
        (.catch (fn [_]
                  (js/Promise.resolve (clj->js {:ok false :status 0 :data nil}))))
        (.then (fn [v] (js->clj v :keywordize-keys true))))))

(defn post-raw
  "POST path with body (clj map). Returns promise of {:ok :status :data} (wire)."
  [path body]
  (request-raw :post path {:body body}))

(defn get-raw
  "GET path with optional query params. Returns promise of {:ok :status :data} (wire)."
  [path query-params]
  (let [q    (when (seq query-params)
               (str "?" (str/join "&" (map (fn [[k v]]
                                             (str (name k) "=" (js/encodeURIComponent (str v))))
                                           query-params))))
        full (str path (or q ""))]
    (request-raw :get full {})))
