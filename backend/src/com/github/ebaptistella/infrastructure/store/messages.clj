(ns com.github.ebaptistella.infrastructure.store.messages
  "Repository adapter: CRUD operations over the in-memory atom store."
  (:import [java.time Instant LocalDate LocalTime ZoneOffset]))

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

(defn get-by-dt-movto
  "Returns all inbound messages received on the given date.
   dt-str format: 'YYYYMMDD' (e.g. '20260324')."
  [store-component dt-str]
  (when dt-str
    (let [all         (:messages @(:store store-component))
          target-date (LocalDate/parse
                       (str (subs dt-str 0 4) "-"
                            (subs dt-str 4 6) "-"
                            (subs dt-str 6 8)))]
      (filter (fn [msg]
                (when-let [ra (:received-at msg)]
                  (let [instant  (Instant/parse ra)
                        msg-date (.toLocalDate (.atZone instant ZoneOffset/UTC))]
                    (= msg-date target-date))))
              all))))

(defn get-by-num-ctrl-str
  "Returns the message whose response contains the given NumCtrlSTR."
  [store-component num-ctrl-str]
  (when num-ctrl-str
    (->> (:messages @(:store store-component))
         (filter #(= (get-in % [:response :num-ctrl-str]) num-ctrl-str))
         first)))

(defn get-by-period
  "Returns messages received on dt-str, optionally filtered by hour range.
   dt-str: 'YYYYMMDD'. hr-ini and hr-fim: 'HH:MM' (optional)."
  [store-component dt-str hr-ini hr-fim]
  (when dt-str
    (let [all         (:messages @(:store store-component))
          target-date (LocalDate/parse
                       (str (subs dt-str 0 4) "-"
                            (subs dt-str 4 6) "-"
                            (subs dt-str 6 8)))
          parse-hhmm  (fn [hhmm]
                        (when hhmm
                          (let [[h m] (clojure.string/split hhmm #":")]
                            (LocalTime/of (Integer/parseInt h)
                                          (Integer/parseInt m)))))]
      (filter (fn [msg]
                (when-let [ra (:received-at msg)]
                  (let [instant  (Instant/parse ra)
                        zdt      (.atZone instant ZoneOffset/UTC)
                        msg-date (.toLocalDate zdt)
                        msg-time (.toLocalTime zdt)]
                    (and (= msg-date target-date)
                         (or (nil? hr-ini)
                             (not (.isBefore msg-time (parse-hhmm hr-ini))))
                         (or (nil? hr-fim)
                             (not (.isAfter msg-time (parse-hhmm hr-fim))))))))
              all))))
