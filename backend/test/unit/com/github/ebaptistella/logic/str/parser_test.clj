(ns com.github.ebaptistella.logic.str.parser-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.parser :as parser]))

(deftest parse-fields-test
  (testing "header and transfer fields present"
    (let [xml (str "<CodMsg>STR0008</CodMsg>"
                   "<NumCtrlIF>ABC123</NumCtrlIF>"
                   "<NumCtrlSTR>NSTR01</NumCtrlSTR>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>1500.00</VlrLanc>"
                   "<FinlddCli>01</FinlddCli>"
                   "<DtMovto>20240115</DtMovto>")
          m   (parser/parse-fields xml)]
      (is (= "STR0008" (:cod-msg m)))
      (is (= "ABC123" (:num-ctrl-if m)))
      (is (= "NSTR01" (:num-ctrl-str m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "1500.00" (:vlr-lanc m)))
      (is (= "01" (:finldd-cli m)))
      (is (= "20240115" (:dt-movto m)))))
  (testing "missing field returns nil"
    (let [m (parser/parse-fields "<CodMsg>STR0008</CodMsg><NumCtrlIF>X</NumCtrlIF>")]
      (is (nil? (:num-ctrl-str m)))
      (is (nil? (:ispb-if-credtd m)))))
  (testing "nil body — all fields nil"
    (let [m (parser/parse-fields nil)]
      (is (every? nil? (vals m)))
      (is (contains? m :cod-msg))
      (is (contains? m :vlr-lanc))))
  (testing "empty body — all fields nil"
    (let [m (parser/parse-fields "")]
      (is (every? nil? (vals m))))))

(deftest r1-outbound-queue-test
  (testing "replaces QL prefix with QR"
    (is (= "QR.REQ.00000000.99999999.01"
           (parser/r1-outbound-queue "QL.REQ.00000000.99999999.01")))
    (is (= "QR.RSP.00000000.99999999.02"
           (parser/r1-outbound-queue "QL.RSP.00000000.99999999.02")))))

(deftest r2-outbound-queue-test
  (testing "replaces sender segment (index 2) with ISPBIFCredtd"
    (is (= "QR.REQ.11111111.99999999.01"
           (parser/r2-outbound-queue "QL.REQ.00000000.99999999.01" "11111111"))))
  (testing "nil ispb-if-credtd returns nil"
    (is (nil? (parser/r2-outbound-queue "QL.REQ.00000000.99999999.01" nil)))))
