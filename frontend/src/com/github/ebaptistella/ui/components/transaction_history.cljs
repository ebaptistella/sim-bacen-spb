(ns com.github.ebaptistella.ui.components.transaction-history
  "Transaction history section: loading, error, empty, table, load more. Presentational; caller passes state slice and callbacks."
  (:require [com.github.ebaptistella.ui.components.transaction-table :as transaction-table]))

(defn- refresh-icon
  [class-name]
  [:svg {:class       (str "w-5 h-5 " (or class-name ""))
         :fill        "none"
         :stroke      "currentColor"
         :viewBox     "0 0 24 24"
         :aria-hidden "true"}
   [:path {:stroke-linecap  "round"
           :stroke-linejoin "round"
           :stroke-width    "2"
           :d "M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"}]])

(defn transaction-history
  "Props: error, loading?, transactions, has-more?, on-retry (fn []), on-load-more (fn [])."
  [{:keys [error loading? transactions has-more? on-retry on-load-more]}]
  [:section.mb-10
   [:div.flex.items-center.gap-3.mb-5
    [:h2.text-2xl.text-gray-800 "Histórico de Transações"]
    [:button.p-1.5.rounded.text-gray-600.hover:bg-gray-200.hover:text-gray-800
     {:type     "button"
      :title    "Atualizar lista"
      :disabled loading?
      :on-click (fn [_] (when on-retry (on-retry)))}
     [refresh-icon (when loading? "animate-spin")]]]
   (cond
     error
     [:div.p-4.bg-red-100.text-red-800.rounded
      [:p error]
      [:button.mt-2.px-3.py-1.bg-red-200.rounded.hover:bg-red-300
       {:on-click (fn [_] (when on-retry (on-retry)))}
       "Tentar novamente"]]

     (and (empty? transactions) loading?)
     [:p.text-gray-600 "Carregando..."]

     (empty? transactions)
     [:p.text-gray-600 "Nenhuma transação encontrada."]

     :else
     [:<>
      [transaction-table/transaction-table transactions]
      (when has-more?
        [:button.mt-4.px-4.py-2.bg-gray-200.rounded.hover:bg-gray-300
         {:on-click (fn [_] (when on-load-more (on-load-more)))
          :disabled loading?}
         (if loading? "Carregando..." "Carregar mais")])])])
