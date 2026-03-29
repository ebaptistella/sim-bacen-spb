(ns com.github.ebaptistella.frontend.events
  (:require [com.github.ebaptistella.frontend.db :as db]
            [com.github.ebaptistella.frontend.events.message-filter-events]
            [com.github.ebaptistella.frontend.events.messages-events]
            [com.github.ebaptistella.frontend.events.outbound-events]
            [com.github.ebaptistella.frontend.events.respond-events]
            [com.github.ebaptistella.frontend.events.router-events]
            [com.github.ebaptistella.frontend.events.slb-form-events]
            [com.github.ebaptistella.frontend.events.slb-response-events]
            [com.github.ebaptistella.frontend.events.toast-events]
            [re-frame.core :as rf]))

(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   (db/default-db)))
