(ns com.github.ebaptistella.wire.in.str.str0041-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0041
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private sample-xml
  (str "<STR0041>"
       "<CodMsg>STR0041</CodMsg>"
       "<NumCtrlIF>NC-041</NumCtrlIF>"
       "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
       "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
       "<VlrLanc>9000.00</VlrLanc>"
       "<FinlddCli>0013</FinlddCli>"
       "<DtMovto>20260103</DtMovto>"
       "<TpCtDebtd>01</TpCtDebtd>"
       "<TpCtCredtd>05</TpCtCredtd>"
       "<Agencia>4321</Agencia>"
       "</STR0041>"))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-041"
   :body       sample-xml})

(deftest str0041-wire-in-type-test
  (testing ":type is :STR0041"
    (is (= :STR0041 (:type (parse-inbound raw-input))))))

(deftest str0041-wire-in-finldd-cli-test
  (testing ":finldd-cli echoes FinlddCli"
    (is (= "0013" (:finldd-cli (parse-inbound raw-input))))))

(deftest str0041-wire-in-tp-ct-test
  (testing ":tp-ct-debtd echoes TpCtDebtd"
    (is (= "01" (:tp-ct-debtd (parse-inbound raw-input)))))
  (testing ":tp-ct-credtd echoes TpCtCredtd"
    (is (= "05" (:tp-ct-credtd (parse-inbound raw-input))))))

(deftest str0041-wire-in-agencia-test
  (testing ":agencia echoes Agencia"
    (is (= "4321" (:agencia (parse-inbound raw-input))))))
