(ns com.github.ebaptistella.integration.str0007-flow-test
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
            com.github.ebaptistella.controllers.str.str0007
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.integration.aux.init :as aux.init]
            com.github.ebaptistella.wire.in.str.str0007
            [state-flow.api :as sf :refer [defflow flow match?]])
  (:import [java.net URI]
           [java.net.http HttpClient HttpRequest HttpResponse$BodyHandlers HttpRequest$BodyPublishers]))

(defn- http-post-json
  [base-url path body-map]
  (let [client (HttpClient/newHttpClient)
        uri    (URI/create (str base-url path))
        body   (json/generate-string body-map)
        req    (-> (HttpRequest/newBuilder uri)
                   (.header "Content-Type" "application/json")
                   (.POST (HttpRequest$BodyPublishers/ofString body))
                   .build)
        resp   (.send client req (HttpResponse$BodyHandlers/ofString))]
    {:status (.statusCode resp)
     :body   (try (json/parse-string (.body resp) true)
                  (catch Exception _ (.body resp)))}))

(defn- sample-str0007-msg
  [id]
  {:id             id
   :type           :STR0007
   :status         :pending
   :direction      :inbound
   :num-ctrl-if    "NC-007"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "4000.00"
   :finldd-if      "00001"
   :dt-movto       "20260103"
   :participant    "00000000"
   :queue-name     "QL.REQ.00000000.99999999.01"
   :message-id     "mq-test-0007"
   :body           "<STR0007><CodMsg>STR0007</CodMsg></STR0007>"
   :received-at    "2025-01-01T00:00:00Z"})

(defn- flow-init
  []
  {:store    (aux.init/test-store)
   :base-url (aux.init/test-base-url)})

(use-fixtures :once
  (fn [f]
    (aux.init/start-test-system!)
    (f)
    (aux.init/stop-test-system!)))

(defflow str0007-respond-r1
  {:init flow-init}
  (flow "POST respond STR0007R1 — 200, store :responded, XML contains STR0007R1 and LQDADO"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0007-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0007R1"})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? :STR0007R1 (sf/invoke #(-> (store.messages/find-by-id store id) :responses first :type)))
    (match? some? (sf/invoke #(-> (store.messages/find-by-id store id) :responses first :sent-at)))
    (match? "QR.REQ.99999999.00000000.01" (sf/invoke #(:queue @capture)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "STR0007R1")))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "LQDADO")))))

(defflow str0007-respond-r1-then-r2-finldd-if
  {:init flow-init}
  (flow "POST R1 then R2 — R2 XML has FinlddIF, no FinlddCli, correct queue, same NumCtrlSTR"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom {:r1-xml nil :r1-queue nil :r2-xml nil :r2-queue nil}))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0007-msg id) :id id)))
     _        (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (swap! capture assoc :r1-xml body :r1-queue q))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0007R1"})))
     _        (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (swap! capture assoc :r2-xml body :r2-queue q))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0007R2"})))]
    (match? true (sf/invoke #(str/includes? (:r2-xml @capture) "<FinlddIF>00001</FinlddIF>")))
    (match? true (sf/invoke #(not (str/includes? (:r2-xml @capture) "<FinlddCli>"))))
    (match? "QR.REQ.99999999.11111111.01" (sf/invoke #(:r2-queue @capture)))
    (match? true (sf/invoke #(let [r1-ncs (second (re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" (:r1-xml @capture)))
                                   r2-ncs (second (re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" (:r2-xml @capture)))]
                               (= r1-ncs r2-ncs))))))

(defflow str0007-respond-e-com-motivo
  {:init flow-init}
  (flow "POST STR0007E with MotivoRejeicao — 200, store :responded, XML contains rejection code"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0007-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0007E"
                                             :params        {"MotivoRejeicao" "AC09"}})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "AC09")))))

(defflow str0007-r2-before-r1
  {:init flow-init}
  (flow "POST STR0007R2 on pending message — 422, status remains :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0007-msg id) :id id)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0007R2"}))]
    (match? 422 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? nil (sf/invoke #(-> (store.messages/find-by-id store id) :responses)))))

(defflow str0007-mq-failure
  {:init flow-init}
  (flow "MQ send failure — 500, store remains :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0007-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ _ _] (throw (ex-info "MQ failure" {})))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0007R1"})))]
    (match? 500 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? nil (sf/invoke #(-> (store.messages/find-by-id store id) :responses)))))
