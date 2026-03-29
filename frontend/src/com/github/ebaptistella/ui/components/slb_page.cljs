(ns com.github.ebaptistella.ui.components.slb-page
  "SLB page with submenu structure (Débitos, Créditos, Consultar, Histórico)."
  (:require [reagent.core :as r]
            [com.github.ebaptistella.ui.components.slb-form :as slb-form]
            [com.github.ebaptistella.ui.components.slb-forms-additional :as slb-forms-add]
            [com.github.ebaptistella.ui.components.slb-history :as slb-history]))

(defonce page-state (r/atom {:current-submenu :debitos}))

(defn set-submenu! [submenu]
  (swap! page-state assoc :current-submenu submenu)
  (slb-form/reset-form!))

(defn submenu-button [label submenu]
  [:button.px-4.py-2.text-sm.font-medium.border-b-2
   {:class (if (= submenu (:current-submenu @page-state))
             "border-blue-500 text-blue-600"
             "border-transparent text-gray-600 hover:text-gray-800")
    :on-click #(set-submenu! submenu)}
   label])

(defn slb-debitos []
  [:div
   [:h3.text-lg.font-semibold.mb-4 "Debit Operations (SLB0001, SLB0002, SLB0007, SLB0008)"]
   [:div.space-y-8
    [:div
     [:h4.font-medium.mb-2 "SLB0001"]
     [slb-form/slb0001-form]]
    [:div
     [:h4.font-medium.mb-2 "SLB0002"]
     [slb-forms-add/slb0002-form]]
    [:div
     [:h4.font-medium.mb-2 "SLB0007"]
     [slb-forms-add/slb0007-form]]
    [:div
     [:h4.font-medium.mb-2 "SLB0008"]
     [slb-forms-add/slb0008-form]]]])

(defn slb-creditos []
  [:div
   [:h3.text-lg.font-semibold.mb-4 "Credit Operations (SLB0005)"]
   [:div
    [:h4.font-medium.mb-2 "SLB0005"]
    [slb-forms-add/slb0005-form]]])

(defn slb-consultar []
  [:div
   [:h3.text-lg.font-semibold.mb-4 "Query Operations (SLB0006)"]
   [slb-form/slb0006-form]])

(defn slb-historico []
  [:div
   [:h3.text-lg.font-semibold.mb-4 "Message History"]
   [slb-history/history-list]])

(defn slb-page []
  [:div.p-6
   [:h2.text-2xl.font-bold.mb-6 "SLB Messages"]

   ;; Submenu
   [:div.flex.gap-4.border-b.mb-6
    [submenu-button "Débitos" :debitos]
    [submenu-button "Créditos" :creditos]
    [submenu-button "Consultar" :consultar]
    [submenu-button "Histórico" :historico]]

   ;; Content
   [:div.bg-white.rounded.p-6
    (case (:current-submenu @page-state)
      :debitos [slb-debitos]
      :creditos [slb-creditos]
      :consultar [slb-consultar]
      :historico [slb-historico]
      [slb-debitos])]])
