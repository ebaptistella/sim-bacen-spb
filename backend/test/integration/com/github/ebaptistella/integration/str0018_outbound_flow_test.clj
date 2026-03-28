(ns com.github.ebaptistella.integration.str0018-outbound-flow-test
  "Smoke tests for CA3: new broadcast types STR0018/19/30/42/50 via POST /api/v1/messages/outbound."
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.integration.aux.init :as aux.init]
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

(defn- outbound-entries [store]
  (->> (:messages @(:store store))
       (filter #(= (:direction %) :outbound))))

(defn- flow-init [] {:store (aux.init/test-store) :base-url (aux.init/test-base-url)})

(use-fixtures :once (fn [f] (aux.init/start-test-system!) (f) (aux.init/stop-test-system!)))

(defflow str0018-outbound-smoke
  {:init flow-init}
  (flow "POST /outbound STR0018 — 201, MQ receives STR0018 XML"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url "/api/v1/messages/outbound"
                                            {:type        "STR0018"
                                             :participant "00000000"})))]
    (match? 201 (sf/invoke #(:status resp)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "<CodMsg>STR0018</CodMsg>")))))

(defflow str0019-outbound-smoke
  {:init flow-init}
  (flow "POST /outbound STR0019 — 201, MQ receives STR0019 XML"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url "/api/v1/messages/outbound"
                                            {:type        "STR0019"
                                             :participant "00000000"})))]
    (match? 201 (sf/invoke #(:status resp)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "<CodMsg>STR0019</CodMsg>")))))

(defflow str0030-outbound-smoke
  {:init flow-init}
  (flow "POST /outbound STR0030 — 201, MQ receives STR0030 XML"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url "/api/v1/messages/outbound"
                                            {:type        "STR0030"
                                             :participant "00000000"})))]
    (match? 201 (sf/invoke #(:status resp)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "<CodMsg>STR0030</CodMsg>")))))

(defflow str0042-outbound-smoke
  {:init flow-init}
  (flow "POST /outbound STR0042 — 201, MQ receives STR0042 XML"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url "/api/v1/messages/outbound"
                                            {:type        "STR0042"
                                             :participant "00000000"})))]
    (match? 201 (sf/invoke #(:status resp)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "<CodMsg>STR0042</CodMsg>")))))

(defflow str0050-outbound-smoke
  {:init flow-init}
  (flow "POST /outbound STR0050 — 201, MQ receives STR0050 XML"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                        (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url "/api/v1/messages/outbound"
                                            {:type        "STR0050"
                                             :participant "00000000"})))]
    (match? 201 (sf/invoke #(:status resp)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "<CodMsg>STR0050</CodMsg>")))))

(defflow outbound-new-invalid-type-returns-400-smoke
  {:init flow-init}
  (flow "POST /outbound with type not in new set — 400"
    [base-url (sf/get-state :base-url)
     resp     (sf/invoke #(http-post-json base-url "/api/v1/messages/outbound"
                                          {:type        "STR9999"
                                           :participant "00000000"}))]
    (match? 400 (sf/invoke #(:status resp)))))
