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

(defn first-response
  "Returns the first response in :responses [], analogous to the old :response field."
  [msg]
  (first (:responses msg)))

(defn responses-of-type
  "Returns seq of responses matching rtype keyword from :responses []."
  [msg rtype]
  (filter #(= (:type %) rtype) (:responses msg)))

(defn response-sent?
  "Returns true if a response of the given type keyword was already sent."
  [msg rtype]
  (boolean (some #(= (:type %) rtype) (:responses msg))))

(defn get-by-num-ctrl-str
  "Returns the message whose first response contains the given NumCtrlSTR."
  [store-component num-ctrl-str]
  (when num-ctrl-str
    (->> (:messages @(:store store-component))
         (filter #(= (-> % :responses first :num-ctrl-str) num-ctrl-str))
         first)))

(defn find-by-num-ctrl-if
  "Returns the first message whose :num-ctrl-if equals num-ctrl-if, or nil if not found.
   NumCtrlIF is unique per IF per lançamento in the SPB protocol; O(n) scan is acceptable
   for the in-memory simulator. In case of collision, returns the earliest inserted entry (FIFO)."
  [store-component num-ctrl-if]
  (->> (:messages @(:store store-component))
       (filter #(= (:num-ctrl-if %) num-ctrl-if))
       first))

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
