(ns com.github.ebaptistella.interceptors.components
  (:require [io.pedestal.http :as http.server]
            [schema.core :as s]))

(s/defn ^:private get-system
  [request]
  (get-in request [::http.server/context :system]))

(s/defn get-component
  "Gets a component from the system by key."
  [request component-key]
  (get (get-system request) component-key))
