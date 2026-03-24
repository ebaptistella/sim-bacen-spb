(ns com.github.ebaptistella.frontend.core
  (:require [com.github.ebaptistella.frontend.events]
            [com.github.ebaptistella.frontend.pages.messages :as messages]
            [com.github.ebaptistella.frontend.subs]
            [com.github.ebaptistella.frontend.util.router :as router]
            [re-frame.core :as rf]
            [reagent.dom :as rdom]))

(defn app []
  (let [page @(rf/subscribe [:router/current-page])]
    (case page
      :messages       [messages/messages-page]
      :message-detail [messages/messages-page]
      [messages/messages-page])))

(defn mount-root []
  (when-let [el (.getElementById js/document "app")]
    (rdom/render [app] el)))

(defn ^:export init []
  (rf/dispatch-sync [:initialize-db])
  (router/init!)
  (mount-root))
