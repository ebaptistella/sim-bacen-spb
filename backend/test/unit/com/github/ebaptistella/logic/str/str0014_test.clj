(ns com.github.ebaptistella.logic.str.str0014-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0014 :as str0014])
  (:import [java.time Instant ZoneId]
           [java.time.format DateTimeFormatter]))

(def ^:private zone-br (ZoneId/of "America/Sao_Paulo"))

(def ^:private fmt-date (DateTimeFormatter/ofPattern "yyyyMMdd"))

(def ^:private msg-str0008
  {:id            "uuid-ext-1"
   :type          "STR0008"
   :status        :responded
   :num-ctrl-if   "CTRL0014A"
   :ispb-if-debtd "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc      "1500.00"
   :received-at   "20240315100000"
   :response      {:num-ctrl-str "aabbccdd1122334455aa" :sent-at "20240315100500"}})

(def ^:private msg-unknown-type
  {:id            "uuid-ext-2"
   :type          "STR9999"
   :status        :responded
   :num-ctrl-if   "CTRL0014B"
   :ispb-if-debtd "22222222"
   :ispb-if-credtd "33333333"
   :vlr-lanc      "9999.99"
   :received-at   "20240315110000"
   :response      {:num-ctrl-str "zz1122334455aabbccdd" :sent-at "20240315110500"}})

(def ^:private store-msgs [msg-str0008 msg-unknown-type])

(deftest filter-extrato-test
  (testing "filter-extrato is a pass-through: returns the same list unchanged"
    (let [parsed-msg {:num-ctrl-if "Q" :ispb-if-debtd "00000000" :dt-ref nil}
          result     (str0014/filter-extrato store-msgs parsed-msg)]
      (is (= store-msgs result))))
  (testing "filter-extrato with empty list returns empty list"
    (let [parsed-msg {:num-ctrl-if "Q" :ispb-if-debtd "00000000" :dt-ref nil}
          result     (str0014/filter-extrato [] parsed-msg)]
      (is (= [] result))))
  (testing "filter-extrato preserves order"
    (let [parsed-msg {:num-ctrl-if "Q" :ispb-if-debtd "00000000" :dt-ref nil}
          result     (str0014/filter-extrato store-msgs parsed-msg)]
      (is (= "uuid-ext-1" (:id (first result))))
      (is (= "uuid-ext-2" (:id (second result)))))))

(deftest r1-response-lista-vazia-test
  (testing "empty movimentos list produces QtdLanc=0 and no Lancamento blocks"
    (let [parsed-msg {:num-ctrl-if "CTRL0014" :ispb-if-debtd "44444444" :dt-ref nil}
          xml        (str0014/r1-response parsed-msg [])]
      (is (str/includes? xml "<QtdLanc>0</QtdLanc>"))
      (is (not (str/includes? xml "<Lancamento>"))))))

(deftest r1-response-tp-lanc-ted-test
  (testing "STR0008 message produces TpLanc=TED in Lancamento block"
    (let [parsed-msg {:num-ctrl-if "CTRL0014" :ispb-if-debtd "00000000" :dt-ref nil}
          xml        (str0014/r1-response parsed-msg [msg-str0008])]
      (is (str/includes? xml "<QtdLanc>1</QtdLanc>"))
      (is (str/includes? xml "<TpLanc>TED</TpLanc>")))))

(deftest r1-response-tp-lanc-otr-test
  (testing "unknown message type produces TpLanc=OTR in Lancamento block"
    (let [parsed-msg {:num-ctrl-if "CTRL0014" :ispb-if-debtd "22222222" :dt-ref nil}
          xml        (str0014/r1-response parsed-msg [msg-unknown-type])]
      (is (str/includes? xml "<TpLanc>OTR</TpLanc>")))))

(deftest r1-response-dt-movto-test
  (testing "DtMovto echoes :dt-ref when present"
    (let [parsed-msg {:num-ctrl-if "CTRL0014" :ispb-if-debtd "55555555" :dt-ref "20240315"}
          xml        (str0014/r1-response parsed-msg [])]
      (is (str/includes? xml "<DtMovto>20240315</DtMovto>"))))
  (testing "DtMovto uses today's date when :dt-ref is nil"
    (let [parsed-msg {:num-ctrl-if "CTRL0014" :ispb-if-debtd "66666666" :dt-ref nil}
          xml        (str0014/r1-response parsed-msg [])
          now        (.format fmt-date (.atZone (Instant/now) zone-br))
          expected   (str "<DtMovto>" now "</DtMovto>")]
      (is (str/includes? xml expected)))))

(deftest r1-response-ecoa-campos-test
  (testing "NumCtrlIF from parsed-msg is echoed in XML"
    (let [parsed-msg {:num-ctrl-if "ECHOCTRL14" :ispb-if-debtd "77777777" :dt-ref nil}
          xml        (str0014/r1-response parsed-msg [])]
      (is (str/includes? xml "<NumCtrlIF>ECHOCTRL14</NumCtrlIF>"))))
  (testing "ISPBIFDebtd from parsed-msg is echoed in XML"
    (let [parsed-msg {:num-ctrl-if "X" :ispb-if-debtd "88888888" :dt-ref nil}
          xml        (str0014/r1-response parsed-msg [])]
      (is (str/includes? xml "<ISPBIFDebtd>88888888</ISPBIFDebtd>"))))
  (testing "CodMsg is STR0014R1"
    (let [parsed-msg {:num-ctrl-if "X" :ispb-if-debtd "99999999" :dt-ref nil}
          xml        (str0014/r1-response parsed-msg [])]
      (is (str/includes? xml "<CodMsg>STR0014R1</CodMsg>"))))
  (testing "NumCtrlSTR has 20 alphanumeric chars"
    (let [parsed-msg {:num-ctrl-if "X" :ispb-if-debtd "12345678" :dt-ref nil}
          xml        (str0014/r1-response parsed-msg [])
          match      (re-find #"<NumCtrlSTR>([^<]+)</NumCtrlSTR>" xml)
          num-ctrl   (second match)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl))))))

(deftest r1-response-multiplos-movimentos-test
  (testing "2 movimentos produce QtdLanc=2 and 2 Lancamento blocks"
    (let [parsed-msg {:num-ctrl-if "CTRL0014" :ispb-if-debtd "00000000" :dt-ref nil}
          xml        (str0014/r1-response parsed-msg store-msgs)]
      (is (str/includes? xml "<QtdLanc>2</QtdLanc>"))
      (is (= 2 (count (re-seq #"<Lancamento>" xml)))))))
