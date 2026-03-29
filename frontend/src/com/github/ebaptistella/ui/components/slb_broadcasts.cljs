(ns com.github.ebaptistella.ui.components.slb-broadcasts
  "SLB unidirectional broadcasts (SLB0001, SLB0003) - sent by simulator without user input."
  (:require [reagent.core :as r]
            [com.github.ebaptistella.ui.http-client :as http]))

(defonce broadcast-state (r/atom {:loading? false :message nil}))

(defn send-broadcast! [msg-type endpoint data]
  (swap! broadcast-state assoc :loading? true :message nil)
  (-> (http/post-json endpoint data)
      (.then (fn [res]
               (let [res-clj (js->clj res :keywordize-keys true)]
                 (swap! broadcast-state assoc
                        :loading? false
                        :message (str msg-type " sent successfully"))
                 (js/setTimeout #(swap! broadcast-state assoc :message nil) 3000))))
      (.catch (fn [err]
                (swap! broadcast-state assoc
                       :loading? false
                       :message (str "Error: " err))))))

(defn broadcast-button [label msg-type endpoint data]
  [:button.bg-purple-500.text-white.px-6.py-3.rounded.hover:bg-purple-600.disabled:opacity-50
   {:on-click #(send-broadcast! msg-type endpoint data)
    :disabled (:loading? @broadcast-state)}
   (if (:loading? @broadcast-state) "Sending..." label)])

(defn slb-broadcasts []
  [:div
   [:h3.text-lg.font-semibold.mb-4 "Broadcasts (Simulator Initiative)"]
   [:p.text-sm.text-gray-600.mb-6
    "Send unidirectional messages without waiting for response."]
   [:div.space-y-4
    [:div
     [:h4.font-medium.mb-2 "SLB0001 - Situation Notification"]
     [broadcast-button "Send SLB0001" "SLB0001" "/api/v1/slb/slb0001"
      {:NumCtrlPart "AUTO" :ISPBPart "00000000" :DtMovto ""}]]
    [:div
     [:h4.font-medium.mb-2 "SLB0003 - Position Statement"]
     [broadcast-button "Send SLB0003" "SLB0003" "/api/v1/slb/slb0003"
      {:NumCtrlPart "AUTO" :ISPBPart "00000000"}]]]
   (when (:message @broadcast-state)
     [:p.mt-4.text-sm {:class (if (clojure.string/includes? (:message @broadcast-state) "Error")
                                "text-red-600" "text-green-600")}
      (:message @broadcast-state)])])
