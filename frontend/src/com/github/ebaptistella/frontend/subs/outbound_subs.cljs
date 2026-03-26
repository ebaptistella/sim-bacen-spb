(ns com.github.ebaptistella.frontend.subs.outbound-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub :outbound/modal-visible? (fn [db _] (get-in db [:outbound :modal-visible?])))
(rf/reg-sub :outbound/type           (fn [db _] (get-in db [:outbound :type])))
(rf/reg-sub :outbound/participant    (fn [db _] (get-in db [:outbound :participant])))
(rf/reg-sub :outbound/params         (fn [db _] (get-in db [:outbound :params])))
(rf/reg-sub :outbound/submitting?    (fn [db _] (get-in db [:outbound :submitting?])))
(rf/reg-sub :outbound/error          (fn [db _] (get-in db [:outbound :error])))
