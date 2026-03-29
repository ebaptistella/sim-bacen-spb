(ns com.github.ebaptistella.frontend.pages.messages
  (:require [com.github.ebaptistella.frontend.components.broadcast-form :as broadcast]
            [com.github.ebaptistella.frontend.components.confirmation-modal :as confirmation]
            [com.github.ebaptistella.frontend.components.message-detail-panel :as detail]
            [com.github.ebaptistella.frontend.components.message-list :as list]
            [com.github.ebaptistella.frontend.components.respond-modal :as respond]
            [com.github.ebaptistella.frontend.components.slb-form :as slb-form]
            [com.github.ebaptistella.frontend.components.slb-response-modal :as slb-response]
            [com.github.ebaptistella.frontend.components.toast-notification :as toast]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(defn messages-page []
  (r/create-class
   {:display-name "messages-page"

    :component-did-mount
    (fn [_]
      (rf/dispatch [:messages/fetch-initial])
      (rf/dispatch [:messages/start-polling]))

    :reagent-render
    (fn []
      (let [selected-id @(rf/subscribe [:messages/selected-id])]
        [:div.min-h-screen.bg-gray-100
         [toast/toast]
         [:header.bg-gradient-to-r.from-indigo-500.to-purple-600.text-white.p-6
          [:div.flex.items-center.justify-between.max-w-7xl.mx-auto
           [:div.text-center.flex-1
            [:h1.text-3xl.md:text-4xl.font-bold.mb-1 "Simulador BACEN"]
            [:p.text-base.opacity-90 "Sistema de Pagamentos Brasileiro"]]
           [:div.flex-shrink-0.flex.gap-2
            [:button {:class    "bg-white text-indigo-600 px-4 py-2 rounded-lg font-medium hover:bg-indigo-50 transition-colors text-sm"
                      :on-click #(rf/dispatch [:outbound/open-modal])}
             "Enviar Mensagem BACEN"]
            [:button {:class    "bg-purple-400 text-white px-4 py-2 rounded-lg font-medium hover:bg-purple-500 transition-colors text-sm"
                      :on-click #(rf/dispatch [:slb-form/open])}
             "Enviar Mensagem SLB"]]]]
         [:main {:class (str "max-w-7xl mx-auto p-4 "
                             (when selected-id
                               "sm:grid sm:grid-cols-5 sm:gap-4"))}
          [:section {:class (if selected-id "sm:col-span-3" "")}
           [:div.bg-white.rounded-lg.shadow.overflow-hidden
            [list/message-list]]]
          (when selected-id
            [:section.sm:col-span-2
             [:div.bg-white.rounded-lg.shadow.overflow-hidden
              [detail/message-detail-panel]]])]
         [respond/respond-modal]
         [broadcast/broadcast-form]
         [slb-form/slb-form]
         [slb-response/slb-response-modal]
         [confirmation/confirmation-modal]]))}))
