(ns com.github.ebaptistella.frontend.subs.messages-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :messages/db-raw
 (fn [db _]
   (:messages db)))

(rf/reg-sub
 :messages/list
 (fn [db _]
   (get-in db [:messages :list])))

(rf/reg-sub
 :messages/loading?
 (fn [db _]
   (get-in db [:messages :loading?])))

(rf/reg-sub
 :messages/offline?
 (fn [db _]
   (get-in db [:messages :offline?])))

(rf/reg-sub
 :messages/selected-id
 (fn [db _]
   (get-in db [:messages :selected-id])))

(rf/reg-sub
 :messages/polling-active?
 (fn [db _]
   (get-in db [:messages :polling-active?])))

(rf/reg-sub
 :messages/last-fetch-at
 (fn [db _]
   (get-in db [:messages :last-fetch-at])))

(rf/reg-sub
 :messages/retry-count
 (fn [db _]
   (get-in db [:messages :retry-count])))

(rf/reg-sub
 :messages/selected-message
 :<- [:messages/list]
 :<- [:messages/selected-id]
 (fn [[messages selected-id] _]
   (when selected-id
     (some #(when (= (:id %) selected-id) %) messages))))
