(ns com.github.ebaptistella.ui.adapters
  "Adapters for wire (API JSON) <-> internal models. Pure data only, no I/O.")

;; ---- Submit transaction (POST /api/v1/transactions) ----

(defn wire->submit-response
  "API returns body e.g. {\"id\":\"abc-123\"}. data = that body (map) -> {:id string} or nil."
  [data]
  (when (map? data)
    (when-let [id (or (:id data) (get data "id"))]
      {:id (str id)})))

(defn wire->error-message
  "Wire (4xx/5xx body) -> error string for UI. Returns nil for empty body."
  [data]
  (when (map? data)
    (let [msg (or (:error data) (:message data)
                  (when (seq data) (str data)))]
      (when (and msg (not= msg "{}"))
        msg))))

;; ---- Transaction history (GET /api/v1/transactions). Backend returns {items: [...], limit, offset}. ----

(defn- get-kw-or-str
  "Gets value from map by keyword or string key."
  [m k]
  (or (get m k) (get m (name k))))

(defn wire->transaction-item
  "Single transaction wire -> model {:id :tipo :valor :origem :destino :status :created-at}."
  [w]
  (when (map? w)
    {:id         (or (get-kw-or-str w :id) "")
     :tipo       (or (get-kw-or-str w :tipo) (get-kw-or-str w :type) "")
     :valor      (or (get-kw-or-str w :valor) (get-kw-or-str w :amount) "")
     :origem     (or (get-kw-or-str w :origem) (get-kw-or-str w :source) "")
     :destino    (or (get-kw-or-str w :destino) (get-kw-or-str w :destination) "")
     :status     (or (get-kw-or-str w :status) "pending")
     :created-at (or (get-kw-or-str w :created-at) (get-kw-or-str w :created_at) "")}))

(defn wire->transaction-list
  "Wire (200 body with :items or \"items\") -> vector of transaction models."
  [data]
  (when (map? data)
    (let [items (or (get data :items) (get data "items"))]
      (if (sequential? items)
        (mapv wire->transaction-item items)
        []))))
