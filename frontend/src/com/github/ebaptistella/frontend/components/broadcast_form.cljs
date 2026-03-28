(ns com.github.ebaptistella.frontend.components.broadcast-form
  (:require [re-frame.core :as rf]))

(defn- dynamic-fields [msg-type params]
  (case msg-type
    "STR0015"
    [:div.space-y-3
     [:div
      [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Hora de Fechamento"]
      [:input {:type      "text"
               :class     "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
               :value     (or (:hr-fechamento params) "")
               :on-change #(rf/dispatch [:outbound/set-param :hr-fechamento (.. % -target -value)])}]]]

    "STR0016"
    [:div.space-y-3
     [:div
      [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Hora de Fechamento"]
      [:input {:type      "text"
               :class     "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
               :value     (or (:hr-fechamento params) "")
               :on-change #(rf/dispatch [:outbound/set-param :hr-fechamento (.. % -target -value)])}]]
     [:div
      [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Saldo Reservas (R$)"]
      [:input {:type      "text"
               :class     "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
               :value     (or (:saldo params) "")
               :on-change #(rf/dispatch [:outbound/set-param :saldo (.. % -target -value)])}]]]

    "STR0017"
    [:div.space-y-3
     [:div
      [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Hora de Abertura"]
      [:input {:type      "text"
               :class     "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
               :value     (or (:hr-abertura params) "")
               :on-change #(rf/dispatch [:outbound/set-param :hr-abertura (.. % -target -value)])}]]
     [:div
      [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Hora de Fechamento Prevista"]
      [:input {:type      "text"
               :class     "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
               :value     (or (:hr-fechamento params) "")
               :on-change #(rf/dispatch [:outbound/set-param :hr-fechamento (.. % -target -value)])}]]]

    "STR0018"
    [:div.space-y-3
     [:div
      [:label.block.text-sm.font-medium.text-gray-700.mb-1 "ISPB do Participante Excluído"]
      [:input {:type      "text"
               :class     "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
               :max-length 8
               :value     (or (:ispb-participante params) "")
               :on-change #(rf/dispatch [:outbound/set-param :ispb-participante (.. % -target -value)])}]
      [:p.text-xs.text-gray-500.mt-1 "8 dígitos — deixe vazio para usar o ISPB do simulador"]]]

    "STR0019"
    [:div.space-y-3
     [:div
      [:label.block.text-sm.font-medium.text-gray-700.mb-1 "ISPB do Participante Incluído/Alterado"]
      [:input {:type      "text"
               :class     "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
               :max-length 8
               :value     (or (:ispb-participante params) "")
               :on-change #(rf/dispatch [:outbound/set-param :ispb-participante (.. % -target -value)])}]
      [:p.text-xs.text-gray-500.mt-1 "8 dígitos — deixe vazio para usar o ISPB do simulador"]]]

    ;; STR0030, STR0042, STR0050 — sem parâmetros adicionais
    [:div]))

(defn broadcast-form []
  (let [visible?    @(rf/subscribe [:outbound/modal-visible?])
        msg-type    @(rf/subscribe [:outbound/type])
        participant @(rf/subscribe [:outbound/participant])
        params      @(rf/subscribe [:outbound/params])
        submitting? @(rf/subscribe [:outbound/submitting?])
        error       @(rf/subscribe [:outbound/error])
        valid?      (and msg-type
                         (= 8 (count participant))
                         (re-matches #"[0-9]{8}" participant)
                         (not submitting?))]
    (when visible?
      [:div.fixed.inset-0.z-50.flex.items-center.justify-center
       [:div.absolute.inset-0.bg-black.bg-opacity-50
        {:on-click #(rf/dispatch [:outbound/close-modal])}]
       [:div.relative.bg-white.rounded-xl.shadow-2xl.w-full.max-w-md.mx-4.p-6
        [:h3.text-lg.font-semibold.text-gray-800.mb-4 "Enviar Mensagem BACEN"]
        [:div.space-y-4.mb-4
         [:div
          [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Tipo de Mensagem"]
          [:select {:class     "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                    :value     (or msg-type "")
                    :on-change #(rf/dispatch [:outbound/set-type (.. % -target -value)])}
           [:option {:value ""} "Selecione..."]
           [:optgroup {:label "Avisos de operação"}
            [:option {:value "STR0015"} "STR0015 — Fechamento do STR"]
            [:option {:value "STR0017"} "STR0017 — Abertura do STR"]
            [:option {:value "STR0030"} "STR0030 — Aptidão para abertura"]
            [:option {:value "STR0042"} "STR0042 — Início/fim de otimização"]]
           [:optgroup {:label "Participantes"}
            [:option {:value "STR0018"} "STR0018 — Exclusão de participante"]
            [:option {:value "STR0019"} "STR0019 — Inclusão/Alteração de participante"]]
           [:optgroup {:label "Saldo e encerramento"}
            [:option {:value "STR0016"} "STR0016 — Saldo no fechamento"]
            [:option {:value "STR0050"} "STR0050 — Encerramento do RDC"]]]]
         [:div
          [:label.block.text-sm.font-medium.text-gray-700.mb-1
           "ISPB Participante " [:span.text-red-500 "*"]]
          [:input {:type       "text"
                   :class      "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                   :max-length 8
                   :value      participant
                   :on-change  #(rf/dispatch [:outbound/set-participant (.. % -target -value)])}]
          [:p.text-xs.text-gray-500.mt-1 "8 dígitos numéricos"]]
         (when msg-type
           [dynamic-fields msg-type params])]
        (when error
          [:div.mb-4.p-3.bg-red-50.rounded-lg.border.border-red-200
           [:p.text-sm.text-red-700 (:message error)]])
        [:div.flex.gap-3
         [:button {:class    "flex-1 px-4 py-2.5 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors font-medium"
                   :on-click #(rf/dispatch [:outbound/close-modal])}
          "Cancelar"]
         [:button {:class    (str "flex-1 px-4 py-2.5 rounded-lg font-medium transition-colors "
                                  (if valid?
                                    "bg-indigo-600 text-white hover:bg-indigo-700"
                                    "bg-gray-200 text-gray-400 cursor-not-allowed"))
                   :disabled (not valid?)
                   :on-click #(when valid? (rf/dispatch [:outbound/submit]))}
          (if submitting? "Enviando..." "Enviar")]]]])))
