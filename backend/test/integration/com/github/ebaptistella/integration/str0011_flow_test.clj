(ns com.github.ebaptistella.integration.str0011-flow-test
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
            com.github.ebaptistella.controllers.str.str0011
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.integration.aux.init :as aux.init]
            com.github.ebaptistella.wire.in.str.str0011
            [state-flow.api :as sf :refer [defflow flow match?]])
  (:import [java.net URI]
           [java.net.http HttpClient HttpRequest HttpResponse$BodyHandlers HttpRequest$BodyPublishers]))

(defn- http-get-json
  [base-url path]
  (let [client (HttpClient/newHttpClient)
        uri    (URI/create (str base-url path))
        req    (-> (HttpRequest/newBuilder uri)
                   (.GET)
                   .build)
        resp   (.send client req (HttpResponse$BodyHandlers/ofString))]
    {:status (.statusCode resp)
     :body   (try (json/parse-string (.body resp) true)
                  (catch Exception _ (.body resp)))}))

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

(defn- sample-str0008-msg
  [id]
  {:id             id
   :type           :STR0008
   :status         :pending
   :direction      :inbound
   :num-ctrl-if    "NC-001"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "1500.00"
   :finldd-cli     "01"
   :dt-movto       "20240115"
   :participant    "00000000"
   :queue-name     "QL.REQ.00000000.99999999.01"
   :message-id     "mq-test-0008"
   :body           "<CodMsg>STR0008</CodMsg>"
   :received-at    "2025-01-01T00:00:00Z"})

(defn- sample-str0011-msg
  [id]
  {:id              id
   :type            :STR0011
   :status          :pending
   :direction       :inbound
   :num-ctrl-if     "NC-011"
   :ispb-if-debtd   "00000000"
   :num-ctrl-str-or "NC-001"
   :original-msg-id nil
   :participant     "00000000"
   :queue-name      "QL.REQ.00000000.99999999.01"
   :message-id      "mq-test-0011"
   :body            "<STR0011><CodMsg>STR0011</CodMsg></STR0011>"
   :received-at     "2025-01-01T00:00:00Z"})

(defn- flow-init
  []
  {:store    (aux.init/test-store)
   :base-url (aux.init/test-base-url)})

(use-fixtures :once
  (fn [f]
    (aux.init/start-test-system!)
    (f)
    (aux.init/stop-test-system!)))

(defflow str0011-respond-r1-cancelado
  {:init flow-init}
  (flow "POST respond STR0011R1 — 200, store :responded, MQ QR.* receives CANCELADO XML"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0011-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0011R1"})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? :STR0011R1 (sf/invoke #(-> (store.messages/find-by-id store id) :responses first :type)))
    (match? some? (sf/invoke #(-> (store.messages/find-by-id store id) :responses first :sent-at)))
    (match? "QR.REQ.99999999.00000000.01" (sf/invoke #(:queue @capture)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "STR0011R1")))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "CANCELADO")))))

(defflow str0011-respond-r1-lancamento-original-inalterado
  {:init flow-init}
  (flow "POST STR0011R1 — STR0008 original permanece com mesmo status"
    [store        (sf/get-state :store)
     base-url     (sf/get-state :base-url)
     id-str0008   (sf/invoke #(str (random-uuid)))
     id-str0011   (sf/invoke #(str (random-uuid)))
     _            (sf/invoke #(store.messages/save! store (assoc (sample-str0008-msg id-str0008)
                                                                  :id id-str0008)))
     status-antes (sf/invoke #(-> (store.messages/find-by-id store id-str0008) :status))
     _            (sf/invoke #(store.messages/save! store (assoc (sample-str0011-msg id-str0011)
                                                                  :id id-str0011
                                                                  :original-msg-id id-str0008)))
     _            (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                                (http-post-json base-url (str "/api/v1/messages/" id-str0011 "/respond")
                                                {:response-type "STR0011R1"})))]
    (match? status-antes (sf/invoke #(-> (store.messages/find-by-id store id-str0008) :status)))))

(defflow str0011-respond-e-sem-motivo
  {:init flow-init}
  (flow "POST STR0011E without MotivoRejeicao — 400, status remains :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0011-msg id) :id id)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0011E"}))]
    (match? 400 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? nil (sf/invoke #(-> (store.messages/find-by-id store id) :responses)))))

(defflow str0011-respond-e-com-motivo
  {:init flow-init}
  (flow "POST STR0011E with MotivoRejeicao — 200, store :responded, XML contains motivo"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0011-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0011E"
                                             :params        {"MotivoRejeicao" "AC09"}})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "AC09")))))

(defflow str0011-respond-409
  {:init flow-init}
  (flow "POST respond already-responded STR0011 — 409, store unchanged"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store
                                                 (-> (sample-str0011-msg id)
                                                     (assoc :status :responded
                                                            :responses [{:type    :STR0011R1
                                                                         :body    "<x/>"
                                                                         :sent-at "t"}]))))
     before   (sf/invoke #(store.messages/find-by-id store id))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0011R1"}))
     after    (sf/invoke #(store.messages/find-by-id store id))]
    (match? 409 (sf/invoke #(:status resp)))
    (match? before (sf/invoke #(identity after)))))

(defflow str0011-get-available-responses-excludes-r2
  {:init flow-init}
  (flow "GET /api/v1/messages/:id — STR0011 available-responses has R1 and E only"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0011-msg id) :id id)))
     resp     (sf/invoke #(http-get-json base-url (str "/api/v1/messages/" id)))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? ["STR0011R1" "STR0011E"]
            (sf/invoke #(-> resp :body :data :available-responses)))))

(defflow str0011-mq-failure
  {:init flow-init}
  (flow "MQ send failure on STR0011R1 — 500, store remains :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0011-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ _ _] (throw (ex-info "MQ failure" {})))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0011R1"})))]
    (match? 500 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? nil (sf/invoke #(-> (store.messages/find-by-id store id) :responses)))))
