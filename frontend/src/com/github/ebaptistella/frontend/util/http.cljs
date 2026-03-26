(ns com.github.ebaptistella.frontend.util.http
  (:require [cljs.core.async :refer [chan put! go <! timeout]]))

;; HTTP com retry automático e timeout via AbortController.
;; Timeout: 10s por request. Se exceder, AbortController.abort() cancela o fetch.
;; Retry: até 3 tentativas com backoff exponencial (500ms, 1s, 2s).
;; Retryable: network errors e timeouts. Erros 4xx NÃO fazem retry (erro do usuário).

(def ^:private request-timeout-ms 10000)
(def ^:private max-retries 3)
(def ^:private backoff-ms [500 1000 2000])

(defn- parse-json [text]
  (try
    (js->clj (.parse js/JSON text) :keywordize-keys true)
    (catch :default _
      nil)))

(defn- do-fetch [url opts]
  (let [ch (chan 1)
        controller (js/AbortController.)
        signal     (.-signal controller)
        timer-id   (js/setTimeout #(.abort controller) request-timeout-ms)]
    (-> (js/fetch url (clj->js (assoc opts :signal signal)))
        (.then (fn [response]
                 (js/clearTimeout timer-id)
                 (-> (.text response)
                     (.then (fn [text]
                              (put! ch {:ok?    (.-ok response)
                                        :status (.-status response)
                                        :body   (parse-json text)}))))))
        (.catch (fn [error]
                  (js/clearTimeout timer-id)
                  (let [aborted? (= (.-name error) "AbortError")]
                    (put! ch {:ok?   false
                              :status 0
                              :error (if aborted? :timeout :network-error)
                              :body  nil})))))
    ch))

(defn- retryable-error? [{:keys [ok? error]}]
  (or (= error :network-error) (= error :timeout)))

(defn- fetch-with-retry [url opts]
  (go
    (loop [attempt 0]
      (let [result (<! (do-fetch url opts))]
        (if (and (retryable-error? result)
                 (< attempt max-retries))
          (do
            (<! (timeout (get backoff-ms attempt 2000)))
            (recur (inc attempt)))
          result)))))

(defn fetch-messages [limit offset]
  (fetch-with-retry
   (str "/api/v1/messages?limit=" limit "&offset=" offset)
   {:method  "GET"
    :headers {"Accept" "application/json"}}))

(defn post-response [id body]
  (fetch-with-retry
   (str "/api/v1/messages/" id "/respond")
   {:method  "POST"
    :headers {"Content-Type" "application/json"
              "Accept"       "application/json"}
    :body    (.stringify js/JSON (clj->js body))}))

(defn post-outbound [body]
  (fetch-with-retry
   "/api/v1/messages/outbound"
   {:method  "POST"
    :headers {"Content-Type" "application/json"
              "Accept"       "application/json"}
    :body    (.stringify js/JSON (clj->js body))}))
