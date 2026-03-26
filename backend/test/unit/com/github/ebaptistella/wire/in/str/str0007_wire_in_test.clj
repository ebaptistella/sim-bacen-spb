(ns com.github.ebaptistella.wire.in.str.str0007-wire-in-test
  (:require [clojure.test :refer [deftest is testing]]
            com.github.ebaptistella.wire.in.str.str0007
            [com.github.ebaptistella.wire.in.str.str :refer [parse-inbound]]))

(def ^:private sample-xml
  "<STR0007><CodMsg>STR0007</CodMsg><NumCtrlIF>NC-007</NumCtrlIF><ISPBIFDebtd>00000000</ISPBIFDebtd><ISPBIFCredtd>11111111</ISPBIFCredtd><VlrLanc>4000.00</VlrLanc><FinlddIF>00001</FinlddIF><DtMovto>20260103</DtMovto></STR0007>")

(def ^:private raw-input
  {:queue-name "QL.REQ.00000000.99999999.01"
   :message-id "MSG-7"
   :body       sample-xml})

(deftest str0007-wire-in-finldd-if-test
  (testing ":finldd-if echoes FinlddIF from XML"
    (let [result (parse-inbound raw-input)]
      (is (= "00001" (:finldd-if result))))))

(deftest str0007-wire-in-no-finldd-cli-test
  (testing ":finldd-cli is not present in the result map for STR0007"
    (let [result (parse-inbound raw-input)]
      (is (not (contains? result :finldd-cli))))))

(deftest str0007-wire-in-type-test
  (testing ":type is STR0007"
    (let [result (parse-inbound raw-input)]
      (is (= "STR0007" (:type result))))))

(deftest str0007-wire-in-status-test
  (testing ":status is :pending"
    (let [result (parse-inbound raw-input)]
      (is (= :pending (:status result))))))
