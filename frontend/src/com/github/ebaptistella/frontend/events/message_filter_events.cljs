(ns com.github.ebaptistella.frontend.events.message-filter-events
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 :message-filter/set-type
 (fn [db [_ filter-type]]
   (assoc db :message-filter {:type filter-type})))
