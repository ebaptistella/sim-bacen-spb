(ns com.github.ebaptistella.integration.str0045-flow-test
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
            com.github.ebaptistella.controllers.str.str0045
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.integration.aux.init :as aux.init]
            com.github.ebaptistella.wire.in.str.str0045
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

(defn- sample-str0045-msg
  [id]
  {:id             id
   :type           :STR0045
   :status         :pending
   :direction      :inbound
   :num-ctrl-if    "NC-045"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "2000.00"
   :finldd-if      "00045"
   :dt-movto       "20260327"
   :participant    "00000000"
   :queue-name     "QL.REQ.00000000.99999999.01"
   :message-id     "mq-test-0045"
   :body           "<STR0045><CodMsg>STR0045</CodMsg></STR0045>"
   :received-at    "2026-03-27T00:00:00Z"})

(defn- flow-init
  []
  {:store    (aux.init/test-store)
   :base-url (aux.init/test-base-url)})

(use-fixtures :once
  (fn [f]
    (aux.init/start-test-system!)
    (f)
    (aux.init/stop-test-system!)))

(defflow str0045-respond-r1
  {:init flow-init}
  (flow "POST respond STR0045R1 — 200, store :responded, queue QR.*"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0045-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0045R1"})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? "QR.REQ.99999999.00000000.01" (sf/invoke #(:queue @capture)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "LQDADO")))))

(defflow str0045-respond-r1-then-r2-finldd-if
  {:init flow-init}
  (flow "POST R1 then R2 — R2 XML has FinlddIF (not FinlddCli), correct queue"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom {:r2-xml nil :r2-queue nil}))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0045-msg id) :id id)))
     _        (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0045R1"})))
     _        (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (swap! capture assoc :r2-xml body :r2-queue q))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0045R2"})))]
    (match? true (sf/invoke #(str/includes? (:r2-xml @capture) "<FinlddIF>00045</FinlddIF>")))
    (match? true (sf/invoke #(not (str/includes? (:r2-xml @capture) "<FinlddCli>"))))
    (match? "QR.REQ.99999999.11111111.01" (sf/invoke #(:r2-queue @capture)))))

(defflow str0045-respond-e-com-motivo
  {:init flow-init}
  (flow "POST STR0045E with MotivoRejeicao — 200, store :responded"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0045-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0045E"
                                             :params        {"MotivoRejeicao" "AC09"}})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))))

(defflow str0045-respond-e-sem-motivo
  {:init flow-init}
  (flow "POST STR0045E without MotivoRejeicao — 400, store :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0045-msg id) :id id)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0045E"}))]
    (match? 400 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))))

(defflow str0045-r2-before-r1
  {:init flow-init}
  (flow "POST STR0045R2 on pending — 422"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0045-msg id) :id id)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0045R2"}))]
    (match? 422 (sf/invoke #(:status resp)))))

(defflow str0045-mq-failure
  {:init flow-init}
  (flow "MQ failure — 500, store :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0045-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ _ _] (throw (ex-info "MQ failure" {})))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0045R1"})))]
    (match? 500 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))))
