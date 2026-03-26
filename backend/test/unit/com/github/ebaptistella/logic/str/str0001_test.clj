(ns com.github.ebaptistella.logic.str.str0001-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0001 :as str0001])
  (:import [java.time Instant ZoneId]
           [java.time.format DateTimeFormatter]))

(def ^:private zone-br (ZoneId/of "America/Sao_Paulo"))

(def ^:private fmt-date (DateTimeFormatter/ofPattern "yyyyMMdd"))

(deftest r1-response-horarios-test
  (testing "HrAbertura and HrFechamento from config are present in XML"
    (let [parsed-msg {:num-ctrl-if "CTRL001" :ispb-if-debtd "00000000" :dt-ref nil}
          config     {:str-horario-abertura "07:00" :str-horario-fechamento "17:30"}
          xml        (str0001/r1-response parsed-msg config)]
      (is (str/includes? xml "<HrAbertura>07:00</HrAbertura>"))
      (is (str/includes? xml "<HrFechamento>17:30</HrFechamento>"))))
  (testing "custom horarios from config override defaults"
    (let [parsed-msg {:num-ctrl-if "CTRL002" :ispb-if-debtd "11111111" :dt-ref nil}
          config     {:str-horario-abertura "08:30" :str-horario-fechamento "18:00"}
          xml        (str0001/r1-response parsed-msg config)]
      (is (str/includes? xml "<HrAbertura>08:30</HrAbertura>"))
      (is (str/includes? xml "<HrFechamento>18:00</HrFechamento>")))))

(deftest r1-response-defaults-test
  (testing "nil config uses default horarios 07:00 and 17:30"
    (let [parsed-msg {:num-ctrl-if "CTRL003" :ispb-if-debtd "22222222" :dt-ref nil}
          xml        (str0001/r1-response parsed-msg nil)]
      (is (str/includes? xml "<HrAbertura>07:00</HrAbertura>"))
      (is (str/includes? xml "<HrFechamento>17:30</HrFechamento>"))))
  (testing "empty config map uses default horarios"
    (let [parsed-msg {:num-ctrl-if "CTRL004" :ispb-if-debtd "33333333" :dt-ref nil}
          xml        (str0001/r1-response parsed-msg {})]
      (is (str/includes? xml "<HrAbertura>07:00</HrAbertura>"))
      (is (str/includes? xml "<HrFechamento>17:30</HrFechamento>")))))

(deftest r1-response-num-ctrl-if-test
  (testing "NumCtrlIF from parsed-msg is echoed in XML"
    (let [parsed-msg {:num-ctrl-if "MECTRL999" :ispb-if-debtd "44444444" :dt-ref nil}
          xml        (str0001/r1-response parsed-msg {})]
      (is (str/includes? xml "<NumCtrlIF>MECTRL999</NumCtrlIF>")))))

(deftest r1-response-num-ctrl-str-test
  (testing "NumCtrlSTR has 20 alphanumeric chars"
    (let [parsed-msg {:num-ctrl-if "X" :ispb-if-debtd "55555555" :dt-ref nil}
          xml        (str0001/r1-response parsed-msg {})
          match      (re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" xml)
          num-ctrl   (second match)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl)))
      (is (re-matches #"[0-9a-f]{20}" num-ctrl)))))

(deftest r1-response-sit-lanc-str-test
  (testing "SitLancSTR is absent from STR0001R1 (horários query, not a lançamento)"
    (let [parsed-msg {:num-ctrl-if "Y" :ispb-if-debtd "66666666" :dt-ref nil}
          xml        (str0001/r1-response parsed-msg {})]
      (is (not (str/includes? xml "<SitLancSTR>"))))))

(deftest r1-response-dt-movto-test
  (testing "DtMovto echoes :dt-ref when present"
    (let [parsed-msg {:num-ctrl-if "Z" :ispb-if-debtd "77777777" :dt-ref "20240115"}
          xml        (str0001/r1-response parsed-msg {})]
      (is (str/includes? xml "<DtMovto>20240115</DtMovto>"))))
  (testing "DtMovto uses today's date when :dt-ref is nil"
    (let [parsed-msg {:num-ctrl-if "W" :ispb-if-debtd "88888888" :dt-ref nil}
          xml        (str0001/r1-response parsed-msg {})
          now        (.format fmt-date (.atZone (Instant/now) zone-br))
          expected   (str "<DtMovto>" now "</DtMovto>")]
      (is (str/includes? xml expected))))
  (testing "CodMsg is STR0001R1"
    (let [parsed-msg {:num-ctrl-if "V" :ispb-if-debtd "99999999" :dt-ref nil}
          xml        (str0001/r1-response parsed-msg {})]
      (is (str/includes? xml "<CodMsg>STR0001R1</CodMsg>")))))
