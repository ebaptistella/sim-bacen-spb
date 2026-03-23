(ns com.github.ebaptistella.components.store
  (:require [com.stuartsierra.component :as component]
            [schema.core :as s]))

(defrecord StoreComponent [store]
  component/Lifecycle
  (start [this]
    (if store
      this
      (assoc this :store (atom {:messages []}))))
  (stop [this]
    (dissoc this :store)))

(s/defn new-store []
  (map->StoreComponent {}))
