(ns com.github.ebaptistella.ui.components.form-status-message
  "Presentational: status message (success or error). Single responsibility.")

(defn form-status-message
  "Renders a status message. message-type is :success or :error."
  [{:keys [message message-type]}]
  (when message
    [:div.mt-4.p-3.rounded
     {:class (case message-type
               :success "bg-green-100 text-green-800"
               :error   "bg-red-100 text-red-800"
               "bg-gray-100 text-gray-800")}
     message]))
