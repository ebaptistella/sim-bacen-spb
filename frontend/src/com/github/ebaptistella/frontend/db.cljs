(ns com.github.ebaptistella.frontend.db
  (:require [com.github.ebaptistella.frontend.db.messages-db :as messages-db]
            [com.github.ebaptistella.frontend.db.outbound-db :as outbound-db]
            [com.github.ebaptistella.frontend.db.respond-db :as respond-db]
            [com.github.ebaptistella.frontend.db.toast-db :as toast-db]))

(defn default-db []
  {:messages (messages-db/default-db)
   :outbound (outbound-db/default-db)
   :respond  (respond-db/default-db)
   :toast    (toast-db/default-db)
   :router   {:page :messages :id nil}})
