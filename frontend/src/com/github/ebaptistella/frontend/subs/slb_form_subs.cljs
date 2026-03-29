(ns com.github.ebaptistella.frontend.subs.slb-form-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :slb-form/visible?
 (fn [db _]
   (get-in db [:slb-form :visible?] false)))

(rf/reg-sub
 :slb-form/type
 (fn [db _]
   (get-in db [:slb-form :type])))

(rf/reg-sub
 :slb-form/fields
 (fn [db _]
   (get-in db [:slb-form :fields] {})))

(rf/reg-sub
 :slb-form/submitting?
 (fn [db _]
   (get-in db [:slb-form :submitting?] false)))

(rf/reg-sub
 :slb-form/error
 (fn [db _]
   (get-in db [:slb-form :error])))
