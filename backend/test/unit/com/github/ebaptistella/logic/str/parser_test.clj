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

(deftest parse-str0025-test
  (testing "all fields present"
    (let [xml (str "<STR0025>"
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
                   "</STR0025>")
          m   (parser/parse-str0025 xml)]
      (is (= "NC-025" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "5000.00" (:vlr-lanc m)))
      (is (= "0017" (:finldd-cli m)))
      (is (= "20260103" (:dt-movto m)))
      (is (= "01" (:tp-ct-debtd m)))
      (is (= "05" (:tp-ct-credtd m)))
      (is (= "1234" (:agencia m)))
      (is (= "0000123456789" (:ct-pgto m)))
      (is (= "DEPOSITO JUDICIAL" (:hist m)))))
  (testing "missing optional field returns nil"
    (let [m (parser/parse-str0025 "<STR0025><NumCtrlIF>X</NumCtrlIF></STR0025>")]
      (is (nil? (:agencia m)))
      (is (nil? (:ct-pgto m)))
      (is (nil? (:hist m))))))

(deftest parse-str0034-test
  (testing "all fields present"
    (let [xml (str "<STR0034>"
                   "<NumCtrlIF>NC-034</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>6000.00</VlrLanc>"
                   "<FinlddIF>00006</FinlddIF>"
                   "<DtMovto>20260103</DtMovto>"
                   "<TpCtDebtd>01</TpCtDebtd>"
                   "<TpCtCredtd>05</TpCtCredtd>"
                   "</STR0034>")
          m   (parser/parse-str0034 xml)]
      (is (= "NC-034" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "6000.00" (:vlr-lanc m)))
      (is (= "00006" (:finldd-if m)))
      (is (= "20260103" (:dt-movto m)))
      (is (= "01" (:tp-ct-debtd m)))
      (is (= "05" (:tp-ct-credtd m)))))
  (testing "missing optional field returns nil"
    (let [m (parser/parse-str0034 "<STR0034><NumCtrlIF>X</NumCtrlIF></STR0034>")]
      (is (nil? (:tp-ct-debtd m)))
      (is (nil? (:tp-ct-credtd m))))))

(deftest parse-str0037-test
  (testing "all fields present"
    (let [xml (str "<STR0037>"
                   "<NumCtrlIF>NC-037</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>7000.00</VlrLanc>"
                   "<FinlddCli>0017</FinlddCli>"
                   "<DtMovto>20260103</DtMovto>"
                   "<TpCtDebtd>01</TpCtDebtd>"
                   "<TpCtCredtd>05</TpCtCredtd>"
                   "<Agencia>1234</Agencia>"
                   "<CtPgto>0000123456789</CtPgto>"
                   "</STR0037>")
          m   (parser/parse-str0037 xml)]
      (is (= "NC-037" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "7000.00" (:vlr-lanc m)))
      (is (= "0017" (:finldd-cli m)))
      (is (= "20260103" (:dt-movto m)))
      (is (= "01" (:tp-ct-debtd m)))
      (is (= "05" (:tp-ct-credtd m)))
      (is (= "1234" (:agencia m)))
      (is (= "0000123456789" (:ct-pgto m)))))
  (testing "missing optional field returns nil"
    (let [m (parser/parse-str0037 "<STR0037><NumCtrlIF>X</NumCtrlIF></STR0037>")]
      (is (nil? (:agencia m)))
      (is (nil? (:ct-pgto m))))))

(deftest parse-str0039-test
  (testing "all fields present"
    (let [xml (str "<STR0039>"
                   "<NumCtrlIF>NC-039</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>8000.00</VlrLanc>"
                   "<FinlddIF>00006</FinlddIF>"
                   "<DtMovto>20260103</DtMovto>"
                   "<TpCtDebtd>01</TpCtDebtd>"
                   "<TpCtCredtd>05</TpCtCredtd>"
                   "</STR0039>")
          m   (parser/parse-str0039 xml)]
      (is (= "NC-039" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "8000.00" (:vlr-lanc m)))
      (is (= "00006" (:finldd-if m)))
      (is (= "20260103" (:dt-movto m)))
      (is (= "01" (:tp-ct-debtd m)))
      (is (= "05" (:tp-ct-credtd m)))))
  (testing "missing optional field returns nil"
    (let [m (parser/parse-str0039 "<STR0039><NumCtrlIF>X</NumCtrlIF></STR0039>")]
      (is (nil? (:tp-ct-debtd m)))
      (is (nil? (:tp-ct-credtd m))))))

(deftest parse-str0041-test
  (testing "all fields present"
    (let [xml (str "<STR0041>"
                   "<NumCtrlIF>NC-041</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>9000.00</VlrLanc>"
                   "<FinlddCli>0017</FinlddCli>"
                   "<DtMovto>20260103</DtMovto>"
                   "<TpCtDebtd>01</TpCtDebtd>"
                   "<TpCtCredtd>05</TpCtCredtd>"
                   "<Agencia>1234</Agencia>"
                   "</STR0041>")
          m   (parser/parse-str0041 xml)]
      (is (= "NC-041" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "9000.00" (:vlr-lanc m)))
      (is (= "0017" (:finldd-cli m)))
      (is (= "20260103" (:dt-movto m)))
      (is (= "01" (:tp-ct-debtd m)))
      (is (= "05" (:tp-ct-credtd m)))
      (is (= "1234" (:agencia m)))))
  (testing "missing optional field returns nil"
    (let [m (parser/parse-str0041 "<STR0041><NumCtrlIF>X</NumCtrlIF></STR0041>")]
      (is (nil? (:agencia m))))))

(deftest parse-str0047-test
  (testing "all fields present"
    (let [xml (str "<STR0047>"
                   "<NumCtrlIF>NC-047</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>10000.00</VlrLanc>"
                   "<FinlddIF>00006</FinlddIF>"
                   "<DtMovto>20260103</DtMovto>"
                   "<TpCtDebtd>01</TpCtDebtd>"
                   "<TpCtCredtd>05</TpCtCredtd>"
                   "</STR0047>")
          m   (parser/parse-str0047 xml)]
      (is (= "NC-047" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "10000.00" (:vlr-lanc m)))
      (is (= "00006" (:finldd-if m)))
      (is (= "20260103" (:dt-movto m)))
      (is (= "01" (:tp-ct-debtd m)))
      (is (= "05" (:tp-ct-credtd m)))))
  (testing "missing optional field returns nil"
    (let [m (parser/parse-str0047 "<STR0047><NumCtrlIF>X</NumCtrlIF></STR0047>")]
      (is (nil? (:tp-ct-debtd m)))
      (is (nil? (:tp-ct-credtd m))))))

(deftest parse-str0051-test
  (testing "all fields present"
    (let [xml (str "<STR0051>"
                   "<NumCtrlIF>NC-051</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>11000.00</VlrLanc>"
                   "<FinlddCli>0017</FinlddCli>"
                   "<DtMovto>20260103</DtMovto>"
                   "<TpCtDebtd>01</TpCtDebtd>"
                   "<TpCtCredtd>05</TpCtCredtd>"
                   "<Hist>DEPOSITO JUDICIAL</Hist>"
                   "</STR0051>")
          m   (parser/parse-str0051 xml)]
      (is (= "NC-051" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "11000.00" (:vlr-lanc m)))
      (is (= "0017" (:finldd-cli m)))
      (is (= "20260103" (:dt-movto m)))
      (is (= "01" (:tp-ct-debtd m)))
      (is (= "05" (:tp-ct-credtd m)))
      (is (= "DEPOSITO JUDICIAL" (:hist m)))))
  (testing "missing optional field returns nil"
    (let [m (parser/parse-str0051 "<STR0051><NumCtrlIF>X</NumCtrlIF></STR0051>")]
      (is (nil? (:hist m))))))

(deftest parse-str0052-test
  (testing "all fields present"
    (let [xml (str "<STR0052>"
                   "<NumCtrlIF>NC-052</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>12000.00</VlrLanc>"
                   "<FinlddIF>00006</FinlddIF>"
                   "<DtMovto>20260103</DtMovto>"
                   "<TpCtDebtd>01</TpCtDebtd>"
                   "<TpCtCredtd>05</TpCtCredtd>"
                   "</STR0052>")
          m   (parser/parse-str0052 xml)]
      (is (= "NC-052" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "12000.00" (:vlr-lanc m)))
      (is (= "00006" (:finldd-if m)))
      (is (= "20260103" (:dt-movto m)))
      (is (= "01" (:tp-ct-debtd m)))
      (is (= "05" (:tp-ct-credtd m)))))
  (testing "missing optional field returns nil"
    (let [m (parser/parse-str0052 "<STR0052><NumCtrlIF>X</NumCtrlIF></STR0052>")]
      (is (nil? (:tp-ct-debtd m)))
      (is (nil? (:tp-ct-credtd m))))))
