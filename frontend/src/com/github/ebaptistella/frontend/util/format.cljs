(ns com.github.ebaptistella.frontend.util.format)

(defn- normalize-date-str [s]
  ;; "20260323" → "2026-03-23"
  (if (re-matches #"\d{8}" s)
    (str (subs s 0 4) "-" (subs s 4 6) "-" (subs s 6 8))
    s))

(defn format-date [date-str]
  (when date-str
    (let [d   (js/Date. (normalize-date-str date-str))
          pad #(.padStart (str %) 2 "0")]
      (if (js/isNaN (.getTime d))
        date-str
        (str (pad (.getDate d)) "/"
             (pad (inc (.getMonth d))) "/"
             (.getFullYear d) " "
             (pad (.getHours d)) ":"
             (pad (.getMinutes d)))))))

(defn format-currency [value]
  (when value
    (let [n (if (string? value) (js/parseFloat value) value)]
      (when-not (js/isNaN n)
        (.toLocaleString n "pt-BR" #js {:minimumFractionDigits 2
                                        :maximumFractionDigits 2})))))
