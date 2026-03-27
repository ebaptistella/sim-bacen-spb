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
  (testing "swaps sender/recipient and changes QL to QR"
    (is (= "QR.REQ.99999999.00000000.01"
           (parser/r1-outbound-queue "QL.REQ.00000000.99999999.01")))
    (is (= "QR.RSP.99999999.00000000.02"
           (parser/r1-outbound-queue "QL.RSP.00000000.99999999.02")))))

(deftest r2-outbound-queue-test
  (testing "builds from r1 queue and replaces reader slot with ISPBIFCredtd"
    (is (= "QR.REQ.99999999.11111111.01"
           (parser/r2-outbound-queue "QL.REQ.00000000.99999999.01" "11111111"))))
  (testing "nil ispb-if-credtd returns nil"
    (is (nil? (parser/r2-outbound-queue "QL.REQ.00000000.99999999.01" nil)))))

(deftest parse-str0010-test
  (testing "all fields present"
    (let [xml (str "<STR0010>"
                   "<NumCtrlIF>NC-010</NumCtrlIF>"
                   "<NumCtrlSTROr>NC-008</NumCtrlSTROr>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>1500.00</VlrLanc>"
                   "<CodDevTransf>MD06</CodDevTransf>"
                   "<DtMovto>20240115</DtMovto>"
                   "</STR0010>")
          m   (parser/parse-str0010 xml)]
      (is (= "NC-010" (:num-ctrl-if m)))
      (is (= "NC-008" (:num-ctrl-str-or m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "1500.00" (:vlr-lanc m)))
      (is (= "MD06" (:cod-dev-transf m)))
      (is (= "20240115" (:dt-movto m)))))
  (testing "missing optional fields return nil"
    (let [m (parser/parse-str0010 "<STR0010><NumCtrlIF>X</NumCtrlIF></STR0010>")]
      (is (nil? (:num-ctrl-str-or m)))
      (is (nil? (:cod-dev-transf m))))))

(deftest parse-str0048-test
  (testing "all fields present including ispb-if-devedora"
    (let [xml (str "<STR0048>"
                   "<NumCtrlIF>NC-048</NumCtrlIF>"
                   "<NumCtrlSTROr>NC-047</NumCtrlSTROr>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>2000.00</VlrLanc>"
                   "<CodDevTransf>MD06</CodDevTransf>"
                   "<DtMovto>20240115</DtMovto>"
                   "<ISPBIFDevedora>22222222</ISPBIFDevedora>"
                   "</STR0048>")
          m   (parser/parse-str0048 xml)]
      (is (= "NC-048" (:num-ctrl-if m)))
      (is (= "NC-047" (:num-ctrl-str-or m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "2000.00" (:vlr-lanc m)))
      (is (= "MD06" (:cod-dev-transf m)))
      (is (= "20240115" (:dt-movto m)))
      (is (= "22222222" (:ispb-if-devedora m)))))
  (testing "missing ispb-if-devedora returns nil"
    (let [m (parser/parse-str0048 "<STR0048><NumCtrlIF>X</NumCtrlIF></STR0048>")]
      (is (nil? (:ispb-if-devedora m))))))

(deftest sender-ispb-from-queue-test
  (testing "extracts sender ISPB from index 2"
    (is (= "00000000" (parser/sender-ispb-from-queue "QL.REQ.00000000.99999999.01"))))
  (testing "works for different queue types"
    (is (= "12345678" (parser/sender-ispb-from-queue "QL.RSP.12345678.99999999.02"))))
  (testing "returns nil for malformed queue name"
    (is (nil? (parser/sender-ispb-from-queue "INVALID")))))
