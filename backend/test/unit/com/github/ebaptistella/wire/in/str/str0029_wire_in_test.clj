(ns com.github.ebaptistella.wire.in.str.str0029-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0029
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-029"
   :body       "<STR0029><CodMsg>STR0029</CodMsg><NumCtrlIF>NC-029</NumCtrlIF><FinlddIF>00029</FinlddIF></STR0029>"})

(deftest str0029-wire-in-test
  (testing ":type is :STR0029 and finldd-if mapped"
    (let [m (parse-inbound raw-input)]
      (is (= :STR0029 (:type m)))
      (is (= "00029" (:finldd-if m))))))
