(ns com.github.ebaptistella.integration.slb-flow-test
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

(defflow slb0002-request-stores-and-sends-to-mq
  {:init flow-init}
  (flow "POST /api/v1/slb/slb0002 — 201, stores message, publishes to MQ"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url "/api/v1/slb/slb0002"
                                            {:NumCtrlPart "12345"
                                             :ISPBPart    "00000000"
                                             :VlrLanc     1000.00
                                             :DtMovto     "20260329"
                                             :Hist        "Teste SLB0002"})))]
    ;; HTTP response
    (match? 201
            (sf/invoke #(:status resp)))
    (match? some?
            (sf/invoke #(get-in resp [:body :data])))
    (match? "SLB0002"
            (sf/invoke #(get-in resp [:body :data :message-type])))
    (match? "injected"
            (sf/invoke #(get-in resp [:body :data :status])))
    ;; MQ was called
    (match? string?
            (sf/invoke #(-> @capture :queue)))
    (match? string?
            (sf/invoke #(-> @capture :xml)))))

(defflow slb0005-request-with-specific-fields
  {:init flow-init}
  (flow "POST /api/v1/slb/slb0005 — accepts all required fields"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     resp     (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _])]
                            (http-post-json base-url "/api/v1/slb/slb0005"
                                            {:NumCtrlSTR   "67890"
                                             :ISPBPart     "00000000"
                                             :VlrLanc      2000.00
                                             :FIndddSLB    "99"
                                             :NumCtrlSLB   "11111"
                                             :DtVenc       "20260430"
                                             :Hist         "Teste SLB0005"})))]
    (match? 201
            (sf/invoke #(:status resp)))
    (match? "SLB0005"
            (sf/invoke #(get-in resp [:body :data :message-type])))))

(defflow slb0006-query-response
  {:init flow-init}
  (flow "POST /api/v1/slb/slb0006 — consulta simples"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     resp     (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _])]
                            (http-post-json base-url "/api/v1/slb/slb0006"
                                            {:NumCtrlPart "54321"
                                             :ISPBPart    "00000000"})))]
    (match? 201
            (sf/invoke #(:status resp)))
    (match? "SLB0006"
            (sf/invoke #(get-in resp [:body :data :message-type])))))

(defflow slb0007-debit-request
  {:init flow-init}
  (flow "POST /api/v1/slb/slb0007 — débito específico"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     resp     (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _])]
                            (http-post-json base-url "/api/v1/slb/slb0007"
                                            {:NumCtrlPart "11111"
                                             :ISPBPart    "00000000"
                                             :VlrLanc     500.00})))]
    (match? 201
            (sf/invoke #(:status resp)))
    (match? "SLB0007"
            (sf/invoke #(get-in resp [:body :data :message-type])))))

(defflow slb0008-generic-debit
  {:init flow-init}
  (flow "POST /api/v1/slb/slb0008 — débito genérico"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     resp     (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _])]
                            (http-post-json base-url "/api/v1/slb/slb0008"
                                            {:NumCtrlSLB "22222"
                                             :ISPBPart   "00000000"
                                             :VlrLanc    750.50})))]
    (match? 201
            (sf/invoke #(:status resp)))
    (match? "SLB0008"
            (sf/invoke #(get-in resp [:body :data :message-type])))))

(defflow slb-invalid-fields-returns-400
  {:init flow-init}
  (flow "POST /api/v1/slb/slb0002 with missing required field — 400"
    [base-url (sf/get-state :base-url)
     resp     (sf/invoke #(http-post-json base-url "/api/v1/slb/slb0002"
                                          {:NumCtrlPart "12345"
                                           :ISPBPart    "00000000"
                                           ;; missing VlrLanc
                                          }))]
    (match? 400
            (sf/invoke #(:status resp)))))

(defflow slb-unknown-message-type-returns-404
  {:init flow-init}
  (flow "POST /api/v1/slb/slb9999 with unknown type — 404 (endpoint not found)"
    [base-url (sf/get-state :base-url)
     resp     (sf/invoke #(http-post-json base-url "/api/v1/slb/slb9999"
                                          {:ISPBPart "00000000"}))]
    (match? 404
            (sf/invoke #(:status resp)))))
