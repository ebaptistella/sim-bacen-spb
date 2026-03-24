(ns com.github.ebaptistella.frontend.events.router-events
  (:require [re-frame.core :as rf]))

(rf/reg-event-db
 :router/navigate
 (fn [db [_ route]]
   (let [db (assoc db :router route)]
     (if-let [id (:id route)]
       (assoc-in db [:messages :selected-id] id)
       db))))

(rf/reg-event-db
 :router/set-page
 (fn [db [_ page]]
   (assoc-in db [:router :page] page)))
