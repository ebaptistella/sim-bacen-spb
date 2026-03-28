(ns com.github.ebaptistella.wire.in.str.str0043-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0043
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-043"
   :body       "<STR0043><CodMsg>STR0043</CodMsg><NumCtrlIF>NC-043</NumCtrlIF><ISPBIFDebtd>00000000</ISPBIFDebtd><DtMovto>20260327</DtMovto></STR0043>"})

(deftest str0043-wire-in-type-test
  (testing ":type is :STR0043"
    (is (= :STR0043 (:type (parse-inbound raw-input))))))

(deftest str0043-wire-in-fields-test
  (testing "Fluxo1 fields mapped (no finldd-if)"
    (let [m (parse-inbound raw-input)]
      (is (= "NC-043" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (nil? (:finldd-if m))))))
