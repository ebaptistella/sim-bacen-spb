(ns com.github.ebaptistella.wire.in.str.str0040-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0040
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-040"
   :body       "<STR0040><CodMsg>STR0040</CodMsg><NumCtrlIF>NC-040</NumCtrlIF><FinlddIF>00040</FinlddIF></STR0040>"})

(deftest str0040-wire-in-test
  (testing ":type is :STR0040 and finldd-if mapped"
    (let [m (parse-inbound raw-input)]
      (is (= :STR0040 (:type m)))
      (is (= "00040" (:finldd-if m))))))
