(ns com.github.ebaptistella.ui.core
  "Main entry point. All state in app-state; setters update it; app derefs once so any change re-renders."
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [com.github.ebaptistella.ui.models :as models]
            [com.github.ebaptistella.ui.logic :as logic]
            [com.github.ebaptistella.ui.http-client :as http]
            [com.github.ebaptistella.ui.adapters :as adapters]
            [com.github.ebaptistella.ui.components.transaction-form :as transaction-form]
            [com.github.ebaptistella.ui.components.transaction-history :as transaction-history]))

(defonce app-state (r/atom models/initial-state))

(def limit 20)

;; ---- State setters ----

(defn set-form-field!
  [field value]
  (swap! app-state update-in [:form field] (constantly value)))

(defn set-form-loading!
  [loading]
  (swap! app-state assoc :form-loading? loading :form-message nil :form-message-type nil))

(defn set-form-status!
  [message type]
  (swap! app-state assoc
         :form-loading?     false
         :form-message      message
         :form-message-type type))

(defn reset-form!
  []
  (swap! app-state assoc
         :form              {:tipo "" :valor "" :origem "" :destino ""}
         :form-message      nil
         :form-message-type nil
         :form-loading?     false))

(defn set-transactions-loading!
  [loading]
  (swap! app-state assoc :transactions-loading? loading :transactions-error nil))

(defn set-transactions!
  [items append?]
  (if append?
    (swap! app-state (fn [s]
                       (-> s
                           (update :transactions into items)
                           (assoc :transactions-offset (+ (:transactions-offset s) (count items))
                                  :transactions-loading? false
                                  :transactions-has-more (>= (count items) limit)))))
    (swap! app-state assoc
           :transactions        items
           :transactions-offset (count items)
           :transactions-loading? false
           :transactions-has-more (>= (count items) limit))))

(defn set-transactions-error!
  [error-message]
  (swap! app-state assoc
         :transactions-loading? false
         :transactions-error    error-message))

;; ---- Fetch transactions ----

(defn fetch-transactions!
  ([]
   (fetch-transactions! false))
  ([append?]
   (when-not (models/transactions-loading? @app-state)
     (when-not append?
       (set-transactions-loading! true)
       (swap! app-state assoc :transactions [] :transactions-offset 0))
     (let [offset (if append? (models/transactions-offset @app-state) 0)]
       (-> (http/get-raw "/api/v1/transactions" {:limit limit :offset offset})
           (.then (fn [res]
                    (let [res-clj (if (map? res) res (js->clj res :keywordize-keys true))
                          body    (or (:data res-clj) (get res-clj "data"))
                          ok?     (or (:ok res-clj) (get res-clj "ok"))
                          data    (if (map? body) body (js->clj (or body {}) :keywordize-keys true))
                          items   (or (adapters/wire->transaction-list data) [])]
                      (if ok?
                        (set-transactions! items append?)
                        (set-transactions-error!
                         (or (adapters/wire->error-message data)
                             (str "Erro " (or (:status res-clj) "desconhecido"))))))))
           (.catch (fn [_]
                     (set-transactions-error! "Erro de rede. Verifique sua conexão e tente novamente.")))
           (.finally (fn []
                       (swap! app-state assoc :transactions-loading? false))))))))

;; ---- Submit transaction ----

(defn handle-submit-success
  [data]
  (let [data-clj (if (map? data) data (js->clj data :keywordize-keys true))
        m        (adapters/wire->submit-response data-clj)
        msg      (logic/success-message (or (:id m) (get data-clj "id") (get data-clj :id)))]
    (reset-form!)
    (swap! app-state assoc :form-message msg :form-message-type :success)
    (fetch-transactions!)))

(defn handle-submit-error
  [data status]
  (set-form-status!
   (or (adapters/wire->error-message (if (map? data) data (js->clj data :keywordize-keys true)))
       (str "Erro " status))
   :error))

(defn submit-transaction!
  [e]
  (.preventDefault e)
  (let [form    (models/form-data @app-state)
        payload {:tipo    (get form :tipo "")
                 :valor   (get form :valor "")
                 :origem  (get form :origem "")
                 :destino (get form :destino "")}]
    (when (logic/form-fields-valid? payload)
      (set-form-loading! true)
      (-> (http/post-raw "/api/v1/transactions" payload)
          (.then (fn [res]
                   (let [res-clj (if (map? res) res (js->clj res :keywordize-keys true))
                         body    (or (:data res-clj) (get res-clj "data") (when (get res-clj :id) res-clj))
                         status  (or (:status res-clj) (get res-clj "status"))
                         ok?     (or (:ok res-clj) (get res-clj "ok")
                                     (and status (>= status 200) (< status 300)))]
                     (if ok?
                       (handle-submit-success (or body {}))
                       (handle-submit-error (or body {}) status)))))
          (.catch (fn [_]
                    (set-form-status! "Erro de rede. Verifique sua conexão e tente novamente." :error)))
          (.finally (fn []
                      (set-form-loading! false)))))))

;; ---- Handlers ----

(defn handle-form-change
  [field value]
  (set-form-field! field value))

(defn handle-load-more
  []
  (fetch-transactions! true))

;; ---- App component ----

(defn app
  []
  (let [state        @app-state
        form-data    (models/form-data state)
        loading?     (models/form-loading? state)
        message      (models/form-message state)
        message-type (models/form-message-type state)
        txs          (models/transactions state)
        tx-loading?  (models/transactions-loading? state)
        tx-error     (models/transactions-error state)
        tx-has-more  (models/transactions-has-more? state)]
    [:div.max-w-6xl.mx-auto.bg-white.rounded-xl.shadow-2xl.overflow-hidden
     [:header.bg-gradient-to-r.from-indigo-500.to-purple-600.text-white.p-8.text-center
      [:h1.text-4xl.md:text-5xl.mb-2.5 "Simulador BACEN"]
      [:p.text-lg.opacity-90 "Sistema de Pagamentos Brasileiro"]]
     [:main.p-8
      [transaction-form/transaction-form
       {:form-data      form-data
        :on-form-change handle-form-change
        :loading?       loading?
        :message        message
        :message-type   message-type
        :on-submit      submit-transaction!
        :on-reset       reset-form!}]
      [transaction-history/transaction-history
       {:error        tx-error
        :loading?     tx-loading?
        :transactions txs
        :has-more?    tx-has-more
        :on-retry     #(fetch-transactions! false)
        :on-load-more handle-load-more}]]]))

(defn mount-root
  []
  (when-let [app-el (.getElementById js/document "app")]
    (rdom/render [app] app-el)
    (fetch-transactions!)))

(defn ^:export init
  []
  (mount-root))

(init)
