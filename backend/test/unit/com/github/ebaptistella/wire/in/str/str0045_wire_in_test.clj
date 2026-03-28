(ns com.github.ebaptistella.wire.in.str.str0045-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0045
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-045"
   :body       "<STR0045><CodMsg>STR0045</CodMsg><NumCtrlIF>NC-045</NumCtrlIF><FinlddIF>00045</FinlddIF></STR0045>"})

(deftest str0045-wire-in-test
  (testing ":type is :STR0045 and finldd-if mapped"
    (let [m (parse-inbound raw-input)]
      (is (= :STR0045 (:type m)))
      (is (= "00045" (:finldd-if m))))))
