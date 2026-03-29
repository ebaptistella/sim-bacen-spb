(ns com.github.ebaptistella.frontend.components.slb-form
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

;; Field definitions for each SLB message type
(def ^:private field-specs
  {:SLB0002 {:title "SLB0002 - Débito em Conta Corrente"
             :required [:NumCtrlPart :ISPBPart :VlrLanc]
             :optional [:DtMovto :Hist]}
   :SLB0005 {:title "SLB0005 - Débito em Conta Corrente (Específico)"
             :required [:NumCtrlSTR :ISPBPart :VlrLanc :FIndddSLB :NumCtrlSLB :DtVenc]
             :optional [:Hist]}
   :SLB0006 {:title "SLB0006 - Consulta de Posição"
             :required [:NumCtrlPart :ISPBPart]
             :optional [:DtRef :TpDeb_Cred :NumCtrlSLB]}
   :SLB0007 {:title "SLB0007 - Débito Específico"
             :required [:NumCtrlPart :ISPBPart :VlrLanc]
             :optional [:DtMovto :Hist]}
   :SLB0008 {:title "SLB0008 - Débito Genérico"
             :required [:NumCtrlSLB :ISPBPart :VlrLanc]
             :optional [:Hist :DtVenc]}})

(def ^:private field-labels
  {:NumCtrlPart "Nº Controle Participante"
   :NumCtrlSTR "Nº Controle STR"
   :NumCtrlSLB "Nº Controle SLB"
   :ISPBPart "ISPB Participante"
   :VlrLanc "Valor (R$)"
   :DtMovto "Data Movimento (YYYYMMDD)"
   :DtRef "Data Referência (YYYYMMDD)"
   :DtVenc "Data Vencimento (YYYYMMDD)"
   :FIndddSLB "Finalidade SLB"
   :TpDeb_Cred "Tipo Débito/Crédito"
   :NumCtrlSLBOr "Nº Controle SLB Original"
   :Hist "Histórico"})

(defn- field-input [field-key value loading?]
  (let [label (get field-labels field-key (name field-key))]
    [:div.mb-4
     [:label.block.text-sm.font-medium.text-gray-700.mb-1 label]
     [:input {:type       (if (= field-key :VlrLanc) "number" "text")
              :class      "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              :value      (or value "")
              :on-change  #(rf/dispatch [:slb-form/set-field field-key (.. % -target -value)])
              :disabled   loading?
              :step       (when (= field-key :VlrLanc) "0.01")
              :placeholder label}]]))

(defn slb-form []
  (let [visible?    @(rf/subscribe [:slb-form/visible?])
        msg-type    @(rf/subscribe [:slb-form/type])
        fields      @(rf/subscribe [:slb-form/fields])
        submitting? @(rf/subscribe [:slb-form/submitting?])
        error       @(rf/subscribe [:slb-form/error])
        spec        (get field-specs (keyword msg-type))
        valid?      (and msg-type
                         spec
                         (every? #(not (str/blank? (get fields %))) (:required spec))
                         (not submitting?))]
    (when visible?
      [:div.fixed.inset-0.z-50.flex.items-center.justify-center
       [:div.absolute.inset-0.bg-black.bg-opacity-50
        {:on-click #(rf/dispatch [:slb-form/close])}]
       [:div.relative.bg-white.rounded-xl.shadow-2xl.w-full.max-w-md.mx-4.p-6
        [:h3.text-lg.font-semibold.text-gray-800.mb-4
         (if spec (:title spec) "Enviar Mensagem SLB")]

        [:div.space-y-4.mb-4
         [:div
          [:label.block.text-sm.font-medium.text-gray-700.mb-1 "Tipo de Mensagem"]
          [:select {:class     "w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                    :value     (or msg-type "")
                    :on-change #(rf/dispatch [:slb-form/set-type (.. % -target -value)])}
           [:option {:value ""} "Selecione..."]
           [:option {:value "SLB0002"} "SLB0002 — Débito em Conta Corrente"]
           [:option {:value "SLB0005"} "SLB0005 — Débito Específico"]
           [:option {:value "SLB0006"} "SLB0006 — Consulta de Posição"]
           [:option {:value "SLB0007"} "SLB0007 — Débito Específico"]
           [:option {:value "SLB0008"} "SLB0008 — Débito Genérico"]]]

         (when spec
           [:div.space-y-4
            (doall
              (for [field-key (concat (:required spec) (:optional spec))]
                ^{:key field-key}
                [field-input field-key (get fields field-key) submitting?]))])]

        (when error
          [:div.mb-4.p-3.bg-red-50.rounded-lg.border.border-red-200
           [:p.text-sm.text-red-700 (:message error)]])

        [:div.flex.gap-3
         [:button {:class    "flex-1 px-4 py-2.5 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors font-medium"
                   :on-click #(rf/dispatch [:slb-form/close])}
          "Cancelar"]
         [:button {:class    (str "flex-1 px-4 py-2.5 rounded-lg font-medium transition-colors "
                                  (if valid?
                                    "bg-indigo-600 text-white hover:bg-indigo-700"
                                    "bg-gray-200 text-gray-400 cursor-not-allowed"))
                   :disabled (not valid?)
                   :on-click #(when valid? (rf/dispatch [:slb-form/submit]))}
          (if submitting? "Enviando..." "Enviar")]]]])))
