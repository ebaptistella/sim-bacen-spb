(ns com.github.ebaptistella.wire.in.str.str0020-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0020
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-020"
   :body       (str "<STR0020>"
                    "<CodMsg>STR0020</CodMsg>"
                    "<NumCtrlIF>NC-020</NumCtrlIF>"
                    "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                    "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                    "<VlrLanc>1000.00</VlrLanc>"
                    "<FinlddIF>00020</FinlddIF>"
                    "<DtMovto>20260327</DtMovto>"
                    "</STR0020>")})

(deftest str0020-wire-in-type-test
  (testing ":type is :STR0020"
    (is (= :STR0020 (:type (parse-inbound raw-input))))))

(deftest str0020-wire-in-fields-test
  (testing "basic fields mapped"
    (let [m (parse-inbound raw-input)]
      (is (= "NC-020" (:num-ctrl-if m)))
      (is (= "00020" (:finldd-if m)))
      (is (= "00000000" (:participant m))))))
