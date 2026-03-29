(ns com.github.ebaptistella.frontend.subs.slb-response-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :slb-response/visible?
 (fn [db _]
   (get-in db [:slb-response :visible?] false)))

(rf/reg-sub
 :slb-response/data
 (fn [db _]
   (get-in db [:slb-response :data])))

(rf/reg-sub
 :slb-response/message-type
 (fn [db _]
   (get-in db [:slb-response :data :type])))

(rf/reg-sub
 :slb-response/fields
 (fn [db _]
   (let [data (get-in db [:slb-response :data])]
     (when data
       (dissoc data :type :body :id :direction)))))
