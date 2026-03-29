(ns com.github.ebaptistella.ui.components.slb-forms-additional
  "Additional SLB form components (SLB0002, SLB0005, SLB0007, SLB0008)."
  (:require [reagent.core :as r]
            [com.github.ebaptistella.ui.http-client :as http]))

(defn slb-form-generic [{:keys [msg-type endpoint fields required-fields optional-fields]}]
  (let [form-state (r/atom {:fields {} :loading? false :message nil :tracking-id nil})]
    (fn []
      [:div.max-w-md
       (for [field (concat required-fields optional-fields)]
         ^{:key field}
         [:div.mb-4
          [:label.block.text-sm.font-medium.mb-1 (str (name field) (when (required-fields field) " *"))]
          [:input.border.border-gray-300.rounded.px-3.py-2.w-full
           {:type (case field :VlrLanc "number" "text")
            :value (get-in @form-state [:fields field] "")
            :on-change #(swap! form-state update :fields assoc field (.. % -target -value))
            :disabled (:loading? @form-state)
            :placeholder (name field)}]])

       [:button.bg-blue-500.text-white.px-4.py-2.rounded.disabled:opacity-50
        {:on-click (fn []
                    (swap! form-state assoc :loading? true)
                    (let [data (:fields @form-state)]
                      (-> (http/post-json endpoint data)
                          (.then (fn [res]
                                  (let [res-clj (js->clj res :keywordize-keys true)
                                        tracking-id (get-in res-clj [:data :num-ctrl-part])]
                                    (swap! form-state assoc
                                           :loading? false
                                           :message (str msg-type " injected successfully")
                                           :tracking-id tracking-id)
                                    (js/setTimeout #(swap! form-state assoc :fields {}) 3000))))
                          (.catch (fn [err]
                                  (swap! form-state assoc
                                         :loading? false
                                         :message (str "Error: " err)))))))
         :disabled (:loading? @form-state)}
        (if (:loading? @form-state) "Sending..." (str "Submit " msg-type))]

       (when (:message @form-state)
         [:p.mt-4.text-sm {:class (if (clojure.string/includes? (:message @form-state) "Error") "text-red-600" "text-green-600")}
          (:message @form-state)])
       (when (:tracking-id @form-state)
         [:p.mt-2.text-sm.text-gray-600 "Tracking ID: " (:tracking-id @form-state)])])))

(defn slb0002-form []
  [slb-form-generic
   {:msg-type "SLB0002"
    :endpoint "/api/v1/slb/slb0002"
    :required-fields {:NumCtrlPart true :ISPBPart true :VlrLanc true}
    :optional-fields {:DtMovto false :Hist false}}])

(defn slb0005-form []
  [slb-form-generic
   {:msg-type "SLB0005"
    :endpoint "/api/v1/slb/slb0005"
    :required-fields {:NumCtrlSTR true :ISPBPart true :VlrLanc true :FIndddSLB true :NumCtrlSLB true :DtVenc true}
    :optional-fields {:Hist false}}])

(defn slb0007-form []
  [slb-form-generic
   {:msg-type "SLB0007"
    :endpoint "/api/v1/slb/slb0007"
    :required-fields {:NumCtrlPart true :ISPBPart true :VlrLanc true}
    :optional-fields {:DtMovto false :Hist false}}])

(defn slb0008-form []
  [slb-form-generic
   {:msg-type "SLB0008"
    :endpoint "/api/v1/slb/slb0008"
    :required-fields {:NumCtrlSLB true :ISPBPart true :VlrLanc true}
    :optional-fields {:Hist false :DtVenc false}}])
