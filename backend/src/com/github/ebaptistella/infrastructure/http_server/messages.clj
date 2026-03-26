(ns com.github.ebaptistella.infrastructure.http-server.messages
  (:require [com.github.ebaptistella.config.reader :as config.reader]
            [com.github.ebaptistella.controllers.str.outbound :as controllers.outbound]
            [com.github.ebaptistella.controllers.str.str0008 :as controllers.str0008]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.interceptors.components :as components]
            [com.github.ebaptistella.interface.http.response :as response]
            [com.github.ebaptistella.wire.in.messages :refer [OutboundBody RespondBody]]
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

          (= result {:error :r2-already-sent})
          (response/conflict "STR0008R2 already sent for this message")

          (= result {:error :r2-requires-r1})
          (response/unprocessable-entity "R2 requires R1 to be sent first")

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


(s/defn handle-outbound [request]
  (let [store  (components/get-component request :store)
        config (components/get-component request :config)
        mq-cfg (config.reader/mq-config config)
        raw    (:json-params request)]
    (try
      (let [req         (s/validate OutboundBody raw)
            msg-type    (:type req)
            participant (:participant req)
            params      (or (:params req) {})
            result      (controllers.outbound/send-outbound! msg-type participant params
                                                             {:store  store
                                                              :mq-cfg mq-cfg
                                                              :config config})]
        (cond
          (:ok result)
          (response/created {:data {:id      (:id result)
                                    :type    msg-type
                                    :sent-at (:sent-at result)}})

          (#{:invalid-type :invalid-participant} (:error result))
          (response/bad-request (str "Invalid request: " (name (:error result))))

          (= :mq-error (:error result))
          (response/internal-server-error "Falha ao enviar na fila MQ")

          :else
          (response/internal-server-error "Unexpected outbound result")))
      (catch ExceptionInfo _
        (response/bad-request "Tipo ou parâmetros inválidos"))
      (catch Exception e
        (response/internal-server-error (or (.getMessage e) "MQ or internal error"))))))

(defn test-inject-message [request]
  (if-not (= "true" (System/getenv "ENABLE_TEST_ENDPOINTS"))
    (response/not-found "Not found")
    (let [store (components/get-component request :store)
        msg   {:id           (str (java.util.UUID/randomUUID))
               :type         "STR0008"
               :status       :pending
               :direction    :inbound
               :participant  "00000000"
               :queue-name   "QL.REQ.00000000.99999999.01"
               :message-id   (str "MSG-" (rand-int 999999))
               :num-ctrl-if  "CTL20260323-TEST"
               :body         "<STR0008><CodMsg>STR0008</CodMsg><NumCtrlIF>CTL20260323-TEST</NumCtrlIF><ISPBIFDebtd>00000000</ISPBIFDebtd><ISPBIFCredtd>99999999</ISPBIFCredtd><VlrLanc>5000.00</VlrLanc><FinlddCli>0001</FinlddCli><DtMovto>20260323</DtMovto></STR0008>"
               :received-at  (str (java.time.Instant/now))
               :response     nil
               :vlr-lanc     "5000.00"
               :ispb-if-debtd "00000000"
               :ispb-if-credtd "99999999"
               :finldd-cli   "0001"
               :dt-movto     "20260323"}]
      (store.messages/save! store msg)
      (response/ok {:data {:id (:id msg) :type "STR0008"}}))))
