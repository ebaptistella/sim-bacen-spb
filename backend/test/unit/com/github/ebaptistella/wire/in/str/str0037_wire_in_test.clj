(ns com.github.ebaptistella.wire.in.str.str0037-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0037
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private sample-xml
  (str "<STR0037>"
       "<CodMsg>STR0037</CodMsg>"
       "<NumCtrlIF>NC-037</NumCtrlIF>"
       "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
       "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
       "<VlrLanc>7000.00</VlrLanc>"
       "<FinlddCli>0016</FinlddCli>"
       "<DtMovto>20260103</DtMovto>"
       "<TpCtDebtd>01</TpCtDebtd>"
       "<TpCtCredtd>05</TpCtCredtd>"
       "<Agencia>5678</Agencia>"
       "<CtPgto>0000987654321</CtPgto>"
       "</STR0037>"))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-037"
   :body       sample-xml})

(deftest str0037-wire-in-type-test
  (testing ":type is :STR0037"
    (is (= :STR0037 (:type (parse-inbound raw-input))))))

(deftest str0037-wire-in-finldd-cli-test
  (testing ":finldd-cli echoes FinlddCli"
    (is (= "0016" (:finldd-cli (parse-inbound raw-input))))))

(deftest str0037-wire-in-tp-ct-test
  (testing ":tp-ct-debtd echoes TpCtDebtd"
    (is (= "01" (:tp-ct-debtd (parse-inbound raw-input)))))
  (testing ":tp-ct-credtd echoes TpCtCredtd"
    (is (= "05" (:tp-ct-credtd (parse-inbound raw-input))))))

(deftest str0037-wire-in-agencia-ct-pgto-test
  (testing ":agencia echoes Agencia"
    (is (= "5678" (:agencia (parse-inbound raw-input)))))
  (testing ":ct-pgto echoes CtPgto"
    (is (= "0000987654321" (:ct-pgto (parse-inbound raw-input))))))
