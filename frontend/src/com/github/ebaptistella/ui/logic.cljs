(ns com.github.ebaptistella.ui.logic
  "Pure business logic for the frontend. No I/O, no Reagent."
  (:require [clojure.string :as str]))

(defn form-fields-valid?
  "True if all required transaction fields are non-blank."
  [{:keys [tipo valor origem destino]}]
  (and (not (str/blank? (str tipo)))
       (not (str/blank? (str valor)))
       (not (str/blank? (str origem)))
       (not (str/blank? (str destino)))))

(defn success-message
  "Message string for successful transaction submission. id can be string or nil."
  [id]
  (if id
    (str "Transação enviada com sucesso. ID: " id)
    "Transação enviada com sucesso."))

(defn format-valor
  "Formats a numeric string as BRL currency display. Returns original string if not parseable."
  [valor-str]
  (let [v (js/parseFloat (str/replace (str valor-str) "," "."))]
    (if (js/isNaN v)
      (str valor-str)
      (.toLocaleString v "pt-BR" #js {:style "currency" :currency "BRL"}))))

(defn status-label
  "Human-readable label for a transaction status."
  [status]
  (case (str/lower-case (or (str status) ""))
    "pending"   "Pendente"
    "processed" "Processado"
    "failed"    "Falhou"
    "queued"    "Na fila"
    (or (str status) "Desconhecido")))
