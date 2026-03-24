(ns com.github.ebaptistella.frontend.events.messages-events
  (:require [cljs.core.async :as async :refer [go go-loop <! timeout]]
            [com.github.ebaptistella.frontend.util.http :as http]
            [re-frame.core :as rf]))

;; Polling: go-loop faz GET /api/v1/messages a cada 4s.
;; Retry (em http.cljs): 500ms → 1s → 2s backoff em network errors/timeout.
;; Offline: após 3 falhas consecutivas, polling para e banner "Offline" aparece.
;; Reconnect: go-loop separado tenta a cada 10s; ao ter sucesso, retoma polling.

(def ^:private polling-interval-ms 4000)
(def ^:private max-retries 3)
(def ^:private backoff-ms [500 1000 2000])
(def ^:private reconnect-interval-ms 10000)

(rf/reg-event-db
 :messages/fetch-success
 (fn [db [_ {:keys [messages total]}]]
   (-> db
       (assoc-in [:messages :list] messages)
       (assoc-in [:messages :total] (or total 0))
       (assoc-in [:messages :loading?] false)
       (assoc-in [:messages :offline?] false)
       (assoc-in [:messages :retry-count] 0)
       (assoc-in [:messages :last-fetch-at] (js/Date.)))))

(rf/reg-event-db
 :messages/fetch-error
 (fn [db [_ _error]]
   (let [retry-count (inc (get-in db [:messages :retry-count] 0))
         offline?    (>= retry-count max-retries)]
     (-> db
         (assoc-in [:messages :loading?] false)
         (assoc-in [:messages :retry-count] retry-count)
         (assoc-in [:messages :offline?] offline?)
         (cond->
           offline? (assoc-in [:messages :polling-active?] false))))))

(rf/reg-event-db
 :messages/select-message
 (fn [db [_ id]]
   (assoc-in db [:messages :selected-id] id)))

(rf/reg-event-db
 :messages/deselect-message
 (fn [db _]
   (assoc-in db [:messages :selected-id] nil)))

(defn- do-fetch! []
  (let [db @(rf/subscribe [:messages/db-raw])]
    (go
      (let [limit  (get db :limit 50)
            offset (get db :offset 0)
            result (<! (http/fetch-messages limit offset))]
        (if (:ok? result)
          (rf/dispatch [:messages/fetch-success (get-in result [:body :data])])
          (rf/dispatch [:messages/fetch-error (:error result)]))))))

(rf/reg-event-fx
 :messages/fetch-initial
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:messages :loading?] true)
    :messages/do-fetch nil}))

(rf/reg-fx
 :messages/do-fetch
 (fn [_]
   (do-fetch!)))

(rf/reg-fx
 :messages/start-polling!
 (fn [_]
   (go-loop []
     (let [polling? @(rf/subscribe [:messages/polling-active?])]
       (when polling?
         (<! (timeout polling-interval-ms))
         (let [still-polling? @(rf/subscribe [:messages/polling-active?])]
           (when still-polling?
             (do-fetch!)
             (recur))))))))

(rf/reg-event-fx
 :messages/start-polling
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:messages :polling-active?] true)
    :messages/start-polling! nil}))

(rf/reg-fx
 :messages/start-reconnect!
 (fn [_]
   (go-loop []
     (let [offline? @(rf/subscribe [:messages/offline?])]
       (when offline?
         (<! (timeout reconnect-interval-ms))
         (let [result (<! (http/fetch-messages 50 0))]
           (if (:ok? result)
             (do
               (rf/dispatch [:messages/fetch-success (get-in result [:body :data])])
               (rf/dispatch [:messages/start-polling]))
             (recur))))))))

(rf/reg-event-fx
 :messages/try-reconnect
 (fn [_ _]
   {:messages/start-reconnect! nil}))
