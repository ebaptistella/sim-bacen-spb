(ns com.github.ebaptistella.ui.models
  "Data models and application state structure. Single state atom; accessor fns keep consumers decoupled from shape.")

(def initial-state
  "Initial application state."
  {:form {:tipo "" :valor "" :origem "" :destino ""}
   :form-loading?       false
   :form-message        nil
   :form-message-type   nil
   :transactions        []
   :transactions-offset 0
   :transactions-loading? false
   :transactions-error  nil
   :transactions-has-more true})

(defn form-data
  [state]
  (get state :form {:tipo "" :valor "" :origem "" :destino ""}))

(defn form-loading?
  [state]
  (:form-loading? state))

(defn form-message
  [state]
  (:form-message state))

(defn form-message-type
  [state]
  (:form-message-type state))

(defn transactions
  [state]
  (:transactions state))

(defn transactions-loading?
  [state]
  (:transactions-loading? state))

(defn transactions-error
  [state]
  (:transactions-error state))

(defn transactions-has-more?
  [state]
  (:transactions-has-more state))

(defn transactions-offset
  [state]
  (:transactions-offset state))

(def tipos-transacao
  "Tipos de transação suportados pelo SPB."
  [{:value "TED" :label "TED - Transferência Eletrônica Disponível"}
   {:value "DOC" :label "DOC - Documento de Ordem de Crédito"}
   {:value "TEC" :label "TEC - Transferência Especial de Crédito"}
   {:value "PIX" :label "PIX - Pagamento Instantâneo"}])
