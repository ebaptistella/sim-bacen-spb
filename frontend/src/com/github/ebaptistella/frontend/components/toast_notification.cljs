(ns com.github.ebaptistella.frontend.components.toast-notification
  (:require [re-frame.core :as rf]))

(defn toast []
  (let [current @(rf/subscribe [:toast/current-toast])]
    (when current
      (let [success? (= (:type current) :success)]
        [:div {:class (str "fixed top-4 right-4 z-50 max-w-sm w-full shadow-lg rounded-lg pointer-events-auto
                            transition-all transform "
                           (if success?
                             "bg-green-500"
                             "bg-red-500"))}
         [:div.flex.items-center.justify-between.p-4
          [:p.text-sm.font-medium.text-white (:message current)]
          [:button {:class    "ml-4 text-white hover:text-gray-200 focus:outline-none"
                    :on-click #(rf/dispatch [:toast/dismiss])}
           [:svg {:xmlns "http://www.w3.org/2000/svg" :class "h-4 w-4" :viewBox "0 0 20 20" :fill "currentColor"}
            [:path {:fill-rule "evenodd"
                    :d "M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                    :clip-rule "evenodd"}]]]]]))))
