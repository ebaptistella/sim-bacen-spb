(ns com.github.ebaptistella.frontend.subs.toast-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :toast/current-toast
 (fn [db _]
   (get-in db [:toast :current])))
