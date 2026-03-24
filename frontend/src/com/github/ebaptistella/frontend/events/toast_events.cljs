(ns com.github.ebaptistella.frontend.events.toast-events
  (:require [re-frame.core :as rf]))

(def ^:private toast-duration-ms 5000)

(rf/reg-event-fx
 :toast/show
 (fn [{:keys [db]} [_ toast-data]]
   {:db (assoc-in db [:toast :current] toast-data)
    :toast/auto-dismiss toast-duration-ms}))

(rf/reg-event-db
 :toast/dismiss
 (fn [db _]
   (assoc-in db [:toast :current] nil)))

(rf/reg-fx
 :toast/auto-dismiss
 (fn [duration]
   (js/setTimeout #(rf/dispatch [:toast/dismiss]) duration)))
