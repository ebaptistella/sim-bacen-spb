(ns com.github.ebaptistella.frontend.components.message-list
  (:require [clojure.string :as str]
            [com.github.ebaptistella.frontend.util.format :as fmt]
            [re-frame.core :as rf]))

(defn- status-badge [status direction msg-type]
  (cond
    (= direction "outbound")
    [:span.inline-flex.items-center.px-2.5.py-0.5.rounded-full.text-xs.font-medium.bg-purple-100.text-purple-800
     "Enviada"]

    (= status "auto-responded")
    [:span.inline-flex.items-center.px-2.5.py-0.5.rounded-full.text-xs.font-medium.bg-blue-100.text-blue-800
     "Auto"]

    (and (str/starts-with? msg-type "SLB") (= status "pending"))
    [:span.inline-flex.items-center.px-2.5.py-0.5.rounded-full.text-xs.font-medium.bg-yellow-100.text-yellow-800
     "Aguardando"]

    (= status "pending")
    [:span.inline-flex.items-center.px-2.5.py-0.5.rounded-full.text-xs.font-medium.bg-yellow-100.text-yellow-800
     "Pendente"]

    :else
    [:span.inline-flex.items-center.px-2.5.py-0.5.rounded-full.text-xs.font-medium.bg-green-100.text-green-800
     "Respondida"]))

(defn- message-row [msg selected-id]
  (let [selected? (= (:id msg) selected-id)]
    [:tr {:class    (str "cursor-pointer transition-colors "
                         (if selected?
                           "bg-indigo-50"
                           "hover:bg-gray-50"))
          :on-click #(rf/dispatch [:messages/select-message (:id msg)])}
     [:td.px-4.py-3.text-sm.font-mono.text-indigo-600 (:type msg)]
     [:td.px-4.py-3.text-sm.font-mono (:participant msg)]
     [:td.px-4.py-3.text-sm.text-right.font-mono (fmt/format-currency (:vlr-lanc msg))]
     [:td.px-4.py-3.text-sm [status-badge (name (:status msg)) (:direction msg) (:type msg)]]
     [:td.px-4.py-3.text-sm.text-gray-500 (fmt/format-date (:received-at msg))]
     [:td.px-4.py-3.text-sm.font-mono.text-gray-600 (:num-ctrl-if msg)]
     [:td.px-4.py-3.text-sm.text-gray-500 (:finldd-cli msg)]]))

(defn- mobile-card [msg selected-id]
  (let [selected? (= (:id msg) selected-id)]
    [:div {:class    (str "p-4 border-b cursor-pointer transition-colors "
                          (if selected? "bg-indigo-50" "hover:bg-gray-50"))
           :on-click #(rf/dispatch [:messages/select-message (:id msg)])}
     [:div.flex.items-center.justify-between.mb-2
      [:span.font-mono.text-indigo-600.font-medium (:type msg)]
      [status-badge (name (:status msg)) (:direction msg) (:type msg)]]
     [:div.grid.grid-cols-2.gap-1.text-sm
      [:span.text-gray-500 "Participante:"]
      [:span.font-mono (:participant msg)]
      [:span.text-gray-500 "Valor:"]
      [:span.font-mono.text-right (fmt/format-currency (:vlr-lanc msg))]
      [:span.text-gray-500 "NumCtrlIF:"]
      [:span.font-mono.text-gray-600 (:num-ctrl-if msg)]
      [:span.text-gray-500 "Recebida:"]
      [:span (fmt/format-date (:received-at msg))]]]))

(defn- spinner []
  [:div.flex.justify-center.py-4
   [:div.animate-spin.rounded-full.h-8.w-8.border-b-2.border-indigo-500]])

(defn- offline-banner []
  [:div.bg-yellow-100.border-l-4.border-yellow-500.text-yellow-700.p-4.mb-4
   [:p.font-medium "Offline — tentando reconectar..."]])

(defn- empty-state []
  [:div.text-center.py-12
   [:p.text-gray-400.text-lg "Nenhuma mensagem recebida."]])

(defn message-list []
  (let [messages      @(rf/subscribe [:messages/filtered-list])
        filter-type   @(rf/subscribe [:message-filter/type])
        loading?      @(rf/subscribe [:messages/loading?])
        offline?      @(rf/subscribe [:messages/offline?])
        selected-id   @(rf/subscribe [:messages/selected-id])]
    [:div
     (when offline? [offline-banner])
     [:div.px-4.py-3.bg-gray-50.border-b.border-gray-200.flex.items-center.gap-3
      [:label.text-sm.font-medium.text-gray-700 "Filtrar por:"]
      [:select.px-3.py-1.border.border-gray-300.rounded-lg.text-sm.text-gray-700
       {:value filter-type
        :on-change #(rf/dispatch [:message-filter/set-type (.. % -target -value)])}
       [:option {:value "all"} "Todas as mensagens"]
       [:option {:value "str"} "Apenas STR"]
       [:option {:value "slb"} "Apenas SLB"]]]
     (when loading? [spinner])
     (if (empty? messages)
       [empty-state]
       [:<>
        ;; Desktop table
        [:div.hidden.sm:block
         [:table.min-w-full.divide-y.divide-gray-200
          [:thead.bg-gray-50
           [:tr
            [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-500.uppercase "Tipo"]
            [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-500.uppercase "Participante"]
            [:th.px-4.py-3.text-right.text-xs.font-medium.text-gray-500.uppercase "Valor"]
            [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-500.uppercase "Status"]
            [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-500.uppercase "Recebida"]
            [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-500.uppercase "NumCtrlIF"]
            [:th.px-4.py-3.text-left.text-xs.font-medium.text-gray-500.uppercase "Finalidade"]]]
          [:tbody.bg-white.divide-y.divide-gray-200
           (for [msg messages]
             ^{:key (:id msg)}
             [message-row msg selected-id])]]]
        ;; Mobile cards
        [:div.sm:hidden
         (for [msg messages]
           ^{:key (:id msg)}
           [mobile-card msg selected-id])]])]))
