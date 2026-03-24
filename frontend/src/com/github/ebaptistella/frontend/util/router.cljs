(ns com.github.ebaptistella.frontend.util.router
  (:require [clojure.string :as str]
            [re-frame.core :as rf]))

(defn- parse-hash []
  (let [hash (.-hash js/location)
        path (if (str/blank? hash) "/messages" (subs hash 1))]
    (cond
      (re-matches #"/messages/(.+)" path)
      (let [[_ id] (re-matches #"/messages/(.+)" path)]
        {:page :message-detail :id id})

      (= path "/messages")
      {:page :messages}

      ;; Unknown routes → redirect to /messages (default landing page)
      :else
      {:page :messages})))

(defn init! []
  (rf/dispatch [:router/navigate (parse-hash)])
  (.addEventListener js/window "hashchange"
                     (fn [_] (rf/dispatch [:router/navigate (parse-hash)]))))

(defn nav-to-messages! []
  (set! (.-hash js/location) "/messages"))

(defn nav-to-detail! [id]
  (set! (.-hash js/location) (str "/messages/" id)))
