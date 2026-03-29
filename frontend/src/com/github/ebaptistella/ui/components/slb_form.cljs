(ns com.github.ebaptistella.ui.components.slb-form
  "SLB message injection forms for each message type."
  (:require [reagent.core :as r]
            [com.github.ebaptistella.ui.http-client :as http]))

(defonce slb-form-state (r/atom {:type nil :fields {} :loading? false :message nil :tracking-id nil}))

(defn set-field! [field value]
  (swap! slb-form-state update :fields assoc field value))

(defn reset-form! []
  (swap! slb-form-state assoc
         :fields {} :loading? false :message nil :tracking-id nil))

(defn submit-form! [msg-type endpoint]
  (swap! slb-form-state assoc :loading? true)
  (let [data (:fields @slb-form-state)]
    (-> (http/post-json endpoint data)
        (.then (fn [res]
                 (let [res-clj (js->clj res :keywordize-keys true)
                       tracking-id (get-in res-clj [:data :num-ctrl-part])]
                   (swap! slb-form-state assoc
                          :loading? false
                          :message (str msg-type " injected successfully")
                          :tracking-id tracking-id)
                   (js/setTimeout #(reset-form!) 3000))))
        (.catch (fn [err]
                  (swap! slb-form-state assoc
                         :loading? false
                         :message (str "Error: " err)))))))

(defn text-input [{:keys [label field type required]}]
  [:div.mb-4
   [:label.block.text-sm.font-medium.mb-1 (str label (when required " *"))]
   [:input.border.border-gray-300.rounded.px-3.py-2.w-full
    {:type (or type "text")
     :value (get-in @slb-form-state [:fields field] "")
     :on-change #(set-field! field (.. % -target -value))
     :disabled (:loading? @slb-form-state)
     :placeholder label}]])

(defn slb0001-form []
  [:div.max-w-md
   [text-input {:label "NumCtrlSLB" :field :NumCtrlSLB :required true}]
   [text-input {:label "ISPBPart" :field :ISPBPart :required true}]
   [text-input {:label "DtVenc (YYYYMMDD)" :field :DtVenc :required true}]
   [text-input {:label "VlrLanc" :field :VlrLanc :type "number" :required true}]
   [text-input {:label "FIndddSLB" :field :FIndddSLB :required true}]
   [text-input {:label "Hist (optional)" :field :Hist}]
   [:button.bg-blue-500.text-white.px-4.py-2.rounded.disabled:opacity-50
    {:on-click #(submit-form! "SLB0001" "/api/v1/slb/slb0001")
     :disabled (:loading? @slb-form-state)}
    (if (:loading? @slb-form-state) "Sending..." "Submit SLB0001")]
   (when (:message @slb-form-state)
     [:p.mt-4.text-sm {:class (if (clojure.string/includes? (:message @slb-form-state) "Error") "text-red-600" "text-green-600")}
      (:message @slb-form-state)])
   (when (:tracking-id @slb-form-state)
     [:p.mt-2.text-sm.text-gray-600
      "Tracking ID: " (:tracking-id @slb-form-state)])])

(defn slb0006-form []
  [:div.max-w-md
   [text-input {:label "NumCtrlPart" :field :NumCtrlPart :required true}]
   [text-input {:label "ISPBPart" :field :ISPBPart :required true}]
   [text-input {:label "DtRef (optional)" :field :DtRef}]
   [text-input {:label "TpDeb_Cred (optional)" :field :TpDeb_Cred}]
   [text-input {:label "NumCtrlSLB (optional)" :field :NumCtrlSLB}]
   [:button.bg-blue-500.text-white.px-4.py-2.rounded.disabled:opacity-50
    {:on-click #(submit-form! "SLB0006" "/api/v1/slb/slb0006")
     :disabled (:loading? @slb-form-state)}
    (if (:loading? @slb-form-state) "Sending..." "Consult")]
   (when (:message @slb-form-state)
     [:p.mt-4.text-sm {:class (if (clojure.string/includes? (:message @slb-form-state) "Error") "text-red-600" "text-green-600")}
      (:message @slb-form-state)])
   (when (:tracking-id @slb-form-state)
     [:p.mt-2.text-sm.text-gray-600
      "Tracking ID: " (:tracking-id @slb-form-state)])])
