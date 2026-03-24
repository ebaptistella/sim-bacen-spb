(ns com.github.ebaptistella.wire.in.str.str
  "Adapter: raw MQ message (XML) → STR domain model.
   Uses defmulti dispatching on CodMsg so each STR type
   can register its own defmethod in a specialized namespace."
  (:require [clojure.string :as str]
            [com.github.ebaptistella.logic.str.parser :as parser])
  (:import [java.time Instant]
           [java.util UUID]))

(defn- extract-cod-msg
  "Extracts <CodMsg> value from a raw STR XML body.
   Returns nil if not found or body is blank."
  [body]
  (when-not (str/blank? body)
    (second (re-find #"<CodMsg>(\w+)</CodMsg>" body))))


(defmulti parse-inbound
  "Parses a raw MQ message into a STR domain model map.
   Input:  {:queue-name :message-id :body<xml>}
   Output: domain map or nil if unparseable.
   Dispatch: CodMsg extracted from XML body."
  (fn [{:keys [body]}]
    (extract-cod-msg body)))

(defmethod parse-inbound :default
  [{:keys [queue-name message-id body]}]
  (let [cod-msg (extract-cod-msg body)]
    {:id          (str (UUID/randomUUID))
     :type        (or cod-msg :unknown)
     :num-ctrl-if nil
     :participant (parser/sender-ispb-from-queue queue-name)
     :queue-name  queue-name
     :message-id  message-id
     :body        body
     :received-at (str (Instant/now))
     :status      :pending
     :direction   :inbound
     :response    nil}))
