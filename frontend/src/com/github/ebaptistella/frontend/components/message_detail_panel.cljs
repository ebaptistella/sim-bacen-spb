(ns com.github.ebaptistella.frontend.components.message-detail-panel
  (:require [com.github.ebaptistella.frontend.util.format :as fmt]
            [re-frame.core :as rf]))

(defn- field-row [label value]
  [:div.grid.grid-cols-3.gap-2.py-2.border-b.border-gray-100
   [:dt.text-sm.font-medium.text-gray-500 label]
   [:dd.col-span-2.text-sm.text-gray-900.font-mono (or value "—")]])

(defn message-detail-panel []
  (let [msg @(rf/subscribe [:messages/selected-message])]
    (when msg
      (let [pending?    (= (:status msg) "pending")
            responded?  (= (:status msg) "responded")
            response    (:response msg)
            r2-response (:r2-response msg)]
        [:div {:class "bg-white border-l border-gray-200 overflow-y-auto
                        fixed inset-0 z-40 sm:static sm:z-auto sm:inset-auto"}
         [:div.flex.items-center.justify-between.p-4.border-b.border-gray-200.bg-gray-50
          [:h2.text-lg.font-semibold.text-gray-800 "Detalhes da Mensagem"]
          [:button {:class    "text-gray-400 hover:text-gray-600 p-1 rounded-full hover:bg-gray-200"
                    :on-click #(rf/dispatch [:messages/deselect-message])}
           [:svg {:xmlns "http://www.w3.org/2000/svg" :class "h-5 w-5" :viewBox "0 0 20 20" :fill "currentColor"}
            [:path {:fill-rule "evenodd"
                    :d "M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                    :clip-rule "evenodd"}]]]]
         [:div.p-4
          [:dl
           [field-row "Tipo" (:type msg)]
           [field-row "Participante" (:participant msg)]
           [field-row "Valor" (fmt/format-currency (:vlr-lanc msg))]
           [field-row "Status" (if pending? "Pendente" "Respondida")]
           [field-row "Recebida em" (fmt/format-date (:received-at msg))]
           [field-row "NumCtrlIF" (:num-ctrl-if msg)]
           [field-row "ISPB IF Debitada" (:ispb-if-debtd msg)]
           [field-row "ISPB IF Creditada" (:ispb-if-credtd msg)]
           [field-row "Finalidade" (:finldd-cli msg)]
           [field-row "Data Movimento" (fmt/format-date (:dt-movto msg))]]
          [:<>
           (when responded?
             [:div.mt-4.p-3.bg-green-50.rounded-lg.border.border-green-200
              [:p.text-sm.text-green-800
               "Respondida em " [:span.font-medium (fmt/format-date (get-in response [:sent-at]))]
               " — tipo: " [:span.font-mono.font-medium (:type response)]]])
           (when r2-response
             [:div.mt-2.p-3.bg-blue-50.rounded-lg.border.border-blue-200
              [:p.text-sm.text-blue-800
               "R2 enviada em " [:span.font-medium (fmt/format-date (:sent-at r2-response))]
               " — " [:span.font-mono.font-medium "STR0008R2"]]])
           [:div.mt-6
            (if pending?
              [:button {:class    "w-full bg-indigo-600 text-white py-2.5 px-4 rounded-lg
                                    hover:bg-indigo-700 transition-colors font-medium"
                        :on-click #(rf/dispatch [:respond/open-modal])}
               "Responder"]
              [:div.text-center.text-sm.text-gray-400
               "Nenhuma ação disponível"])]]]]))))
