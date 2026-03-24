(ns com.github.ebaptistella.frontend.events.respond-events
  (:require [cljs.core.async :refer [go <!]]
            [com.github.ebaptistella.frontend.util.http :as http]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :respond/open-modal
 (fn [db _]
   (-> db
       (assoc-in [:respond :modal-visible?] true)
       (assoc-in [:respond :confirmation-visible?] false)
       (assoc-in [:respond :response-type] nil)
       (assoc-in [:respond :motivo] nil)
       (assoc-in [:respond :error] nil)
       (assoc-in [:respond :submitting?] false))))

(rf/reg-event-db
 :respond/close-modal
 (fn [db _]
   (-> db
       (assoc-in [:respond :modal-visible?] false)
       (assoc-in [:respond :confirmation-visible?] false)
       (assoc-in [:respond :response-type] nil)
       (assoc-in [:respond :motivo] nil)
       (assoc-in [:respond :error] nil)
       (assoc-in [:respond :submitting?] false))))

(rf/reg-event-db
 :respond/close-all
 (fn [db _]
   (-> db
       (assoc-in [:respond :modal-visible?] false)
       (assoc-in [:respond :confirmation-visible?] false)
       (assoc-in [:respond :response-type] nil)
       (assoc-in [:respond :motivo] nil)
       (assoc-in [:respond :error] nil)
       (assoc-in [:respond :submitting?] false))))

(rf/reg-event-db
 :respond/set-response-type
 (fn [db [_ response-type]]
   (-> db
       (assoc-in [:respond :response-type] response-type)
       (assoc-in [:respond :motivo] nil))))

(rf/reg-event-db
 :respond/set-motivo
 (fn [db [_ motivo]]
   (assoc-in db [:respond :motivo] motivo)))

(rf/reg-event-db
 :respond/show-confirmation
 (fn [db _]
   (-> db
       (assoc-in [:respond :modal-visible?] false)
       (assoc-in [:respond :confirmation-visible?] true)
       (assoc-in [:respond :error] nil))))

(rf/reg-event-db
 :respond/back-to-modal
 (fn [db _]
   (-> db
       (assoc-in [:respond :confirmation-visible?] false)
       (assoc-in [:respond :modal-visible?] true)
       (assoc-in [:respond :error] nil))))

(rf/reg-event-db
 :respond/submit-success
 (fn [db _]
   (-> db
       (assoc-in [:respond :modal-visible?] false)
       (assoc-in [:respond :confirmation-visible?] false)
       (assoc-in [:respond :submitting?] false)
       (assoc-in [:respond :error] nil)
       (assoc-in [:respond :response-type] nil)
       (assoc-in [:respond :motivo] nil))))

(rf/reg-event-db
 :respond/submit-error
 (fn [db [_ error]]
   (-> db
       (assoc-in [:respond :submitting?] false)
       (assoc-in [:respond :error] error))))

(defn- build-response-type [msg-type response-type]
  (case response-type
    :accept   (str msg-type "R1")
    :reject   (str msg-type "E")
    :send-r2  (str msg-type "R2")))

(rf/reg-event-fx
 :respond/submit
 (fn [{:keys [db]} _]
   (let [msg-id        (get-in db [:messages :selected-id])
         response-type (get-in db [:respond :response-type])
         motivo        (get-in db [:respond :motivo])
         msg           (some #(when (= (:id %) msg-id) %)
                             (get-in db [:messages :list]))
         body          (cond-> {:response-type (build-response-type (:type msg) response-type)}
                         (= response-type :reject)   (assoc :params {:motivo-rejeicao motivo})
                         (= response-type :accept)   (assoc :params {})
                         (= response-type :send-r2)  (assoc :params {}))]
     {:db (assoc-in db [:respond :submitting?] true)
      :respond/do-submit {:msg-id msg-id :body body}})))

(rf/reg-fx
 :respond/do-submit
 (fn [{:keys [msg-id body]}]
   (go
     (let [result (<! (http/post-response msg-id body))]
       (if (:ok? result)
         (do
           (rf/dispatch [:respond/submit-success])
           (rf/dispatch [:toast/show {:message "Resposta enviada com sucesso" :type :success}])
           (rf/dispatch [:messages/fetch-initial]))
         (rf/dispatch [:respond/submit-error {:status (:status result)}])))))

(rf/reg-event-fx
 :respond/open-r2-confirm
 (fn [{:keys [db]} _]
   (-> db
       (assoc-in [:respond :response-type] :send-r2)
       (assoc-in [:respond :motivo] nil)
       (assoc-in [:respond :error] nil)
       (assoc-in [:respond :submitting?] false)
       (assoc-in [:respond :modal-visible?] false)
       (assoc-in [:respond :confirmation-visible?] true)
       (->> (assoc {} :db))))))
