(ns com.github.ebaptistella.frontend.subs.respond-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :respond/modal-visible?
 (fn [db _]
   (get-in db [:respond :modal-visible?])))

(rf/reg-sub
 :respond/confirmation-visible?
 (fn [db _]
   (get-in db [:respond :confirmation-visible?])))

(rf/reg-sub
 :respond/response-type
 (fn [db _]
   (get-in db [:respond :response-type])))

(rf/reg-sub
 :respond/motivo
 (fn [db _]
   (get-in db [:respond :motivo])))

(rf/reg-sub
 :respond/submitting?
 (fn [db _]
   (get-in db [:respond :submitting?])))

(rf/reg-sub
 :respond/error
 (fn [db _]
   (get-in db [:respond :error])))
