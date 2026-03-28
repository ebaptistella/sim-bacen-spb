(ns com.github.ebaptistella.wire.in.str.str0051-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0051
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private sample-xml
  (str "<STR0051>"
       "<CodMsg>STR0051</CodMsg>"
       "<NumCtrlIF>NC-051</NumCtrlIF>"
       "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
       "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
       "<VlrLanc>11000.00</VlrLanc>"
       "<FinlddCli>0017</FinlddCli>"
       "<DtMovto>20260103</DtMovto>"
       "<TpCtDebtd>01</TpCtDebtd>"
       "<TpCtCredtd>05</TpCtCredtd>"
       "<Hist>LIBERACAO JUDICIAL</Hist>"
       "</STR0051>"))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-051"
   :body       sample-xml})

(deftest str0051-wire-in-type-test
  (testing ":type is :STR0051"
    (is (= :STR0051 (:type (parse-inbound raw-input))))))

(deftest str0051-wire-in-finldd-cli-test
  (testing ":finldd-cli echoes FinlddCli"
    (is (= "0017" (:finldd-cli (parse-inbound raw-input))))))

(deftest str0051-wire-in-tp-ct-test
  (testing ":tp-ct-debtd echoes TpCtDebtd"
    (is (= "01" (:tp-ct-debtd (parse-inbound raw-input)))))
  (testing ":tp-ct-credtd echoes TpCtCredtd"
    (is (= "05" (:tp-ct-credtd (parse-inbound raw-input))))))

(deftest str0051-wire-in-hist-test
  (testing ":hist echoes Hist from XML"
    (is (= "LIBERACAO JUDICIAL" (:hist (parse-inbound raw-input))))))
