(ns com.github.ebaptistella.integration.outbound-flow-test
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.integration.aux.init :as aux.init]
            [state-flow.api :as sf :refer [defflow flow match?]])
  (:import [java.net URI]
           [java.net.http HttpClient HttpRequest HttpResponse$BodyHandlers HttpRequest$BodyPublishers]))

;; ---- HTTP helpers ----------------------------------------------------------

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

;; ---- helpers ---------------------------------------------------------------

(defn- outbound-entries
  "Returns all store entries with :direction :outbound."
  [store]
  (->> (:messages @(:store store))
       (filter #(= (:direction %) :outbound))))

;; ---- flow helpers ----------------------------------------------------------

(defn- flow-init
  []
  {:store    (aux.init/test-store)
   :base-url (aux.init/test-base-url)})

;; ---- fixtures --------------------------------------------------------------

(use-fixtures :once
  (fn [f]
    (aux.init/start-test-system!)
    (f)
    (aux.init/stop-test-system!)))

;; ---- flows -----------------------------------------------------------------

(defflow outbound-str0017-sends-to-mq-and-stores
  {:init flow-init}
  (flow "POST /outbound STR0017 — 201, store has :outbound/:sent entry, MQ receives XML"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url "/api/v1/messages/outbound"
                                            {:type        "STR0017"
                                             :participant "00000000"})))]
    ;; HTTP response
    (match? 201
            (sf/invoke #(:status resp)))
    (match? some?
            (sf/invoke #(get-in resp [:body :data :id])))
    (match? "STR0017"
            (sf/invoke #(get-in resp [:body :data :type])))
    ;; Store
    (match? 1
            (sf/invoke #(count (outbound-entries store))))
    (match? :outbound
            (sf/invoke #(-> (outbound-entries store) first :direction)))
    (match? :sent
            (sf/invoke #(-> (outbound-entries store) first :status)))
    (match? "STR0017"
            (sf/invoke #(-> (outbound-entries store) first :type)))
    ;; MQ queue — SIMULATOR_ISPB defaults to "99999999" in test env
    (match? "QR.REQ.99999999.00000000.01"
            (sf/invoke #(:queue @capture)))
    ;; XML content
    (match? true
            (sf/invoke #(str/includes? (:xml @capture) "<CodMsg>STR0017</CodMsg>")))))

(defflow outbound-invalid-type-returns-400
  {:init flow-init}
  (flow "POST /outbound with unknown type — 400, no outbound store entry"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     before   (sf/invoke #(count (outbound-entries store)))
     resp     (sf/invoke #(http-post-json base-url "/api/v1/messages/outbound"
                                          {:type        "STR9999"
                                           :participant "00000000"}))
     after    (sf/invoke #(count (outbound-entries store)))]
    (match? 400
            (sf/invoke #(:status resp)))
    (match? before
            (sf/invoke #(identity after)))))

(defflow outbound-invalid-participant-returns-400
  {:init flow-init}
  (flow "POST /outbound with non-numeric participant — 400"
    [base-url (sf/get-state :base-url)
     resp     (sf/invoke #(http-post-json base-url "/api/v1/messages/outbound"
                                          {:type        "STR0017"
                                           :participant "ABCDE123"}))]
    (match? 400
            (sf/invoke #(:status resp)))))

(defflow outbound-mq-failure-returns-500-no-store-entry
  {:init flow-init}
  (flow "POST /outbound STR0015 with MQ failure — 500, no outbound entry in store"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     before   (sf/invoke #(count (outbound-entries store)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ _ _] (throw (ex-info "MQ unavailable" {})))]
                            (http-post-json base-url "/api/v1/messages/outbound"
                                            {:type        "STR0015"
                                             :participant "00000000"})))
     after    (sf/invoke #(count (outbound-entries store)))]
    (match? 500
            (sf/invoke #(:status resp)))
    (match? before
            (sf/invoke #(identity after)))))
