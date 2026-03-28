(ns com.github.ebaptistella.wire.in.str.str0003-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0003
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-003"
   :body       "<STR0003><CodMsg>STR0003</CodMsg><NumCtrlIF>NC-003</NumCtrlIF><FinlddIF>00003</FinlddIF></STR0003>"})

(deftest str0003-wire-in-test
  (testing ":type is :STR0003 and finldd-if mapped"
    (let [m (parse-inbound raw-input)]
      (is (= :STR0003 (:type m)))
      (is (= "00003" (:finldd-if m))))))
