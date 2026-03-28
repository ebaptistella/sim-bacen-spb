(ns com.github.ebaptistella.integration.str0043-flow-test
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
            com.github.ebaptistella.controllers.str.str0043
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.integration.aux.init :as aux.init]
            com.github.ebaptistella.wire.in.str.str0043
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

(defn- sample-str0043-msg
  [id]
  {:id            id
   :type          :STR0043
   :status        :pending
   :direction     :inbound
   :num-ctrl-if   "NC-043"
   :ispb-if-debtd "00000000"
   :dt-movto      "20260327"
   :participant   "00000000"
   :queue-name    "QL.REQ.00000000.99999999.01"
   :message-id    "mq-test-0043"
   :body          "<STR0043><CodMsg>STR0043</CodMsg></STR0043>"
   :received-at   "2026-03-27T00:00:00Z"})

(defn- flow-init
  []
  {:store    (aux.init/test-store)
   :base-url (aux.init/test-base-url)})

(use-fixtures :once
  (fn [f]
    (aux.init/start-test-system!)
    (f)
    (aux.init/stop-test-system!)))

(defflow str0043-respond-r1
  {:init flow-init}
  (flow "POST STR0043R1 — 200, store :responded, XML has LIQUIDADO"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0043-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0043R1"})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? "QR.REQ.99999999.00000000.01" (sf/invoke #(:queue @capture)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "LIQUIDADO")))))

(defflow str0043-r2-returns-400
  {:init flow-init}
  (flow "POST STR0043R2 — 400 (Fluxo1 has no R2, invalid-response-type)"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0043-msg id) :id id)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0043R2"}))]
    (match? 400 (sf/invoke #(:status resp)))))

(defflow str0043-mq-failure
  {:init flow-init}
  (flow "MQ failure on STR0043R1 — 500, store :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0043-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ _ _] (throw (ex-info "MQ failure" {})))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0043R1"})))]
    (match? 500 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))))
