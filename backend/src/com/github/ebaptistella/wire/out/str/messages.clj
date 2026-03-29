(ns com.github.ebaptistella.wire.out.str.messages
  "Adapter: STR domain model maps → HTTP response payload.")

(defn- ->message-response [msg]
  {:id                  (:id msg)
   :type                (some-> (:type msg) name)
   :status              (some-> (:status msg) name)
   :direction           (some-> (:direction msg) name)
   :participant         (:participant msg)
   :queue-name          (:queue-name msg)
   :message-id          (:message-id msg)
   :num-ctrl-if         (:num-ctrl-if msg)
   :received-at         (:received-at msg)
   :sent-at             (:sent-at msg)
   :ispb-if-debtd       (:ispb-if-debtd msg)
   :ispb-if-credtd      (:ispb-if-credtd msg)
   :vlr-lanc            (some-> (:vlr-lanc msg) str)
   :finldd-cli          (:finldd-cli msg)
   :finldd-if           (:finldd-if msg)
   :dt-movto            (:dt-movto msg)
   :body                (:body msg)
   :num-ctrl-str-or     (:num-ctrl-str-or msg)
   :cod-dev-transf      (:cod-dev-transf msg)
   :ispb-if-devedora    (:ispb-if-devedora msg)
   :tp-ct-debtd         (:tp-ct-debtd msg)
   :tp-ct-credtd        (:tp-ct-credtd msg)
   :agencia             (:agencia msg)
   :ct-pgto             (:ct-pgto msg)
   :hist                (:hist msg)
   :responses           (mapv #(update % :type name) (:responses msg))
   :available-responses (mapv name (or (:available-responses msg) []))})

(defn ->list-response
  [{:keys [messages total limit offset]}]
  {:data {:messages (mapv ->message-response messages)
          :total    total
          :limit    limit
          :offset   offset}})

(defn ->single-response [msg]
  {:data (->message-response msg)})
