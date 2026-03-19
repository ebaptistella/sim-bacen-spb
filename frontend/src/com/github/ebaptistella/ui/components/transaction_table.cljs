(ns com.github.ebaptistella.ui.components.transaction-table
  "Presentational: table of transactions. Receives list of transaction maps."
  (:require [com.github.ebaptistella.ui.logic :as logic]))

(defn- status-badge
  [status]
  [:span.px-2.py-0.5.rounded-full.text-xs.font-medium
   {:class (case (str status)
             "processed" "bg-green-100 text-green-800"
             "failed"    "bg-red-100 text-red-800"
             "queued"    "bg-yellow-100 text-yellow-800"
             "pending"   "bg-blue-100 text-blue-800"
             "bg-gray-100 text-gray-800")}
   (logic/status-label status)])

(defn transaction-table
  "transactions: [{:id :tipo :valor :origem :destino :status :created-at} ...]"
  [transactions]
  [:div.overflow-x-auto
   [:table.w-full.text-left
    [:thead
     [:tr.border-b
      [:th.p-2 "ID"]
      [:th.p-2 "Tipo"]
      [:th.p-2 "Valor"]
      [:th.p-2 "Origem"]
      [:th.p-2 "Destino"]
      [:th.p-2 "Status"]
      [:th.p-2 "Data/Hora"]]]
    [:tbody
     (for [t transactions]
       [:tr.border-b.hover:bg-gray-50
        {:key (or (:id t) (str (:origem t) "-" (:destino t) "-" (rand)))}
        [:td.p-2.font-mono.text-xs (str (:id t))]
        [:td.p-2 (str (:tipo t))]
        [:td.p-2 (logic/format-valor (:valor t))]
        [:td.p-2.font-mono (str (:origem t))]
        [:td.p-2.font-mono (str (:destino t))]
        [:td.p-2 [status-badge (:status t)]]
        [:td.p-2.text-sm (str (or (:created-at t) ""))]])]]])
