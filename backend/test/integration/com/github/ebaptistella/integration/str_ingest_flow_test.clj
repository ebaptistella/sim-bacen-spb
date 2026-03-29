(ns com.github.ebaptistella.integration.str-ingest-flow-test
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
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

(defn- flow-init
  []
  {:store    (aux.init/test-store)
   :base-url (aux.init/test-base-url)})

(use-fixtures :once
  (fn [f]
    (aux.init/start-test-system!)
    (f)
    (aux.init/stop-test-system!)))

(defflow str0008-ingest-201
  {:init flow-init}
  (flow "POST /api/v1/str/str0008 with valid TED fields — 201"
    [base-url (sf/get-state :base-url)
     resp     (sf/invoke #(http-post-json base-url "/api/v1/str/str0008"
                                          {:NumCtrlIF "NC-001"
                                           :ISPBIFDebtd "00000000"
                                           :ISPBIFCredtd "11111111"
                                           :VlrLanc "1000.00"
                                           :FinlddCli "10"
                                           :DtMovto "20260328"}))]
    (match? 201 (sf/invoke #(:status resp)))))

(defflow str0001-ingest-201
  {:init flow-init}
  (flow "POST /api/v1/str/str0001 with valid query fields — 201"
    [base-url (sf/get-state :base-url)
     resp     (sf/invoke #(http-post-json base-url "/api/v1/str/str0001"
                                          {:NumCtrlIF "NC-001"
                                           :ISPBIFDebtd "00000000"
                                           :DtRef "20260328"}))]
    (match? 201 (sf/invoke #(:status resp)))))

(defflow str0008-ingest-400-missing-field
  {:init flow-init}
  (flow "POST /api/v1/str/str0008 missing VlrLanc — 400"
    [base-url (sf/get-state :base-url)
     resp     (sf/invoke #(http-post-json base-url "/api/v1/str/str0008"
                                          {:NumCtrlIF "NC-001"
                                           :ISPBIFDebtd "00000000"
                                           :ISPBIFCredtd "11111111"
                                           :FinlddCli "10"
                                           :DtMovto "20260328"}))]
    (match? 400 (sf/invoke #(:status resp)))))

(defflow str0008-ingest-400-invalid-format
  {:init flow-init}
  (flow "POST /api/v1/str/str0008 invalid VlrLanc format — 400"
    [base-url (sf/get-state :base-url)
     resp     (sf/invoke #(http-post-json base-url "/api/v1/str/str0008"
                                          {:NumCtrlIF "NC-001"
                                           :ISPBIFDebtd "00000000"
                                           :ISPBIFCredtd "11111111"
                                           :VlrLanc "not-a-number"
                                           :FinlddCli "10"
                                           :DtMovto "20260328"}))]
    (match? 400 (sf/invoke #(:status resp)))))
