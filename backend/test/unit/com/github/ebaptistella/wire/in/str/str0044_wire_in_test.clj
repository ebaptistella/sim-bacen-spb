(ns com.github.ebaptistella.wire.in.str.str0044-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0044
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-044"
   :body       "<STR0044><CodMsg>STR0044</CodMsg><NumCtrlIF>NC-044</NumCtrlIF><ISPBIFDebtd>00000000</ISPBIFDebtd><DtMovto>20260327</DtMovto></STR0044>"})

(deftest str0044-wire-in-type-test
  (testing ":type is :STR0044"
    (is (= :STR0044 (:type (parse-inbound raw-input))))))

(deftest str0044-wire-in-fields-test
  (testing "Fluxo1 fields mapped (no finldd-if)"
    (let [m (parse-inbound raw-input)]
      (is (= "NC-044" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (nil? (:finldd-if m))))))
