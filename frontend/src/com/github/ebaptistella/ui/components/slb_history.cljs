(ns com.github.ebaptistella.ui.components.slb-history
  "SLB message history with infinite scroll (20 items per chunk, most recent first)."
  (:require [reagent.core :as r]
            [com.github.ebaptistella.ui.http-client :as http]))

(defonce history-state (r/atom {:messages [] :offset 0 :loading? false :has-more true :detail-id nil}))

(defn set-detail! [msg-id]
  (swap! history-state assoc :detail-id msg-id))

(defn close-detail! []
  (swap! history-state assoc :detail-id nil))

(defn fetch-messages! [append?]
  (when-not (:loading? @history-state)
    (swap! history-state assoc :loading? true)
    (let [offset (if append? (:offset @history-state) 0)]
      (-> (http/get-raw "/api/v1/messages" {:limit 20 :offset offset})
          (.then (fn [res]
                   (let [res-clj (js->clj res :keywordize-keys true)
                         msgs (get-in res-clj [:data :messages] [])
                         total (get-in res-clj [:data :total] 0)]
                     (swap! history-state (fn [s]
                                           (-> s
                                               (update :messages #(if append? (into % msgs) msgs))
                                               (assoc :offset (+ offset (count msgs))
                                                      :loading? false
                                                      :has-more (>= (count msgs) 20)))))))
                   (js/setTimeout #(fetch-messages! true) 2000)))
          (.catch (fn [err]
                   (swap! history-state assoc :loading? false)))))))

(defn message-row [msg]
  [:tr.border-t.hover:bg-gray-50.cursor-pointer
   {:on-click #(set-detail! (:id msg))}
   [:td.px-4.py-2.text-sm (subs (:type msg) 0 12)]
   [:td.px-4.py-2.text-sm (subs (:received-at msg) 0 19)]
   [:td.px-4.py-2.text-sm (:participant msg)]
   [:td.px-4.py-2.text-sm (subs (or (:body msg) "") 0 50)]])

(defn detail-modal [msg-id]
  (let [msg (first (filter #(= (:id %) msg-id) (:messages @history-state)))]
    (when msg
      [:div.fixed.inset-0.bg-black.bg-opacity-50.flex.items-center.justify-center.z-50
       [:div.bg-white.rounded.p-6.max-w-2xl.max-h-96.overflow-y-auto
        [:div.flex.justify-between.items-center.mb-4
         [:h3.text-lg.font-semibold (str "Message: " (:type msg))]
         [:button.text-gray-500.hover:text-gray-700
          {:on-click close-detail!}
          "✕"]]
        [:div.space-y-2.text-sm
         [:p [:span.font-medium "ID: "] (:id msg)]
         [:p [:span.font-medium "Type: "] (:type msg)]
         [:p [:span.font-medium "Status: "] (str (:status msg))]
         [:p [:span.font-medium "Received: "] (:received-at msg)]
         [:p [:span.font-medium "Participant: "] (:participant msg)]
         (when (:num-ctrl-part msg)
           [:p [:span.font-medium "Tracking ID: "] (:num-ctrl-part msg)])
         [:p.mt-4.text-xs.bg-gray-100.p-2.rounded
          [:span.font-medium "Body (first 300 chars): "]
          [:br]
          (subs (or (:body msg) "") 0 300)]]]])))

(defn history-list []
  (r/create-class
    {:component-did-mount #(fetch-messages! false)
     :render
     (fn []
       [:div
        [:table.w-full.text-sm.border-collapse
         [:thead.bg-gray-100
          [:tr
           [:th.text-left.px-4.py-2 "Type"]
           [:th.text-left.px-4.py-2 "Received At"]
           [:th.text-left.px-4.py-2 "Participant"]
           [:th.text-left.px-4.py-2 "Preview"]]]
         [:tbody
          (doall
            (for [msg (:messages @history-state)]
              ^{:key (:id msg)}
              [message-row msg]))]]

        (when (:loading? @history-state)
          [:p.text-center.text-gray-500.mt-4 "Loading messages..."])

        (when-not (:has-more @history-state)
          [:p.text-center.text-gray-500.mt-4 "No more messages"])

        (when (:detail-id @history-state)
          [detail-modal (:detail-id @history-state)])])}))
