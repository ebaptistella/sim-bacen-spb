(ns com.github.ebaptistella.frontend.subs.router-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :router/current-page
 (fn [db _]
   (get-in db [:router :page] :messages)))

(rf/reg-sub
 :router/current-route
 (fn [db _]
   (:router db)))

(rf/reg-sub
 :router/route-id
 (fn [db _]
   (get-in db [:router :id])))
