(ns com.github.ebaptistella.wire.in.str.str0022-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0022
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-022"
   :body       "<STR0022><CodMsg>STR0022</CodMsg><NumCtrlIF>NC-022</NumCtrlIF><FinlddIF>00022</FinlddIF></STR0022>"})

(deftest str0022-wire-in-test
  (testing ":type is :STR0022 and finldd-if mapped"
    (let [m (parse-inbound raw-input)]
      (is (= :STR0022 (:type m)))
      (is (= "NC-022" (:num-ctrl-if m)))
      (is (= "00022" (:finldd-if m))))))
