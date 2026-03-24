(ns com.github.ebaptistella.frontend.db.messages-db)

(defn default-db []
  {:list            []
   :selected-id     nil
   :loading?        false
   :offline?        false
   :retry-count     0
   :polling-active? true
   :last-fetch-at   nil
   :limit           50
   :offset          0
   :total           0})
