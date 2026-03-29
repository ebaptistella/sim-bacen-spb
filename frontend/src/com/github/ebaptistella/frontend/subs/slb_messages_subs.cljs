(ns com.github.ebaptistella.frontend.subs.slb-messages-subs
  (:require [re-frame.core :as rf]
            [clojure.string :as str]))

(rf/reg-sub
 :slb-messages/for-selected
 :<- [:messages/selected-id]
 :<- [:messages/all]
 (fn [[selected-id messages] _]
   (when selected-id
     (first (filter #(= (:id %) selected-id) messages)))))

(rf/reg-sub
 :slb-messages/response-for-selected
 :<- [:messages/selected-id]
 :<- [:messages/all]
 (fn [[selected-id messages] _]
   (when selected-id
     (let [selected (first (filter #(= (:id %) selected-id) messages))
           num-ctrl-part (:num-ctrl-part selected)]
       (when num-ctrl-part
         (->> messages
              (filter #(and (str/ends-with? (:type %) "R1")
                           (= (:num-ctrl-part %) num-ctrl-part)))
              first))))))

(rf/reg-sub
 :slb-messages/show-response-indicator?
 :<- [:slb-messages/response-for-selected]
 (fn [response _]
   (boolean response)))
