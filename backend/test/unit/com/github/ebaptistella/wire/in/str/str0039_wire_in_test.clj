(ns com.github.ebaptistella.wire.in.str.str0039-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0039
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private sample-xml
  (str "<STR0039>"
       "<CodMsg>STR0039</CodMsg>"
       "<NumCtrlIF>NC-039</NumCtrlIF>"
       "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
       "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
       "<VlrLanc>8000.00</VlrLanc>"
       "<FinlddIF>00099</FinlddIF>"
       "<DtMovto>20260103</DtMovto>"
       "<TpCtDebtd>02</TpCtDebtd>"
       "<TpCtCredtd>03</TpCtCredtd>"
       "</STR0039>"))

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-039"
   :body       sample-xml})

(deftest str0039-wire-in-type-test
  (testing ":type is :STR0039"
    (is (= :STR0039 (:type (parse-inbound raw-input))))))

(deftest str0039-wire-in-finldd-if-test
  (testing ":finldd-if echoes FinlddIF"
    (is (= "00099" (:finldd-if (parse-inbound raw-input))))))

(deftest str0039-wire-in-tp-ct-test
  (testing ":tp-ct-debtd echoes TpCtDebtd"
    (is (= "02" (:tp-ct-debtd (parse-inbound raw-input)))))
  (testing ":tp-ct-credtd echoes TpCtCredtd"
    (is (= "03" (:tp-ct-credtd (parse-inbound raw-input))))))
