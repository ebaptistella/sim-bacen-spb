(ns com.github.ebaptistella.controllers.str.query
  "Auto-response controller for STR query messages (STR0001/12/13/14).
   Follows Logic Sandwich: store queries → pure logic → MQ send + store update."
  (:require [com.github.ebaptistella.components.logger :as logger]
            [com.github.ebaptistella.config.reader :as config.reader]
            [com.github.ebaptistella.controllers.str.str :refer [process!]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.logic.str.parser :as parser]
            [com.github.ebaptistella.logic.str.str0001 :as logic.str0001]
            [com.github.ebaptistella.logic.str.str0012 :as logic.str0012]
            [com.github.ebaptistella.logic.str.str0013 :as logic.str0013]
            [com.github.ebaptistella.logic.str.str0014 :as logic.str0014])
  (:import [java.time Instant]))

;; ---- helpers ---------------------------------------------------------------

(defn- build-config-map
  "Extracts STR config values from config component.
   Returns defaults when config component is nil (e.g. during tests or if not wired)."
  [config-component]
  (if config-component
    {:str-horario-abertura   (config.reader/str-horario-abertura config-component)
     :str-horario-fechamento (config.reader/str-horario-fechamento config-component)
     :str-saldo-simulado     (config.reader/str-saldo-simulado config-component)}
    {:str-horario-abertura   "07:00"
     :str-horario-fechamento "17:30"
     :str-saldo-simulado     "99999999.99"}))

(defn- r1-queue [msg]
  (parser/r1-outbound-queue (:queue-name msg)))

(defn- persist-and-respond! [store msg r1-xml r1-type]
  (store.messages/save! store msg)
  (store.messages/update-message! store (:id msg)
                                  #(-> %
                                       (assoc :status :auto-responded)
                                       (update :responses (fnil conj [])
                                               {:type    r1-type
                                                :body    r1-xml
                                                :sent-at (str (Instant/now))}))))

;; ---- STR0001 handler -------------------------------------------------------

(defn- handle-str0001! [msg {:keys [store logger mq-cfg config]}]
  (logger/log-call logger :info "[STR] STR0001 received | id=%s" (:message-id msg))
  (let [cfg    (build-config-map config)
        r1-xml (logic.str0001/r1-response msg cfg)
        queue  (r1-queue msg)]
    (mq.producer/send-message! mq-cfg queue r1-xml)
    (persist-and-respond! store msg r1-xml :STR0001R1)))

;; ---- STR0012 handler -------------------------------------------------------

(defn- handle-str0012! [msg {:keys [store logger mq-cfg]}]
  (logger/log-call logger :info "[STR] STR0012 received | id=%s" (:message-id msg))
  (let [all-msgs    (store.messages/get-by-dt-movto store (:dt-movto msg))
        lancamentos (logic.str0012/filter-lancamentos (vec all-msgs) msg)
        r1-xml      (logic.str0012/r1-response msg (vec lancamentos))
        queue       (r1-queue msg)]
    (mq.producer/send-message! mq-cfg queue r1-xml)
    (persist-and-respond! store msg r1-xml :STR0012R1)))

;; ---- STR0013 handler -------------------------------------------------------

(defn- handle-str0013! [msg {:keys [store logger mq-cfg config]}]
  (logger/log-call logger :info "[STR] STR0013 received | id=%s" (:message-id msg))
  (let [cfg    (build-config-map config)
        r1-xml (logic.str0013/r1-response msg cfg)
        queue  (r1-queue msg)]
    (mq.producer/send-message! mq-cfg queue r1-xml)
    (persist-and-respond! store msg r1-xml :STR0013R1)))

;; ---- STR0014 handler -------------------------------------------------------

(defn- handle-str0014! [msg {:keys [store logger mq-cfg]}]
  (logger/log-call logger :info "[STR] STR0014 received | id=%s" (:message-id msg))
  (let [all-msgs   (store.messages/get-by-period store (:dt-ref msg) (:hr-ini msg) (:hr-fim msg))
        movimentos (logic.str0014/filter-extrato (vec all-msgs) msg)
        r1-xml     (logic.str0014/r1-response msg (vec movimentos))
        queue      (r1-queue msg)]
    (mq.producer/send-message! mq-cfg queue r1-xml)
    (persist-and-respond! store msg r1-xml :STR0014R1)))

;; ---- defmethods ------------------------------------------------------------

(defmethod process! :STR0001
  [msg components]
  (handle-str0001! msg components))

(defmethod process! :STR0012
  [msg components]
  (handle-str0012! msg components))

(defmethod process! :STR0013
  [msg components]
  (handle-str0013! msg components))

(defmethod process! :STR0014
  [msg components]
  (handle-str0014! msg components))
