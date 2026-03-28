(ns com.github.ebaptistella.wire.in.str.str0033-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0033
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-033"
   :body       "<STR0033><CodMsg>STR0033</CodMsg><NumCtrlIF>NC-033</NumCtrlIF><FinlddIF>00033</FinlddIF></STR0033>"})

(deftest str0033-wire-in-test
  (testing ":type is :STR0033 and finldd-if mapped"
    (let [m (parse-inbound raw-input)]
      (is (= :STR0033 (:type m)))
      (is (= "00033" (:finldd-if m))))))
