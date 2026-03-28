(ns com.github.ebaptistella.wire.in.str.str0047-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0047
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private sample-xml
  (str "<STR0047>"
       "<CodMsg>STR0047</CodMsg>"
       "<NumCtrlIF>NC-047</NumCtrlIF>"
       "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
       "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
       "<VlrLanc>10000.00</VlrLanc>"
       "<FinlddIF>00098</FinlddIF>"
       "<DtMovto>20260103</DtMovto>"
       "<TpCtDebtd>01</TpCtDebtd>"
       "<TpCtCredtd>05</TpCtCredtd>"
       "</STR0047>"))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-047"
   :body       sample-xml})

(deftest str0047-wire-in-type-test
  (testing ":type is :STR0047"
    (is (= :STR0047 (:type (parse-inbound raw-input))))))

(deftest str0047-wire-in-finldd-if-test
  (testing ":finldd-if echoes FinlddIF"
    (is (= "00098" (:finldd-if (parse-inbound raw-input))))))

(deftest str0047-wire-in-tp-ct-test
  (testing ":tp-ct-debtd echoes TpCtDebtd"
    (is (= "01" (:tp-ct-debtd (parse-inbound raw-input)))))
  (testing ":tp-ct-credtd echoes TpCtCredtd"
    (is (= "05" (:tp-ct-credtd (parse-inbound raw-input))))))
