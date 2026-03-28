(ns com.github.ebaptistella.wire.in.str.str0004-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0004
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-004"
   :body       "<STR0004><CodMsg>STR0004</CodMsg><NumCtrlIF>NC-004</NumCtrlIF><FinlddIF>00004</FinlddIF></STR0004>"})

(deftest str0004-wire-in-test
  (testing ":type is :STR0004 and finldd-if mapped"
    (let [m (parse-inbound raw-input)]
      (is (= :STR0004 (:type m)))
      (is (= "00004" (:finldd-if m))))))
