(ns com.github.ebaptistella.frontend.components.confirmation-modal
  (:require [re-frame.core :as rf]))

(def ^:private error-messages
  {404 "Mensagem não encontrada"
   409 "Mensagem já foi respondida"
   400 "Campo obrigatório faltando"
   500 "Falha ao enviar resposta — tente novamente"
   0   "Conexão perdida — tente novamente"})

(defn- response-type-label [response-type msg-type]
  (case response-type
    :accept  (str msg-type "R1 (Aceitar)")
    :reject  (str msg-type "E (Rejeitar)")
    :send-r2 (str msg-type "R2 (Notificar IF Creditada)")
    (str response-type)))

(defn confirmation-modal []
  (let [visible?      @(rf/subscribe [:respond/confirmation-visible?])
        response-type @(rf/subscribe [:respond/response-type])
        submitting?   @(rf/subscribe [:respond/submitting?])
        error         @(rf/subscribe [:respond/error])
        msg           @(rf/subscribe [:messages/selected-message])]
    (when visible?
      [:div.fixed.inset-0.z-50.flex.items-center.justify-center
       [:div.absolute.inset-0.bg-black.bg-opacity-50]
       [:div.relative.bg-white.rounded-xl.shadow-2xl.w-full.max-w-sm.mx-4.p-6
        [:h3.text-lg.font-semibold.text-gray-800.mb-4 "Confirmar envio"]
        [:p.text-gray-600.mb-6
         "Enviar " [:span.font-semibold (response-type-label response-type (:type msg))]
         " para mensagem " [:span.font-mono.font-semibold (:num-ctrl-if msg)] "?"]
        (when error
          [:div.mb-4.p-3.bg-red-50.border.border-red-200.rounded-lg
           [:p.text-sm.text-red-700
            (get error-messages (:status error)
                 "Erro desconhecido — tente novamente")]
           (when (= (:status error) 409)
             [:button {:class    "mt-2 text-sm text-indigo-600 hover:underline"
                       :on-click #(do (rf/dispatch [:respond/close-all])
                                      (rf/dispatch [:messages/fetch-initial]))}
              "Atualizar lista"])])
        [:div.flex.gap-3
         [:button {:class    "flex-1 px-4 py-2.5 border border-gray-300 rounded-lg
                               text-gray-700 hover:bg-gray-50 transition-colors font-medium"
                   :disabled submitting?
                   :on-click #(rf/dispatch [:respond/back-to-modal])}
          "Voltar"]
         [:button {:class    (str "flex-1 px-4 py-2.5 rounded-lg font-medium transition-colors "
                                  (if submitting?
                                    "bg-indigo-400 cursor-not-allowed"
                                    "bg-indigo-600 hover:bg-indigo-700")
                                  " text-white")
                   :disabled submitting?
                   :on-click #(rf/dispatch [:respond/submit])}
          (if submitting?
            [:span.flex.items-center.justify-center.gap-2
             [:span.animate-spin.inline-block.w-4.h-4.border-2.border-white.border-t-transparent.rounded-full]
             "Enviando..."]
            "Enviar")]]]])))
