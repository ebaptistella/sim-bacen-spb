(ns com.github.ebaptistella.infrastructure.store.messages
  "Repository adapter: CRUD operations over the in-memory atom store.")

(defn save!
  [store-component msg]
  (swap! (:store store-component) update :messages conj msg)
  msg)

(defn list-messages
  [store-component {:keys [limit offset status] :or {limit 20 offset 0}}]
  (let [all      (:messages @(:store store-component))
        filtered (if (and status (not= status :all))
                   (filter #(= (:status %) status) all)
                   all)
        total    (count filtered)]
    {:messages (vec (take limit (drop offset filtered)))
     :total    total
     :limit    limit
     :offset   offset}))

(defn find-by-id
  [store-component id]
  (->> (:messages @(:store store-component))
       (filter #(= (:id %) id))
       first))

(defn update-message!
  [store-component id f]
  (swap! (:store store-component)
         update :messages
         (fn [msgs] (mapv (fn [m] (if (= (:id m) id) (f m) m)) msgs))))
