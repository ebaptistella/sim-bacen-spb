(ns com.github.ebaptistella.frontend.events.outbound-events
  (:require [cljs.core.async :refer [go <!]]
            [com.github.ebaptistella.frontend.util.http :as http]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :outbound/open-modal
 (fn [db _]
   (-> db
       (assoc-in [:outbound :modal-visible?] true)
       (assoc-in [:outbound :type] nil)
       (assoc-in [:outbound :participant] "")
       (assoc-in [:outbound :params] {})
       (assoc-in [:outbound :submitting?] false)
       (assoc-in [:outbound :error] nil))))

(rf/reg-event-db
 :outbound/close-modal
 (fn [db _]
   (assoc-in db [:outbound :modal-visible?] false)))

(rf/reg-event-db
 :outbound/set-type
 (fn [db [_ msg-type]]
   (-> db
       (assoc-in [:outbound :type] msg-type)
       (assoc-in [:outbound :params] {}))))

(rf/reg-event-db
 :outbound/set-participant
 (fn [db [_ participant]]
   (assoc-in db [:outbound :participant] participant)))

(rf/reg-event-db
 :outbound/set-param
 (fn [db [_ k v]]
   (assoc-in db [:outbound :params k] v)))

(rf/reg-event-db
 :outbound/submit-success
 (fn [db _]
   (-> db
       (assoc-in [:outbound :modal-visible?] false)
       (assoc-in [:outbound :submitting?] false)
       (assoc-in [:outbound :error] nil))))

(rf/reg-event-db
 :outbound/submit-error
 (fn [db [_ error]]
   (-> db
       (assoc-in [:outbound :submitting?] false)
       (assoc-in [:outbound :error] error))))

(rf/reg-event-fx
 :outbound/submit
 (fn [{:keys [db]} _]
   (let [msg-type    (get-in db [:outbound :type])
         participant (get-in db [:outbound :participant])
         params      (get-in db [:outbound :params])]
     {:db (assoc-in db [:outbound :submitting?] true)
      :outbound/do-submit {:type msg-type :participant participant :params params}})))

(rf/reg-fx
 :outbound/do-submit
 (fn [{:keys [type participant params]}]
   (go
     (let [is-slb?     (clojure.string/starts-with? type "SLB")
           body        (if is-slb?
                         {:ISPBPart participant}
                         {:type type :participant participant :params params})
           endpoint    (if is-slb?
                         (str "/api/v1/slb/" (clojure.string/lower-case type))
                         "/api/v1/messages/outbound")
           result      (<! (http/post-json endpoint body))]
       (if (:ok? result)
         (do
           (rf/dispatch [:outbound/submit-success])
           (rf/dispatch [:toast/show {:message (str "Mensagem " type " enviada com sucesso") :type :success}])
           (rf/dispatch [:messages/fetch-initial]))
         (let [status (:status result)
               error  (cond
                        (= status 400) "Tipo ou parâmetros inválidos"
                        (= status 500) "Falha ao enviar na fila MQ"
                        :else          "Erro ao enviar mensagem")]
           (rf/dispatch [:outbound/submit-error {:status status :message error}])))))))
