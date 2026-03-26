(ns com.github.ebaptistella.logic.str.str0013-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0013 :as str0013])
  (:import [java.time Instant ZoneId]
           [java.time.format DateTimeFormatter]))

(def ^:private zone-br (ZoneId/of "America/Sao_Paulo"))

(def ^:private fmt-date (DateTimeFormatter/ofPattern "yyyyMMdd"))

(deftest r1-response-saldo-simulado-test
  (testing "SldCntRsv uses str-saldo-simulado from config when present"
    (let [parsed-msg {:num-ctrl-if "CTRL0013" :ispb-if-debtd "00000000" :dt-ref nil}
          config     {:str-saldo-simulado "500000.00"}
          xml        (str0013/r1-response parsed-msg config)]
      (is (str/includes? xml "<SldCntRsv>500000.00</SldCntRsv>"))))
  (testing "custom saldo value from config appears in XML"
    (let [parsed-msg {:num-ctrl-if "CTRL0013B" :ispb-if-debtd "11111111" :dt-ref nil}
          config     {:str-saldo-simulado "123456.78"}
          xml        (str0013/r1-response parsed-msg config)]
      (is (str/includes? xml "<SldCntRsv>123456.78</SldCntRsv>")))))

(deftest r1-response-saldo-default-test
  (testing "nil config uses default saldo 99999999.99"
    (let [parsed-msg {:num-ctrl-if "CTRL0013C" :ispb-if-debtd "22222222" :dt-ref nil}
          xml        (str0013/r1-response parsed-msg nil)]
      (is (str/includes? xml "<SldCntRsv>99999999.99</SldCntRsv>"))))
  (testing "empty config map uses default saldo 99999999.99"
    (let [parsed-msg {:num-ctrl-if "CTRL0013D" :ispb-if-debtd "33333333" :dt-ref nil}
          xml        (str0013/r1-response parsed-msg {})]
      (is (str/includes? xml "<SldCntRsv>99999999.99</SldCntRsv>")))))

(deftest r1-response-num-ctrl-if-test
  (testing "NumCtrlIF from parsed-msg is echoed in XML"
    (let [parsed-msg {:num-ctrl-if "ECHOCTRL13" :ispb-if-debtd "44444444" :dt-ref nil}
          xml        (str0013/r1-response parsed-msg {})]
      (is (str/includes? xml "<NumCtrlIF>ECHOCTRL13</NumCtrlIF>"))))
  (testing "ISPBIFDebtd from parsed-msg is echoed in XML"
    (let [parsed-msg {:num-ctrl-if "X" :ispb-if-debtd "55555555" :dt-ref nil}
          xml        (str0013/r1-response parsed-msg {})]
      (is (str/includes? xml "<ISPBIFDebtd>55555555</ISPBIFDebtd>")))))

(deftest r1-response-sit-lanc-str-test
  (testing "SitLancSTR is absent from STR0013R1 (saldo query, not a lançamento)"
    (let [parsed-msg {:num-ctrl-if "Y" :ispb-if-debtd "66666666" :dt-ref nil}
          xml        (str0013/r1-response parsed-msg {})]
      (is (not (str/includes? xml "<SitLancSTR>")))))
  (testing "CodMsg is STR0013R1"
    (let [parsed-msg {:num-ctrl-if "Z" :ispb-if-debtd "77777777" :dt-ref nil}
          xml        (str0013/r1-response parsed-msg {})]
      (is (str/includes? xml "<CodMsg>STR0013R1</CodMsg>")))))

(deftest r1-response-dt-movto-test
  (testing "DtMovto echoes :dt-ref when present"
    (let [parsed-msg {:num-ctrl-if "W" :ispb-if-debtd "88888888" :dt-ref "20240220"}
          xml        (str0013/r1-response parsed-msg {})]
      (is (str/includes? xml "<DtMovto>20240220</DtMovto>"))))
  (testing "DtMovto uses today's date when :dt-ref is nil"
    (let [parsed-msg {:num-ctrl-if "V" :ispb-if-debtd "99999999" :dt-ref nil}
          xml        (str0013/r1-response parsed-msg {})
          now        (.format fmt-date (.atZone (Instant/now) zone-br))
          expected   (str "<DtMovto>" now "</DtMovto>")]
      (is (str/includes? xml expected)))))

(deftest r1-response-num-ctrl-str-test
  (testing "NumCtrlSTR has 20 alphanumeric chars"
    (let [parsed-msg {:num-ctrl-if "U" :ispb-if-debtd "11223344" :dt-ref nil}
          xml        (str0013/r1-response parsed-msg {})
          match      (re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" xml)
          num-ctrl   (second match)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl))))))
