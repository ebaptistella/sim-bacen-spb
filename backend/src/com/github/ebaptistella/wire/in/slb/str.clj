(ns com.github.ebaptistella.wire.in.slb.str
  "Adapter: raw MQ message (XML) → SLB domain model."
  (:require [clojure.string :as str]
            [com.github.ebaptistella.logic.slb.parser :as parser])
  (:import [java.time Instant]
           [java.util UUID]))

(defn- extract-cod-msg
  "Extracts <CodMsg> value from a raw SLB XML body."
  [body]
  (when-not (str/blank? body)
    (second (re-find #"<CodMsg>([A-Z0-9]+)</CodMsg>" body))))

(defn- extract-field
  "Extracts a specific field value from XML body."
  [body field-name]
  (when body
    (second (re-find (re-pattern (str "<" field-name ">([^<]*)</" field-name ">")) body))))

(defmulti parse-inbound
  "Parses raw MQ message (SLB XML) into domain map.
   Dispatch: CodMsg extracted from XML, coerced to keyword."
  (fn [{:keys [body]}]
    (some-> body extract-cod-msg keyword)))

(defmethod parse-inbound :default
  [{:keys [queue-name message-id body]}]
  (let [cod-msg (extract-cod-msg body)
        msg-type (or cod-msg "UNKNOWN")]
    {:id          (str (UUID/randomUUID))
     :type        msg-type
     :status      :pending
     :direction   :inbound
     :participant (when body (or (extract-field body "ISPBPart") "00000000"))
     :queue-name  queue-name
     :message-id  message-id
     :num-ctrl-part (extract-field body "NumCtrlPart")
     :num-ctrl-slb  (extract-field body "NumCtrlSLB")
     :body        body
     :received-at (str (Instant/now))}))

(defmulti parse-response
  "Parses SLB response messages (R1 types)."
  (fn [{:keys [body]}]
    (some-> body extract-cod-msg keyword)))

(defmethod parse-response :default
  [{:keys [queue-name message-id body]}]
  (try
    (let [parsed-fields (parser/parse-slb-response body)]
      {:id          (str (UUID/randomUUID))
       :type        (:type parsed-fields)
       :status      :pending
       :direction   :inbound
       :participant (get parsed-fields :ISPBPart "00000000")
       :queue-name  queue-name
       :message-id  message-id
       :num-ctrl-part (:NumCtrlPart parsed-fields)
       :data        (:data parsed-fields)
       :body        body
       :received-at (str (Instant/now))
       :parsed-fields (dissoc parsed-fields :data)})
    (catch Exception _
      {:id          (str (UUID/randomUUID))
       :type        (extract-cod-msg body)
       :status      :pending
       :direction   :inbound
       :participant "00000000"
       :queue-name  queue-name
       :message-id  message-id
       :body        body
       :received-at (str (Instant/now))
       :error       "Failed to parse response"})))
