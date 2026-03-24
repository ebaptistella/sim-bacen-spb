(ns com.github.ebaptistella.frontend.components.respond-modal
  (:require [clojure.string :as str]
            [re-frame.core :as rf]))

(defn- option-card [type label description selected?]
  [:div {:class    (str "p-4 border-2 rounded-lg cursor-pointer transition-all "
                        (if selected?
                          "border-indigo-500 bg-indigo-50"
                          "border-gray-200 hover:border-gray-300"))
         :on-click #(rf/dispatch [:respond/set-response-type type])}
   [:div.flex.items-center.gap-3
    [:div {:class (str "w-4 h-4 rounded-full border-2 flex items-center justify-center "
                       (if selected?
                         "border-indigo-500"
                         "border-gray-300"))}
     (when selected?
       [:div.w-2.h-2.rounded-full.bg-indigo-500])]
    [:div
     [:p.font-medium.text-gray-800 label]
     [:p.text-sm.text-gray-500 description]]]])

(defn respond-modal []
  (let [visible?      @(rf/subscribe [:respond/modal-visible?])
        response-type @(rf/subscribe [:respond/response-type])
        motivo        @(rf/subscribe [:respond/motivo])
        msg           @(rf/subscribe [:messages/selected-message])
        reject?       (= response-type :reject)
        motivo-valid? (and motivo (not (str/blank? motivo)))
        can-confirm?  (and response-type
                           (or (not reject?) motivo-valid?))]
    (when visible?
      [:div.fixed.inset-0.z-50.flex.items-center.justify-center
       [:div.absolute.inset-0.bg-black.bg-opacity-50
        {:on-click #(rf/dispatch [:respond/close-modal])}]
       [:div.relative.bg-white.rounded-xl.shadow-2xl.w-full.max-w-md.mx-4.p-6
        [:h3.text-lg.font-semibold.text-gray-800.mb-4
         "Responder " (:type msg)]
        [:div.space-y-3.mb-4
         [option-card :accept
          (str (:type msg) "R1 — Aceitar")
          "Envia R1 (LQDADO) para a IF debitada"
          (= response-type :accept)]
         [option-card :reject
          (str (:type msg) "E — Rejeitar")
          "Envia rejeição com motivo para a IF debitada"
          (= response-type :reject)]]
        (when reject?
          [:div.mb-4
           [:label.block.text-sm.font-medium.text-gray-700.mb-1
            "Motivo de Rejeição " [:span.text-red-500 "*"]]
           [:input {:type      "text"
                    :class     (str "w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 "
                                    (if (and reject? (not motivo-valid?) motivo)
                                      "border-red-300"
                                      "border-gray-300"))
                    :value     (or motivo "")
                    :on-change #(rf/dispatch [:respond/set-motivo (.. % -target -value)])}]
           (when (and motivo (not motivo-valid?))
             [:p.text-sm.text-red-500.mt-1 "Campo obrigatório"])])
        [:div.flex.gap-3
         [:button {:class    "flex-1 px-4 py-2.5 border border-gray-300 rounded-lg
                               text-gray-700 hover:bg-gray-50 transition-colors font-medium"
                   :on-click #(rf/dispatch [:respond/close-modal])}
          "Cancelar"]
         [:button {:class    (str "flex-1 px-4 py-2.5 rounded-lg font-medium transition-colors "
                                  (if can-confirm?
                                    "bg-indigo-600 text-white hover:bg-indigo-700"
                                    "bg-gray-200 text-gray-400 cursor-not-allowed"))
                   :disabled (not can-confirm?)
                   :on-click #(rf/dispatch [:respond/show-confirmation])}
          "Confirmar"]]]])))
