(ns com.github.ebaptistella.frontend.pages.messages
  (:require [com.github.ebaptistella.frontend.components.confirmation-modal :as confirmation]
            [com.github.ebaptistella.frontend.components.message-detail-panel :as detail]
            [com.github.ebaptistella.frontend.components.message-list :as list]
            [com.github.ebaptistella.frontend.components.respond-modal :as respond]
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
         [:header.bg-gradient-to-r.from-indigo-500.to-purple-600.text-white.p-6.text-center
          [:h1.text-3xl.md:text-4xl.font-bold.mb-1 "Simulador BACEN"]
          [:p.text-base.opacity-90 "Sistema de Pagamentos Brasileiro"]]
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
         [confirmation/confirmation-modal]]))}))
