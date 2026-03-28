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

;; --- Iter 6: Repasses e transferências entre IFs ---

(deftest parse-str0020-test
  (testing "extracts FinlddIF and basic fields"
    (let [xml (str "<STR0020>"
                   "<NumCtrlIF>NC-020</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>1000.00</VlrLanc>"
                   "<FinlddIF>00020</FinlddIF>"
                   "<DtMovto>20260327</DtMovto>"
                   "</STR0020>")
          m   (parser/parse-str0020 xml)]
      (is (= "NC-020" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "1000.00" (:vlr-lanc m)))
      (is (= "00020" (:finldd-if m)))
      (is (= "20260327" (:dt-movto m)))))
  (testing "missing field returns nil"
    (let [m (parser/parse-str0020 "<STR0020><NumCtrlIF>X</NumCtrlIF></STR0020>")]
      (is (nil? (:finldd-if m)))
      (is (nil? (:ispb-if-credtd m))))))

(deftest parse-str0021-test
  (testing "extracts FinlddIF"
    (let [m (parser/parse-str0021 "<STR0021><NumCtrlIF>NC-021</NumCtrlIF><FinlddIF>00021</FinlddIF></STR0021>")]
      (is (= "NC-021" (:num-ctrl-if m)))
      (is (= "00021" (:finldd-if m)))))
  (testing "missing FinlddIF returns nil"
    (is (nil? (:finldd-if (parser/parse-str0021 "<STR0021><NumCtrlIF>X</NumCtrlIF></STR0021>"))))))

(deftest parse-str0022-test
  (testing "extracts FinlddIF"
    (let [m (parser/parse-str0022 "<STR0022><NumCtrlIF>NC-022</NumCtrlIF><FinlddIF>00022</FinlddIF></STR0022>")]
      (is (= "NC-022" (:num-ctrl-if m)))
      (is (= "00022" (:finldd-if m)))))
  (testing "missing FinlddIF returns nil"
    (is (nil? (:finldd-if (parser/parse-str0022 "<STR0022><NumCtrlIF>X</NumCtrlIF></STR0022>"))))))

(deftest parse-str0026-test
  (testing "extracts FinlddIF"
    (let [m (parser/parse-str0026 "<STR0026><NumCtrlIF>NC-026</NumCtrlIF><FinlddIF>00026</FinlddIF></STR0026>")]
      (is (= "NC-026" (:num-ctrl-if m)))
      (is (= "00026" (:finldd-if m)))))
  (testing "missing FinlddIF returns nil"
    (is (nil? (:finldd-if (parser/parse-str0026 "<STR0026><NumCtrlIF>X</NumCtrlIF></STR0026>"))))))

(deftest parse-str0029-test
  (testing "extracts FinlddIF"
    (let [m (parser/parse-str0029 "<STR0029><NumCtrlIF>NC-029</NumCtrlIF><FinlddIF>00029</FinlddIF></STR0029>")]
      (is (= "NC-029" (:num-ctrl-if m)))
      (is (= "00029" (:finldd-if m)))))
  (testing "missing FinlddIF returns nil"
    (is (nil? (:finldd-if (parser/parse-str0029 "<STR0029><NumCtrlIF>X</NumCtrlIF></STR0029>"))))))

(deftest parse-str0033-test
  (testing "extracts FinlddIF"
    (let [m (parser/parse-str0033 "<STR0033><NumCtrlIF>NC-033</NumCtrlIF><FinlddIF>00033</FinlddIF></STR0033>")]
      (is (= "NC-033" (:num-ctrl-if m)))
      (is (= "00033" (:finldd-if m)))))
  (testing "missing FinlddIF returns nil"
    (is (nil? (:finldd-if (parser/parse-str0033 "<STR0033><NumCtrlIF>X</NumCtrlIF></STR0033>"))))))

(deftest parse-str0045-test
  (testing "extracts FinlddIF"
    (let [m (parser/parse-str0045 "<STR0045><NumCtrlIF>NC-045</NumCtrlIF><FinlddIF>00045</FinlddIF></STR0045>")]
      (is (= "NC-045" (:num-ctrl-if m)))
      (is (= "00045" (:finldd-if m)))))
  (testing "missing FinlddIF returns nil"
    (is (nil? (:finldd-if (parser/parse-str0045 "<STR0045><NumCtrlIF>X</NumCtrlIF></STR0045>"))))))

(deftest parse-str0046-test
  (testing "extracts CodDevTransf and NumCtrlSTROr"
    (let [xml (str "<STR0046>"
                   "<NumCtrlIF>NC-046</NumCtrlIF>"
                   "<NumCtrlSTROr>NC-020</NumCtrlSTROr>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<ISPBIFCredtd>11111111</ISPBIFCredtd>"
                   "<VlrLanc>500.00</VlrLanc>"
                   "<FinlddIF>00046</FinlddIF>"
                   "<CodDevTransf>MD01</CodDevTransf>"
                   "<DtMovto>20260327</DtMovto>"
                   "</STR0046>")
          m   (parser/parse-str0046 xml)]
      (is (= "NC-046" (:num-ctrl-if m)))
      (is (= "NC-020" (:num-ctrl-str-or m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "11111111" (:ispb-if-credtd m)))
      (is (= "500.00" (:vlr-lanc m)))
      (is (= "00046" (:finldd-if m)))
      (is (= "MD01" (:cod-dev-transf m)))
      (is (= "20260327" (:dt-movto m)))))
  (testing "missing CodDevTransf and NumCtrlSTROr return nil"
    (let [m (parser/parse-str0046 "<STR0046><NumCtrlIF>X</NumCtrlIF></STR0046>")]
      (is (nil? (:cod-dev-transf m)))
      (is (nil? (:num-ctrl-str-or m))))))

(deftest parse-str0003-test
  (testing "extracts FinlddIF"
    (let [m (parser/parse-str0003 "<STR0003><NumCtrlIF>NC-003</NumCtrlIF><FinlddIF>00003</FinlddIF></STR0003>")]
      (is (= "NC-003" (:num-ctrl-if m)))
      (is (= "00003" (:finldd-if m)))))
  (testing "missing FinlddIF returns nil"
    (is (nil? (:finldd-if (parser/parse-str0003 "<STR0003><NumCtrlIF>X</NumCtrlIF></STR0003>"))))))

(deftest parse-str0004-test
  (testing "extracts FinlddIF"
    (let [m (parser/parse-str0004 "<STR0004><NumCtrlIF>NC-004</NumCtrlIF><FinlddIF>00004</FinlddIF></STR0004>")]
      (is (= "NC-004" (:num-ctrl-if m)))
      (is (= "00004" (:finldd-if m)))))
  (testing "missing FinlddIF returns nil"
    (is (nil? (:finldd-if (parser/parse-str0004 "<STR0004><NumCtrlIF>X</NumCtrlIF></STR0004>"))))))

(deftest parse-str0040-test
  (testing "extracts FinlddIF"
    (let [m (parser/parse-str0040 "<STR0040><NumCtrlIF>NC-040</NumCtrlIF><FinlddIF>00040</FinlddIF></STR0040>")]
      (is (= "NC-040" (:num-ctrl-if m)))
      (is (= "00040" (:finldd-if m)))))
  (testing "missing FinlddIF returns nil"
    (is (nil? (:finldd-if (parser/parse-str0040 "<STR0040><NumCtrlIF>X</NumCtrlIF></STR0040>"))))))

;; --- Iter 6: Contingência Fluxo1 ---

(deftest parse-str0043-test
  (testing "extracts NumCtrlIF, ISPBIFDebtd, DtMovto"
    (let [xml (str "<STR0043>"
                   "<NumCtrlIF>NC-043</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<DtMovto>20260327</DtMovto>"
                   "</STR0043>")
          m   (parser/parse-str0043 xml)]
      (is (= "NC-043" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "20260327" (:dt-movto m)))))
  (testing "missing field returns nil"
    (let [m (parser/parse-str0043 "<STR0043><NumCtrlIF>X</NumCtrlIF></STR0043>")]
      (is (nil? (:ispb-if-debtd m)))
      (is (nil? (:dt-movto m))))))

(deftest parse-str0044-test
  (testing "extracts NumCtrlIF, ISPBIFDebtd, DtMovto"
    (let [xml (str "<STR0044>"
                   "<NumCtrlIF>NC-044</NumCtrlIF>"
                   "<ISPBIFDebtd>00000000</ISPBIFDebtd>"
                   "<DtMovto>20260327</DtMovto>"
                   "</STR0044>")
          m   (parser/parse-str0044 xml)]
      (is (= "NC-044" (:num-ctrl-if m)))
      (is (= "00000000" (:ispb-if-debtd m)))
      (is (= "20260327" (:dt-movto m)))))
  (testing "missing field returns nil"
    (let [m (parser/parse-str0044 "<STR0044><NumCtrlIF>X</NumCtrlIF></STR0044>")]
      (is (nil? (:ispb-if-debtd m)))
      (is (nil? (:dt-movto m))))))
