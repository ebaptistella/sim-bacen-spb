(ns com.github.ebaptistella.integration.str0044-flow-test
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
            com.github.ebaptistella.controllers.str.str0044
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.integration.aux.init :as aux.init]
            com.github.ebaptistella.wire.in.str.str0044
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

(defn- sample-msg [id]
  {:id            id
   :type          :STR0044
   :status        :pending
   :direction     :inbound
   :num-ctrl-if   "NC-044"
   :ispb-if-debtd "00000000"
   :dt-movto      "20260327"
   :participant   "00000000"
   :queue-name    "QL.REQ.00000000.99999999.01"
   :message-id    "mq-test-0044"
   :body          "<STR0044><CodMsg>STR0044</CodMsg></STR0044>"
   :received-at   "2026-03-27T00:00:00Z"})

(defn- flow-init [] {:store (aux.init/test-store) :base-url (aux.init/test-base-url)})

(use-fixtures :once (fn [f] (aux.init/start-test-system!) (f) (aux.init/stop-test-system!)))

(defflow str0044-r1-smoke
  {:init flow-init}
  (flow "POST STR0044R1 — 200, store :responded"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0044R1"})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))))

(defflow str0044-r2-returns-400-smoke
  {:init flow-init}
  (flow "POST STR0044R2 — 400 (Fluxo1 has no R2, invalid-response-type)"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-msg id) :id id)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0044R2"}))]
    (match? 400 (sf/invoke #(:status resp)))))
