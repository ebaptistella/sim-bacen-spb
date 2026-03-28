(ns com.github.ebaptistella.wire.in.str.str0025-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0025
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private sample-xml
  (str "<STR0025>"
       "<CodMsg>STR0025</CodMsg>"
       "<NumCtrlIF>NC-025</NumCtrlIF>"
       "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
       "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
       "<VlrLanc>5000.00</VlrLanc>"
       "<FinlddCli>0017</FinlddCli>"
       "<DtMovto>20260103</DtMovto>"
       "<TpCtDebtd>01</TpCtDebtd>"
       "<TpCtCredtd>05</TpCtCredtd>"
       "<Agencia>1234</Agencia>"
       "<CtPgto>0000123456789</CtPgto>"
       "<Hist>DEPOSITO JUDICIAL</Hist>"
       "</STR0025>"))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-025"
   :body       sample-xml})

(deftest str0025-wire-in-type-test
  (testing ":type is :STR0025"
    (is (= :STR0025 (:type (parse-inbound raw-input))))))

(deftest str0025-wire-in-status-test
  (testing ":status is :pending"
    (is (= :pending (:status (parse-inbound raw-input))))))

(deftest str0025-wire-in-direction-test
  (testing ":direction is :inbound"
    (is (= :inbound (:direction (parse-inbound raw-input))))))

(deftest str0025-wire-in-finldd-cli-test
  (testing ":finldd-cli echoes FinlddCli from XML"
    (is (= "0017" (:finldd-cli (parse-inbound raw-input))))))

(deftest str0025-wire-in-tp-ct-test
  (testing ":tp-ct-debtd echoes TpCtDebtd from XML"
    (is (= "01" (:tp-ct-debtd (parse-inbound raw-input)))))
  (testing ":tp-ct-credtd echoes TpCtCredtd from XML"
    (is (= "05" (:tp-ct-credtd (parse-inbound raw-input))))))

(deftest str0025-wire-in-agencia-test
  (testing ":agencia echoes Agencia from XML"
    (is (= "1234" (:agencia (parse-inbound raw-input))))))

(deftest str0025-wire-in-ct-pgto-test
  (testing ":ct-pgto echoes CtPgto from XML"
    (is (= "0000123456789" (:ct-pgto (parse-inbound raw-input))))))

(deftest str0025-wire-in-hist-test
  (testing ":hist echoes Hist from XML"
    (is (= "DEPOSITO JUDICIAL" (:hist (parse-inbound raw-input))))))
