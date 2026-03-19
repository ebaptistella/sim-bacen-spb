(ns com.github.ebaptistella.infrastructure.http-server.health
  (:require [com.github.ebaptistella.interface.http.response :as response]
            [schema.core :as s]))

(s/defn health-check [_request]
  (response/ok {:status "ok" :service "sim-bacen-spb"}))
