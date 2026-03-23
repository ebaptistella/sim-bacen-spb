(ns com.github.ebaptistella.infrastructure.http-server.messages
  (:require [com.github.ebaptistella.config.reader :as config.reader]
            [com.github.ebaptistella.controllers.str.str0008 :as controllers.str0008]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.interceptors.components :as components]
            [com.github.ebaptistella.interface.http.response :as response]
            [com.github.ebaptistella.wire.in.messages :refer [RespondBody]]
            [com.github.ebaptistella.wire.out.str.messages :as wire.out.messages]
            [schema.core :as s])
  (:import [clojure.lang ExceptionInfo]))

(s/defn list-messages [request]
  (let [store  (components/get-component request :store)
        params (:query-params request)
        limit  (or (some-> (get params "limit") Integer/parseInt) 20)
        offset (or (some-> (get params "offset") Integer/parseInt) 0)
        status (some-> (get params "status") keyword)
        result (store.messages/list-messages store {:limit limit :offset offset :status status})]
    (response/ok (wire.out.messages/->list-response result))))

(s/defn get-message [request]
  (let [store (components/get-component request :store)
        id    (get-in request [:path-params :id])
        msg   (store.messages/find-by-id store id)]
    (if msg
      (response/ok (wire.out.messages/->single-response msg))
      (response/not-found (str "Message not found: " id)))))

(s/defn handle-respond [request]
  (let [store   (components/get-component request :store)
        config  (components/get-component request :config)
        mq-cfg  (config.reader/mq-config config)
        id      (get-in request [:path-params :id])
        raw     (:json-params request)]
    (try
      (let [req    (s/validate RespondBody raw)
            result (controllers.str0008/send-response! store mq-cfg id req)]
        (cond
          (= result {:ok true})
          (response/ok {:data {:message-id id :response-type (:response-type req)}})

          (= result {:error :not-found})
          (response/not-found (str "Message not found: " id))

          (= result {:error :already-responded})
          (response/conflict "Message already responded")

          (= result {:error :missing-motivo})
          (response/bad-request "MotivoRejeicao is required for STR0008E")

          (= result {:error :invalid-response-type})
          (response/bad-request "Invalid response-type")

          :else
          (response/internal-server-error "Unexpected respond result")))
      (catch ExceptionInfo e
        (cond
          (= :schema.core/error (:type (ex-data e)))
          (response/bad-request "Invalid request body")

          :else
          (response/internal-server-error (or (.getMessage e) "Respond failed"))))
      (catch Exception e
        (response/internal-server-error (or (.getMessage e) "MQ or internal error"))))))
