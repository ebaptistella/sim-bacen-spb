(ns com.github.ebaptistella.ui.components.select-field
  "Presentational: label + select. Receives value, options, on-change. No business logic.")

(defn select-field
  "Renders a labeled select. options: [{:value \"x\" :label \"X\"} ...]"
  [{:keys [id label value options placeholder on-change]}]
  [:div.mb-4
   [:label.block.text-sm.font-medium.text-gray-700.mb-1 {:for id} label]
   [:select.w-full.border.rounded.p-2
    {:id        id
     :value     (or value "")
     :on-change (fn [e] (when on-change (on-change (.. e -target -value))))}
    [:option {:value ""} (or placeholder "Selecione...")]
    (for [opt options]
      [:option {:key (:value opt) :value (:value opt)} (:label opt)])]])
