(ns com.github.ebaptistella.frontend.components.respond-modal
  (:require [clojure.string :as str]
            [re-frame.core :as rf]))

(defn- response-type->label [rt]
  (condp re-find rt
    #"R1$" [(str rt " — Aceitar")        "Envia R1 (LQDADO) para a IF debitada"]
    #"R2$" [(str rt " — Notificar IF-Creditada") "Envia R2 com notificação para a IF creditada"]
    #"E$"  [(str rt " — Rejeitar")       "Envia rejeição com motivo para a IF debitada"]
    [(str rt) ""]))

(defn- response-type->dispatch-key [rt]
  (condp re-find rt
    #"R1$" :accept
    #"R2$" :send-r2
    #"E$"  :reject
    (keyword rt)))

(defn- option-card [dispatch-key label description selected?]
  [:div {:class    (str "p-4 border-2 rounded-lg cursor-pointer transition-all "
                        (if selected?
                          "border-indigo-500 bg-indigo-50"
                          "border-gray-200 hover:border-gray-300"))
         :on-click #(rf/dispatch [:respond/set-response-type dispatch-key])}
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
        avail         (:available-responses msg)
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
         (for [rt avail
               :let [dispatch-key (response-type->dispatch-key rt)
                     [label desc] (response-type->label rt)]]
           ^{:key rt}
           [option-card dispatch-key label desc (= response-type dispatch-key)])]
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
