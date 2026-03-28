(ns com.github.ebaptistella.wire.in.str.str0034-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0034
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private sample-xml
  (str "<STR0034>"
       "<CodMsg>STR0034</CodMsg>"
       "<NumCtrlIF>NC-034</NumCtrlIF>"
       "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
       "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
       "<VlrLanc>6000.00</VlrLanc>"
       "<FinlddIF>00006</FinlddIF>"
       "<DtMovto>20260103</DtMovto>"
       "<TpCtDebtd>01</TpCtDebtd>"
       "<TpCtCredtd>05</TpCtCredtd>"
       "</STR0034>"))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-034"
   :body       sample-xml})

(deftest str0034-wire-in-type-test
  (testing ":type is :STR0034"
    (is (= :STR0034 (:type (parse-inbound raw-input))))))

(deftest str0034-wire-in-status-test
  (testing ":status is :pending"
    (is (= :pending (:status (parse-inbound raw-input))))))

(deftest str0034-wire-in-finldd-if-test
  (testing ":finldd-if echoes FinlddIF from XML"
    (is (= "00006" (:finldd-if (parse-inbound raw-input))))))

(deftest str0034-wire-in-tp-ct-test
  (testing ":tp-ct-debtd echoes TpCtDebtd"
    (is (= "01" (:tp-ct-debtd (parse-inbound raw-input)))))
  (testing ":tp-ct-credtd echoes TpCtCredtd"
    (is (= "05" (:tp-ct-credtd (parse-inbound raw-input))))))
