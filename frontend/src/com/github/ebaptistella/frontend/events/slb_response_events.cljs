(ns com.github.ebaptistella.frontend.events.slb-response-events
  (:require [re-frame.core :as rf]))

;; Event handlers for SLB response display

(rf/reg-event-db
 :slb-response/show-response
 (fn [db [_ response-msg]]
   (assoc-in db [:slb-response :visible?] true)
   (assoc-in db [:slb-response :data] response-msg)))

(rf/reg-event-db
 :slb-response/hide-response
 (fn [db _]
   (assoc-in db [:slb-response :visible?] false)))

(rf/reg-event-db
 :slb-response/clear
 (fn [db _]
   (assoc db :slb-response {:visible? false :data nil})))
