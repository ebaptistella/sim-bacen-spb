(ns com.github.ebaptistella.integration.str0012-flow-test
  (:require [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
            com.github.ebaptistella.controllers.str.query
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.integration.aux.init :as aux.init]
            com.github.ebaptistella.wire.in.str.str0012
            [state-flow.api :as sf :refer [defflow flow match?]]))

;; ---- sample builders -------------------------------------------------------

(defn- sample-str0012-msg
  [id dt-movto]
  {:id            id
   :type          :STR0012
   :status        :pending
   :direction     :inbound
   :num-ctrl-if   "NC-Q-001"
   :ispb-if-debtd "00000000"
   :dt-movto      dt-movto
   :num-ctrl-str-or nil
   :sit-lanc-str  nil
   :participant   "00000000"
   :queue-name    "QL.REQ.00000000.99999999.01"
   :message-id    "mq-q12-test"
   :body          (str "<STR0012><CodMsg>STR0012</CodMsg>"
                       "<NumCtrlIF>NC-Q-001</NumCtrlIF>"
                       "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                       "<DtMovto>" dt-movto "</DtMovto>"
                       "</STR0012>")
   :received-at   "2099-12-31T23:59:59Z"})

(defn- sample-str0008-responded-msg
  "A completed STR0008 that will match dt-movto of today."
  [id received-at]
  {:id             id
   :type           :STR0008
   :status         :responded
   :direction      :inbound
   :num-ctrl-if    "NC-8-001"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "3000.00"
   :finldd-cli     "01"
   :dt-movto       "20260324"
   :participant    "00000000"
   :queue-name     "QL.REQ.00000000.99999999.01"
   :message-id     "mq-8-test"
   :body           "<CodMsg>STR0008</CodMsg>"
   :received-at    received-at
   :responses      [{:type         :STR0008R1
                     :num-ctrl-str "ABCD1234567890ABCD12"
                     :body         "<STR0008R1/>"
                     :sent-at      received-at}]})

;; ---- flow helpers ----------------------------------------------------------

(defn- flow-init
  []
  {:store    (aux.init/test-store)
   :base-url (aux.init/test-base-url)})

(defn- call-process!
  "Calls process! on the given msg using the real store and a stubbed MQ.
   Returns the captured {:queue q :xml xml} atom."
  [msg store capture]
  (with-redefs [mq.producer/send-message!
                (fn [_ q body] (reset! capture {:queue q :xml body}))]
    (com.github.ebaptistella.controllers.str.str/process!
     msg
     {:store  store
      :logger nil
      :mq-cfg {}
      :config nil})))

;; ---- fixtures --------------------------------------------------------------

(use-fixtures :once
  (fn [f]
    (aux.init/start-test-system!)
    (f)
    (aux.init/stop-test-system!)))

;; ---- flows -----------------------------------------------------------------

(defflow str0012-store-empty-returns-zero-lancamentos
  {:init flow-init}
  (flow "STR0012 process! with no matching store entries — R1 contains <QtdLanc>0</QtdLanc>"
    [store   (sf/get-state :store)
     capture (sf/invoke #(atom nil))
     id      (sf/invoke #(str (random-uuid)))
     ;; Use a unique far-future date (no other test uses this date)
     msg     (sf/invoke #(sample-str0012-msg id "29990101"))
     _       (sf/invoke #(call-process! msg store capture))]
    (match? true
            (sf/invoke #(str/includes? (:xml @capture) "<QtdLanc>0</QtdLanc>")))
    (match? :auto-responded
            (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? :STR0012R1
            (sf/invoke #(-> (store.messages/find-by-id store id) :responses first :type)))))

(defflow str0012-store-with-lancamento-returns-one
  {:init flow-init}
  (flow "STR0012 process! with 1 STR0008 matching DtMovto — R1 contains <QtdLanc>1</QtdLanc>"
    [store     (sf/get-state :store)
     capture   (sf/invoke #(atom nil))
     id-8      (sf/invoke #(str (random-uuid)))
     id-12     (sf/invoke #(str (random-uuid)))
     ;; STR0008 received on 2026-03-24 (UTC)
     _         (sf/invoke #(store.messages/save!
                             store
                             (sample-str0008-responded-msg id-8 "2026-03-24T12:00:00Z")))
     ;; STR0012 querying the same date
     msg       (sf/invoke #(sample-str0012-msg id-12 "20260324"))
     _         (sf/invoke #(call-process! msg store capture))]
    (match? true
            (sf/invoke #(str/includes? (:xml @capture) "<QtdLanc>1</QtdLanc>")))
    (match? :auto-responded
            (sf/invoke #(-> (store.messages/find-by-id store id-12) :status)))
    (match? :STR0012R1
            (sf/invoke #(-> (store.messages/find-by-id store id-12) :responses first :type)))))

(defflow str0012-r1-xml-contains-num-ctrl-str
  {:init flow-init}
  (flow "STR0012 R1 XML contains a <NumCtrlSTR> element"
    [store   (sf/get-state :store)
     capture (sf/invoke #(atom nil))
     id      (sf/invoke #(str (random-uuid)))
     ;; Use a unique far-future date (no other test uses this date)
     msg     (sf/invoke #(sample-str0012-msg id "29980101"))
     _       (sf/invoke #(call-process! msg store capture))]
    (match? some?
            (sf/invoke #(re-find #"<NumCtrlSTR>[^<]+</NumCtrlSTR>" (:xml @capture))))))

(defflow str0012-mq-failure-does-not-persist-response
  {:init flow-init}
  (flow "STR0012 process! with MQ failure — store entry not updated to :auto-responded"
    [store (sf/get-state :store)
     id    (sf/invoke #(str (random-uuid)))
     ;; Use a unique far-future date (no other test uses this date)
     msg   (sf/invoke #(sample-str0012-msg id "29970101"))
     _     (sf/invoke #(try
                         (with-redefs [mq.producer/send-message!
                                       (fn [_ _ _] (throw (ex-info "MQ down" {})))]
                           (com.github.ebaptistella.controllers.str.str/process!
                            msg
                            {:store  store
                             :logger nil
                             :mq-cfg {}
                             :config nil}))
                         (catch Exception _ nil)))]
    ;; message was saved (default process!) but response is nil
    (match? nil
            (sf/invoke #(-> (store.messages/find-by-id store id) :responses)))))
