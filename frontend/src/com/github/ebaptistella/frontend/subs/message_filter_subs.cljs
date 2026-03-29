(ns com.github.ebaptistella.frontend.subs.message-filter-subs
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(rf/reg-sub
 :message-filter/type
 (fn [db _]
   (get-in db [:message-filter :type] "all")))

(rf/reg-sub
 :messages/filtered-list
 :<- [:messages/list]
 :<- [:message-filter/type]
 (fn [[messages filter-type] _]
   (case filter-type
     "str" (filter #(str/starts-with? (:type %) "STR") messages)
     "slb" (filter #(str/starts-with? (:type %) "SLB") messages)
     messages)))
