(ns com.github.ebaptistella.wire.out.str.messages
  "Adapter: STR domain model maps → HTTP response payload.")

(defn- ->message-response [msg]
  {:id              (:id msg)
   :type            (some-> (:type msg) str)
   :status          (some-> (:status msg) name)
   :direction       (some-> (:direction msg) name)
   :participant     (:participant msg)
   :queue-name      (:queue-name msg)
   :message-id      (:message-id msg)
   :num-ctrl-if     (:num-ctrl-if msg)
   :received-at     (:received-at msg)
   :ispb-if-debtd   (:ispb-if-debtd msg)
   :ispb-if-credtd  (:ispb-if-credtd msg)
   :vlr-lanc        (some-> (:vlr-lanc msg) str)
   :finldd-cli      (:finldd-cli msg)
   :dt-movto        (:dt-movto msg)
   :response        (:response msg)
   :r2-response     (:r2-response msg)})

(defn ->list-response
  [{:keys [messages total limit offset]}]
  {:data {:messages (mapv ->message-response messages)
          :total    total
          :limit    limit
          :offset   offset}})

(defn ->single-response [msg]
  {:data (->message-response msg)})
