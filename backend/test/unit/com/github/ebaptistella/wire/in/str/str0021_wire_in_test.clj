(ns com.github.ebaptistella.wire.in.str.str0021-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0021
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-021"
   :body       "<STR0021><CodMsg>STR0021</CodMsg><NumCtrlIF>NC-021</NumCtrlIF><FinlddIF>00021</FinlddIF></STR0021>"})

(deftest str0021-wire-in-test
  (testing ":type is :STR0021 and finldd-if mapped"
    (let [m (parse-inbound raw-input)]
      (is (= :STR0021 (:type m)))
      (is (= "NC-021" (:num-ctrl-if m)))
      (is (= "00021" (:finldd-if m))))))
