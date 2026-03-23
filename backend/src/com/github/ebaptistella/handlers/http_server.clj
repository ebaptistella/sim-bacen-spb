(ns com.github.ebaptistella.handlers.http-server
  (:require [com.github.ebaptistella.handlers.routes.health :as routes.health]
            [com.github.ebaptistella.handlers.routes.messages :as routes.messages]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [schema.core :as s]))

(s/defn ^:private combine-routes []
  (set (concat routes.health/routes
               routes.messages/routes)))

(def routes
  (route/expand-routes (combine-routes)))

(def server-config
  (merge {::http/type      :jetty
          ::http/routes    routes
          ::http/resource-path "/public"
          ::http/container-options {}}
         {::http/join? false}))
