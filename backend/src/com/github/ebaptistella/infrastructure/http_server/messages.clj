(ns com.github.ebaptistella.infrastructure.http-server.messages
  (:require [com.github.ebaptistella.config.reader :as config.reader]
            [com.github.ebaptistella.controllers.str.outbound :as controllers.outbound]
            [com.github.ebaptistella.controllers.str.str :as controllers.str]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.interceptors.components :as components]
            [com.github.ebaptistella.interface.http.response :as response]
            [com.github.ebaptistella.logic.str.ingest :as logic.ingest]
            [com.github.ebaptistella.wire.in.messages :refer [OutboundBody RespondBody]]
            [com.github.ebaptistella.wire.in.str.ingest :as wire.ingest]
            [com.github.ebaptistella.wire.out.str.messages :as wire.out.messages]
            [schema.core :as s])
  (:import [clojure.lang ExceptionInfo]))

(defn- enrich-with-available-responses [msg]
  (assoc msg :available-responses (controllers.str/available-responses msg)))

(s/defn list-messages [request]
  (let [store  (components/get-component request :store)
        params (:query-params request)
        limit  (or (some-> (or (get params "limit") (get params :limit)) Integer/parseInt) 20)
        offset (or (some-> (or (get params "offset") (get params :offset)) Integer/parseInt) 0)
        status (some-> (or (get params "status") (get params :status)) keyword)
        result (store.messages/list-messages store {:limit limit :offset offset :status status})
        enriched (update result :messages #(mapv enrich-with-available-responses %))]
    (response/ok (wire.out.messages/->list-response enriched))))

(s/defn get-message [request]
  (let [store (components/get-component request :store)
        id    (get-in request [:path-params :id])
        msg   (store.messages/find-by-id store id)]
    (if msg
      (response/ok (wire.out.messages/->single-response (enrich-with-available-responses msg)))
      (response/not-found (str "Message not found: " id)))))

(s/defn handle-respond [request]
  (let [store   (components/get-component request :store)
        config  (components/get-component request :config)
        mq-cfg  (config.reader/mq-config config)
        id      (get-in request [:path-params :id])
        raw     (:json-params request)]
    (try
      (let [req (-> (s/validate RespondBody raw)
                    (update :response-type keyword))
            msg (store.messages/find-by-id store id)]
        (if (nil? msg)
          (response/not-found (str "Message not found: " id))
          (let [result (controllers.str/respond! msg {:store store :mq-cfg mq-cfg} req)]
            (cond
              (= result {:ok true})
              (response/ok {:data {:message-id id :response-type (:response-type req)}})

              (= result {:error :already-responded})
              (response/conflict "Message already responded")

              (= result {:error :r2-already-sent})
              (response/conflict "R2 already sent for this message")

              (= result {:error :r2-requires-r1})
              (response/unprocessable-entity "R2 requires R1 to be sent first")

              (= result {:error :r3-requires-r2})
              (response/unprocessable-entity "R3 requires R2 to be sent first")

              (= result {:error :r3-already-sent})
              (response/conflict "R3 already sent for this message")

              (= result {:error :missing-motivo})
              (response/bad-request "MotivoRejeicao is required")

              (= result {:error :invalid-response-type})
              (response/bad-request "Invalid response-type")

              (= result {:error :unsupported-type})
              (response/bad-request (str "Unsupported message type: " (:type msg)))

              :else
              (response/internal-server-error "Unexpected respond result")))))
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
               :type         :STR0008
               :status       :pending
               :direction    :inbound
               :participant  "00000000"
               :queue-name   "QL.REQ.00000000.99999999.01"
               :message-id   (str "MSG-" (rand-int 999999))
               :num-ctrl-if  "CTL20260323-TEST"
               :body         "<STR0008><CodMsg>STR0008</CodMsg><NumCtrlIF>CTL20260323-TEST</NumCtrlIF><ISPBIFDebtd>00000000</ISPBIFDebtd><ISPBIFCredtd>99999999</ISPBIFCredtd><VlrLanc>5000.00</VlrLanc><FinlddCli>0001</FinlddCli><DtMovto>20260323</DtMovto></STR0008>"
               :received-at  (str (java.time.Instant/now))
               :vlr-lanc     "5000.00"
               :ispb-if-debtd "00000000"
               :ispb-if-credtd "99999999"
               :finldd-cli   "0001"
               :dt-movto     "20260323"}]
      (store.messages/save! store msg)
      (response/ok {:data {:id (:id msg) :type "STR0008"}}))))

;; Ingest handlers for message injection

(defn- ingest-message [msg-type request]
  (let [config  (components/get-component request :config)
        mq-cfg  (config.reader/mq-config config)
        raw     (:json-params request)]
    (try
      (let [schema  (wire.ingest/get-schema msg-type)]
        (if (nil? schema)
          (response/not-found (str "Message type not supported: " msg-type))
          (let [params (-> (s/validate schema raw)
                           (update-keys keyword))
                xml    (logic.ingest/build-xml-for-type msg-type params)]
            (mq.producer/send-message! mq-cfg "QL.REQ.00000000.99999999.01" xml)
            (response/created {:data {:message-type msg-type :status "injected"}}))))
      (catch ExceptionInfo e
        (if (= :schema.core/error (:type (ex-data e)))
          (response/bad-request (str "Invalid request body: " (ex-message e)))
          (response/internal-server-error (str "Ingest failed: " (ex-message e)))))
      (catch Exception e
        (response/internal-server-error (str "MQ or internal error: " (.getMessage e)))))))

;; Message type ingest handlers
(s/defn ingest-str0001 [request] (ingest-message "STR0001" request))
(s/defn ingest-str0003 [request] (ingest-message "STR0003" request))
(s/defn ingest-str0004 [request] (ingest-message "STR0004" request))
(s/defn ingest-str0005 [request] (ingest-message "STR0005" request))
(s/defn ingest-str0006 [request] (ingest-message "STR0006" request))
(s/defn ingest-str0007 [request] (ingest-message "STR0007" request))
(s/defn ingest-str0008 [request] (ingest-message "STR0008" request))
(s/defn ingest-str0010 [request] (ingest-message "STR0010" request))
(s/defn ingest-str0011 [request] (ingest-message "STR0011" request))
(s/defn ingest-str0012 [request] (ingest-message "STR0012" request))
(s/defn ingest-str0013 [request] (ingest-message "STR0013" request))
(s/defn ingest-str0014 [request] (ingest-message "STR0014" request))
(s/defn ingest-str0020 [request] (ingest-message "STR0020" request))
(s/defn ingest-str0021 [request] (ingest-message "STR0021" request))
(s/defn ingest-str0022 [request] (ingest-message "STR0022" request))
(s/defn ingest-str0025 [request] (ingest-message "STR0025" request))
(s/defn ingest-str0026 [request] (ingest-message "STR0026" request))
(s/defn ingest-str0029 [request] (ingest-message "STR0029" request))
(s/defn ingest-str0033 [request] (ingest-message "STR0033" request))
(s/defn ingest-str0034 [request] (ingest-message "STR0034" request))
(s/defn ingest-str0035 [request] (ingest-message "STR0035" request))
(s/defn ingest-str0037 [request] (ingest-message "STR0037" request))
(s/defn ingest-str0039 [request] (ingest-message "STR0039" request))
(s/defn ingest-str0040 [request] (ingest-message "STR0040" request))
(s/defn ingest-str0041 [request] (ingest-message "STR0041" request))
(s/defn ingest-str0043 [request] (ingest-message "STR0043" request))
(s/defn ingest-str0044 [request] (ingest-message "STR0044" request))
(s/defn ingest-str0045 [request] (ingest-message "STR0045" request))
(s/defn ingest-str0046 [request] (ingest-message "STR0046" request))
(s/defn ingest-str0047 [request] (ingest-message "STR0047" request))
(s/defn ingest-str0048 [request] (ingest-message "STR0048" request))
(s/defn ingest-str0051 [request] (ingest-message "STR0051" request))
(s/defn ingest-str0052 [request] (ingest-message "STR0052" request))
