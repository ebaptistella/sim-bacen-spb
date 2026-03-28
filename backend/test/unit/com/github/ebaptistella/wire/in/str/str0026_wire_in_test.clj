(ns com.github.ebaptistella.wire.in.str.str0026-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0026
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-026"
   :body       "<STR0026><CodMsg>STR0026</CodMsg><NumCtrlIF>NC-026</NumCtrlIF><FinlddIF>00026</FinlddIF></STR0026>"})

(deftest str0026-wire-in-test
  (testing ":type is :STR0026 and finldd-if mapped"
    (let [m (parse-inbound raw-input)]
      (is (= :STR0026 (:type m)))
      (is (= "00026" (:finldd-if m))))))
