(ns com.github.ebaptistella.frontend.events.slb-form-events
  (:require [cljs.core.async :refer [go <!]]
            [clojure.string :as str]
            [com.github.ebaptistella.frontend.util.http :as http]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :slb-form/open
 (fn [db _]
   (-> db
       (assoc-in [:slb-form :visible?] true)
       (assoc-in [:slb-form :type] nil)
       (assoc-in [:slb-form :fields] {})
       (assoc-in [:slb-form :submitting?] false)
       (assoc-in [:slb-form :error] nil))))

(rf/reg-event-db
 :slb-form/close
 (fn [db _]
   (assoc-in db [:slb-form :visible?] false)))

(rf/reg-event-db
 :slb-form/set-type
 (fn [db [_ msg-type]]
   (-> db
       (assoc-in [:slb-form :type] msg-type)
       (assoc-in [:slb-form :fields] {}))))

(rf/reg-event-db
 :slb-form/set-field
 (fn [db [_ field value]]
   (assoc-in db [:slb-form :fields field] value)))

(rf/reg-event-db
 :slb-form/submit-success
 (fn [db _]
   (-> db
       (assoc-in [:slb-form :visible?] false)
       (assoc-in [:slb-form :submitting?] false)
       (assoc-in [:slb-form :error] nil))))

(rf/reg-event-db
 :slb-form/submit-error
 (fn [db [_ error]]
   (-> db
       (assoc-in [:slb-form :submitting?] false)
       (assoc-in [:slb-form :error] error))))

(rf/reg-event-fx
 :slb-form/submit
 (fn [{:keys [db]} _]
   (let [msg-type (get-in db [:slb-form :type])
         fields   (get-in db [:slb-form :fields])]
     {:db (assoc-in db [:slb-form :submitting?] true)
      :slb-form/do-submit {:type msg-type :fields fields}})))

(rf/reg-fx
 :slb-form/do-submit
 (fn [{:keys [type fields]}]
   (go
     (let [endpoint (str "/api/v1/slb/" (str/lower-case type))
           result   (<! (http/post-json endpoint fields))]
       (if (:ok? result)
         (do
           (rf/dispatch [:slb-form/submit-success])
           (rf/dispatch [:toast/show {:message (str "Mensagem " type " enviada com sucesso") :type :success}])
           (rf/dispatch [:messages/fetch-initial]))
         (let [status (:status result)
               error  (cond
                        (= status 400) "Campos inválidos ou faltando"
                        (= status 500) "Falha ao enviar na fila MQ"
                        :else          "Erro ao enviar mensagem")]
           (rf/dispatch [:slb-form/submit-error {:status status :message error}])))))))
