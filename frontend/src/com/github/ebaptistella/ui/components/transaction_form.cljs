(ns com.github.ebaptistella.ui.components.transaction-form
  "Form section: enviar transação SPB. Receives form data from state and callbacks."
  (:require [com.github.ebaptistella.ui.components.select-field :as select-field]
            [com.github.ebaptistella.ui.components.form-status-message :as form-status-message]
            [com.github.ebaptistella.ui.models :as models]))

(defn transaction-form
  "Props: form-data, on-form-change, loading?, message, message-type, on-submit (fn [e]), on-reset (fn [])."
  [{:keys [form-data on-form-change loading? message message-type on-submit on-reset]}]
  [:section.mb-10
   [:h2.text-2xl.mb-5.text-gray-800 "Enviar Transação"]
   [:form
    {:on-submit (fn [e]
                  (.preventDefault e)
                  (when on-submit (on-submit e)))}
    [select-field/select-field
     {:id        "tipo"
      :label     "Tipo de Transação"
      :value     (get form-data :tipo "")
      :options   models/tipos-transacao
      :on-change #(when on-form-change (on-form-change :tipo %))}]
    [:div.mb-4
     [:label.block.text-sm.font-medium.text-gray-700.mb-1 {:for "valor"} "Valor (R$)"]
     [:input#valor.w-full.border.rounded.p-2
      {:type        "number"
       :min         "0.01"
       :step        "0.01"
       :placeholder "Ex: 1000.00"
       :value       (get form-data :valor "")
       :on-change   #(when on-form-change (on-form-change :valor (.. % -target -value)))}]]
    [:div.mb-4
     [:label.block.text-sm.font-medium.text-gray-700.mb-1 {:for "origem"} "Conta Origem (ISPB)"]
     [:input#origem.w-full.border.rounded.p-2
      {:type        "text"
       :placeholder "Ex: 00000000"
       :value       (get form-data :origem "")
       :on-change   #(when on-form-change (on-form-change :origem (.. % -target -value)))}]]
    [:div.mb-4
     [:label.block.text-sm.font-medium.text-gray-700.mb-1 {:for "destino"} "Conta Destino (ISPB)"]
     [:input#destino.w-full.border.rounded.p-2
      {:type        "text"
       :placeholder "Ex: 00000001"
       :value       (get form-data :destino "")
       :on-change   #(when on-form-change (on-form-change :destino (.. % -target -value)))}]]
    [:div.flex.gap-3
     [:button.px-4.py-2.bg-indigo-600.text-white.rounded.hover:bg-indigo-700
      {:type "submit" :disabled loading?}
      (if loading? "Enviando..." "Enviar")]
     (when on-reset
       [:button.px-4.py-2.bg-gray-200.text-gray-700.rounded.hover:bg-gray-300
        {:type "button" :on-click (fn [_] (on-reset))}
        "Limpar"])]
    [form-status-message/form-status-message
     {:message message :message-type message-type}]]])
