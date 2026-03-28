(ns com.github.ebaptistella.wire.in.str.str0052-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0052
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private sample-xml
  (str "<STR0052>"
       "<CodMsg>STR0052</CodMsg>"
       "<NumCtrlIF>NC-052</NumCtrlIF>"
       "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
       "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
       "<VlrLanc>12000.00</VlrLanc>"
       "<FinlddIF>00100</FinlddIF>"
       "<DtMovto>20260103</DtMovto>"
       "<TpCtDebtd>01</TpCtDebtd>"
       "<TpCtCredtd>05</TpCtCredtd>"
       "</STR0052>"))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-052"
   :body       sample-xml})

(deftest str0052-wire-in-type-test
  (testing ":type is :STR0052"
    (is (= :STR0052 (:type (parse-inbound raw-input))))))

(deftest str0052-wire-in-finldd-if-test
  (testing ":finldd-if echoes FinlddIF"
    (is (= "00100" (:finldd-if (parse-inbound raw-input))))))

(deftest str0052-wire-in-tp-ct-test
  (testing ":tp-ct-debtd echoes TpCtDebtd"
    (is (= "01" (:tp-ct-debtd (parse-inbound raw-input)))))
  (testing ":tp-ct-credtd echoes TpCtCredtd"
    (is (= "05" (:tp-ct-credtd (parse-inbound raw-input))))))
