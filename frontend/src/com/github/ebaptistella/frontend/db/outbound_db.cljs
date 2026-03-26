(ns com.github.ebaptistella.frontend.db.outbound-db)

(defn default-db []
  {:modal-visible? false
   :type           nil
   :participant    ""
   :params         {}
   :submitting?    false
   :error          nil})
