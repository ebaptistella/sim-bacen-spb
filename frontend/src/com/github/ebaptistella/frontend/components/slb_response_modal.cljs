(ns com.github.ebaptistella.frontend.components.slb-response-modal
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(def ^:private response-titles
  {:SLB0002R1 "SLB0002R1 — Resposta de Débito em Conta"
   :SLB0006R1 "SLB0006R1 — Resposta de Posição"
   :SLB0007R1 "SLB0007R1 — Resposta de Débito Específico"})

(defn- field-label [field-key]
  (case field-key
    :NumCtrlPart "Nº Controle Participante"
    :NumCtrlSLB "Nº Controle SLB"
    :ISPBPart "ISPB Participante"
    :VlrLanc "Valor (R$)"
    :VlrDebito "Valor Débito"
    :VlrCredito "Valor Crédito"
    :Saldo "Saldo"
    :DtSaldo "Data Saldo"
    :DtVenc "Data Vencimento"
    :DtMovto "Data Movimento"
    :SitDebito "Situação Débito"
    :DtSitDebito "Data Situação"
    :MotRecusa "Motivo Recusa"
    :Hist "Histórico"
    (str/replace (name field-key) #"([a-z])([A-Z])" "$1 $2")))

(defn- response-field [field-key value]
  (when value
    [:div.py-2.border-b.border-gray-100
     [:p.text-sm.font-medium.text-gray-600 (field-label field-key)]
     [:p.text-sm.text-gray-900 (str value)]]))

(defn slb-response-modal []
  (let [visible?     @(rf/subscribe [:slb-response/visible?])
        msg-type     @(rf/subscribe [:slb-response/message-type])
        fields       @(rf/subscribe [:slb-response/fields])
        title        (or (get response-titles (keyword msg-type)) msg-type)]
    (when visible?
      [:div.fixed.inset-0.z-50.flex.items-center.justify-center
       [:div.absolute.inset-0.bg-black.bg-opacity-50
        {:on-click #(rf/dispatch [:slb-response/hide-response])}]
       [:div.relative.bg-white.rounded-xl.shadow-2xl.w-full.max-w-md.mx-4.p-6
        [:div.flex.items-center.justify-between.mb-4
         [:h3.text-lg.font-semibold.text-gray-800 title]
         [:button.text-gray-400.hover.text-gray-600.transition-colors
          {:on-click #(rf/dispatch [:slb-response/hide-response])}
          "✕"]]

        [:div.space-y-2.max-h-96.overflow-y-auto
         (doall
          (for [[field-key value] fields]
            ^{:key field-key}
            [response-field field-key value]))]

        [:div.mt-6.flex
         [:button.flex-1.px-4.py-2.bg-indigo-600.text-white.rounded-lg.font-medium.hover.bg-indigo-700.transition-colors
          {:on-click #(rf/dispatch [:slb-response/hide-response])}
          "Fechar"]]]])))
