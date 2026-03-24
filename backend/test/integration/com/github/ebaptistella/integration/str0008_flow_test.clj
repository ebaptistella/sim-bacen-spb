(ns com.github.ebaptistella.integration.str0008-flow-test
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
            com.github.ebaptistella.controllers.str.str0008
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.integration.aux.init :as aux.init]
            com.github.ebaptistella.wire.in.str.str0008
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

(defn- sample-str0008-msg
  [id]
  {:id             id
   :type           "STR0008"
   :status         :pending
   :direction      :inbound
   :num-ctrl-if    "NC123"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "1500.00"
   :finldd-cli     "01"
   :dt-movto       "20240115"
   :participant    "00000000"
   :queue-name     "QL.REQ.00000000.99999999.01"
   :message-id     "mq-test"
   :body           "<CodMsg>STR0008</CodMsg>"
   :received-at    "2025-01-01T00:00:00Z"
   :response       nil})

(defn- flow-init
  []
  {:store    (aux.init/test-store)
   :base-url (aux.init/test-base-url)})

(use-fixtures :once
  (fn [f]
    (aux.init/start-test-system!)
    (f)
    (aux.init/stop-test-system!)))

(defflow str0008-respond-r1
  {:init flow-init}
  (flow "POST respond STR0008R1 — 200, store :responded, MQ QR.* receives R1 XML"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0008-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0008R1"})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? "STR0008R1" (sf/invoke #(-> (store.messages/find-by-id store id) :response :type)))
    (match? some? (sf/invoke #(-> (store.messages/find-by-id store id) :response :sent-at)))
    (match? "QR.REQ.99999999.00000000.01" (sf/invoke #(:queue @capture)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "STR0008R1")))))

(defflow str0008-respond-404
  {:init flow-init}
  (flow "POST respond unknown id — 404, store unchanged"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     n-before (sf/invoke #(count (:messages @(:store store))))
     fake-id  (sf/invoke #(str (random-uuid)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" fake-id "/respond")
                                          {:response-type "STR0008R1"}))
     n-after  (sf/invoke #(count (:messages @(:store store))))]
    (match? 404 (sf/invoke #(:status resp)))
    (match? n-before (sf/invoke #(identity n-after)))))

(defflow str0008-respond-409
  {:init flow-init}
  (flow "POST respond already-responded message — 409, store unchanged"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store
                                                 (-> (sample-str0008-msg id)
                                                     (assoc :status :responded
                                                            :response {:type "STR0008R1"
                                                                       :body   "<x/>"
                                                                       :sent-at "t"}))))
     before   (sf/invoke #(store.messages/find-by-id store id))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0008R1"}))
     after    (sf/invoke #(store.messages/find-by-id store id))]
    (match? 409 (sf/invoke #(:status resp)))
    (match? before (sf/invoke #(identity after)))))

(defflow str0008-respond-e-sem-motivo
  {:init flow-init}
  (flow "POST STR0008E without MotivoRejeicao — 400, status remains :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0008-msg id) :id id)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0008E"}))]
    (match? 400 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? nil (sf/invoke #(-> (store.messages/find-by-id store id) :response)))))

(defflow str0008-r2-before-r1
  {:init flow-init}
  (flow "POST STR0008R2 on pending message — 422, status remains :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0008-msg id) :id id)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0008R2"}))]
    (match? 422 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? nil (sf/invoke #(-> (store.messages/find-by-id store id) :r2-response)))))

(defflow str0008-r1-then-r2-same-num-ctrl-str
  {:init flow-init}
  (flow "POST R1 then R2 — R2 XML contains same NumCtrlSTR as R1"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom {:r1-xml nil :r2-xml nil}))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0008-msg id) :id id)))
     _        (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ _ body] (swap! capture assoc :r1-xml body))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0008R1"})))
     _        (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ _ body] (swap! capture assoc :r2-xml body))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0008R2"})))]
    (match? some? (sf/invoke #(re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" (:r1-xml @capture))))
    (match? some? (sf/invoke #(re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" (:r2-xml @capture))))
    (match? true  (sf/invoke #(let [r1-ncs (second (re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" (:r1-xml @capture)))
                                    r2-ncs (second (re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" (:r2-xml @capture)))]
                                (= r1-ncs r2-ncs))))))

(defflow str0008-mq-failure
  {:init flow-init}
  (flow "MQ send failure — 500, store remains :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0008-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ _ _] (throw (ex-info "MQ failure" {})))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0008R1"})))]
    (match? 500 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? nil (sf/invoke #(-> (store.messages/find-by-id store id) :response)))))
