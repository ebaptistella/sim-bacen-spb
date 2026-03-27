(ns com.github.ebaptistella.integration.str0048-flow-test
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.test :refer [use-fixtures]]
            com.github.ebaptistella.controllers.str.str0048
            [com.github.ebaptistella.infrastructure.mq.producer :as mq.producer]
            [com.github.ebaptistella.infrastructure.store.messages :as store.messages]
            [com.github.ebaptistella.integration.aux.init :as aux.init]
            com.github.ebaptistella.wire.in.str.str0048
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

(defn- sample-str0048-msg
  [id]
  {:id               id
   :type             :STR0048
   :status           :pending
   :direction        :inbound
   :num-ctrl-if      "NC-048"
   :num-ctrl-str-or  "NC-047"
   :ispb-if-debtd    "00000000"
   :ispb-if-credtd   "11111111"
   :vlr-lanc         "2000.00"
   :cod-dev-transf   "MD06"
   :dt-movto         "20240115"
   :ispb-if-devedora "22222222"
   :participant      "00000000"
   :queue-name       "QL.REQ.00000000.99999999.01"
   :message-id       "mq-test-0048"
   :body             "<STR0048><CodMsg>STR0048</CodMsg></STR0048>"
   :received-at      "2025-01-01T00:00:00Z"})

(defn- flow-init
  []
  {:store    (aux.init/test-store)
   :base-url (aux.init/test-base-url)})

(use-fixtures :once
  (fn [f]
    (aux.init/start-test-system!)
    (f)
    (aux.init/stop-test-system!)))

(defflow str0048-respond-r1
  {:init flow-init}
  (flow "POST respond STR0048R1 — 200, store :responded, MQ QR.* receives LQDADO XML"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ q body] (reset! capture {:queue q :xml body}))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R1"})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? :STR0048R1 (sf/invoke #(-> (store.messages/find-by-id store id) :responses first :type)))
    (match? some? (sf/invoke #(-> (store.messages/find-by-id store id) :responses first :sent-at)))
    (match? "QR.REQ.99999999.00000000.01" (sf/invoke #(:queue @capture)))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "STR0048R1")))
    (match? true (sf/invoke #(str/includes? (:xml @capture) "LQDADO")))))

(defflow str0048-r1-then-r2-same-num-ctrl-str
  {:init flow-init}
  (flow "POST R1 then R2 — R2 XML has same NumCtrlSTR as R1, R2 goes to creditor queue"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom {:r1-xml nil :r2-xml nil :r2-queue nil}))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     _        (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ _ body] (swap! capture assoc :r1-xml body))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R1"})))
     _        (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ q body] (swap! capture assoc :r2-xml body :r2-queue q))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R2"})))]
    (match? some? (sf/invoke #(re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" (:r1-xml @capture))))
    (match? some? (sf/invoke #(re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" (:r2-xml @capture))))
    (match? true  (sf/invoke #(let [r1-ncs (second (re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" (:r1-xml @capture)))
                                    r2-ncs (second (re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" (:r2-xml @capture)))]
                                (= r1-ncs r2-ncs))))
    (match? "QR.REQ.99999999.11111111.01" (sf/invoke #(:r2-queue @capture)))))

(defflow str0048-r2-then-r3-to-devedora-queue
  {:init flow-init}
  (flow "POST R1→R2→R3 — R3 goes to ispb-if-devedora queue"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom {:r3-xml nil :r3-queue nil}))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     _        (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R1"})))
     _        (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R2"})))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ q body] (reset! capture {:r3-queue q :r3-xml body}))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R3"})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? "QR.REQ.99999999.22222222.01" (sf/invoke #(:r3-queue @capture)))
    (match? true (sf/invoke #(str/includes? (:r3-xml @capture) "STR0048R3")))
    (match? true (sf/invoke #(str/includes? (:r3-xml @capture) "22222222")))))

(defflow str0048-r3-via-params-fallback
  {:init flow-init}
  (flow "POST R3 with ISPBIFDevedora in params — uses params when msg has no ispb-if-devedora"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store
                                                 (-> (sample-str0048-msg id)
                                                     (dissoc :ispb-if-devedora))))
     _        (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R1"})))
     _        (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R2"})))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ q _] (reset! capture q))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R3"
                                             :params        {"ISPBIFDevedora" "33333333"}})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? "QR.REQ.99999999.33333333.01" (sf/invoke #(identity @capture)))))

(defflow str0048-r3-before-r2
  {:init flow-init}
  (flow "POST STR0048R3 before R2 — 422"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     _        (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R1"})))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0048R3"}))]
    (match? 422 (sf/invoke #(:status resp)))))

(defflow str0048-r2-before-r1
  {:init flow-init}
  (flow "POST STR0048R2 before R1 — 422, status remains :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0048R2"}))]
    (match? 422 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? nil (sf/invoke #(-> (store.messages/find-by-id store id) :responses)))))

(defflow str0048-respond-e-com-motivo
  {:init flow-init}
  (flow "POST STR0048E with MotivoRejeicao — 200, store :responded"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     capture  (sf/invoke #(atom nil))
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ _ body] (reset! capture body))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048E"
                                             :params        {"MotivoRejeicao" "AC09"}})))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? :responded (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? true (sf/invoke #(str/includes? @capture "AC09")))))

(defflow str0048-respond-e-sem-motivo
  {:init flow-init}
  (flow "POST STR0048E without MotivoRejeicao — 400, status remains :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0048E"}))]
    (match? 400 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? nil (sf/invoke #(-> (store.messages/find-by-id store id) :responses)))))

(defflow str0048-respond-409
  {:init flow-init}
  (flow "POST respond already-responded STR0048 — 409, store unchanged"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store
                                                 (-> (sample-str0048-msg id)
                                                     (assoc :status :responded
                                                            :responses [{:type    :STR0048R1
                                                                         :body    "<x/>"
                                                                         :sent-at "t"}]))))
     before   (sf/invoke #(store.messages/find-by-id store id))
     resp     (sf/invoke #(http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                          {:response-type "STR0048R1"}))
     after    (sf/invoke #(store.messages/find-by-id store id))]
    (match? 409 (sf/invoke #(:status resp)))
    (match? before (sf/invoke #(identity after)))))

(defflow str0048-mq-failure
  {:init flow-init}
  (flow "MQ send failure on STR0048R1 — 500, store remains :pending"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     resp     (sf/invoke #(with-redefs [mq.producer/send-message!
                                         (fn [_ _ _] (throw (ex-info "MQ failure" {})))]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R1"})))]
    (match? 500 (sf/invoke #(:status resp)))
    (match? :pending (sf/invoke #(-> (store.messages/find-by-id store id) :status)))
    (match? nil (sf/invoke #(-> (store.messages/find-by-id store id) :responses)))))

(defflow str0048-get-available-responses-pending
  {:init flow-init}
  (flow "GET /api/v1/messages/:id — STR0048 pending returns R1, R2, E"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     resp     (sf/invoke #(http-get-json base-url (str "/api/v1/messages/" id)))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? ["STR0048R1" "STR0048R2" "STR0048E"]
            (sf/invoke #(-> resp :body :data :available-responses)))))

(defflow str0048-get-available-responses-after-r1
  {:init flow-init}
  (flow "GET /api/v1/messages/:id — STR0048 after R1 returns only R2"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     _        (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R1"})))
     resp     (sf/invoke #(http-get-json base-url (str "/api/v1/messages/" id)))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? ["STR0048R2"]
            (sf/invoke #(-> resp :body :data :available-responses)))))

(defflow str0048-get-available-responses-after-r2
  {:init flow-init}
  (flow "GET /api/v1/messages/:id — STR0048 after R2 returns only R3"
    [store    (sf/get-state :store)
     base-url (sf/get-state :base-url)
     id       (sf/invoke #(str (random-uuid)))
     _        (sf/invoke #(store.messages/save! store (assoc (sample-str0048-msg id) :id id)))
     _        (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R1"})))
     _        (sf/invoke #(with-redefs [mq.producer/send-message! (fn [_ _ _] nil)]
                            (http-post-json base-url (str "/api/v1/messages/" id "/respond")
                                            {:response-type "STR0048R2"})))
     resp     (sf/invoke #(http-get-json base-url (str "/api/v1/messages/" id)))]
    (match? 200 (sf/invoke #(:status resp)))
    (match? ["STR0048R3"]
            (sf/invoke #(-> resp :body :data :available-responses)))))
