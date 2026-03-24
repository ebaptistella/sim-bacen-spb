(ns com.github.ebaptistella.frontend.db.respond-db)

(defn default-db []
  {:modal-visible?        false
   :confirmation-visible? false
   :response-type         nil
   :motivo                nil
   :submitting?           false
   :error                 nil})
