(ns com.github.ebaptistella.logic.str.str0008-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.str0008 :as str0008])
  (:import [java.time Instant ZoneId]
           [java.time.format DateTimeFormatter]))

(def ^:private zone-br (ZoneId/of "America/Sao_Paulo"))

(def ^:private fmt-date (DateTimeFormatter/ofPattern "yyyyMMdd"))

(def ^:private fmt-datetime (DateTimeFormatter/ofPattern "yyyyMMddHHmmss"))

(deftest r1-response-test
  (testing "echoes NumCtrlIF and ISPBIFDebtd; SitLancSTR defaults to LQDADO; NumCtrlSTR is 20 chars"
    (let [msg {:num-ctrl-if "ABC123" :ispb-if-debtd "00000000" :dt-movto "20240115"}
          r1  (str0008/r1-response msg nil)]
      (is (= "ABC123" (:NumCtrlIF r1)))
      (is (= "00000000" (:ISPBIFDebtd r1)))
      (is (= "LQDADO" (:SitLancSTR r1)))
      (is (= 20 (count (:NumCtrlSTR r1))))
      (is (re-matches #"[0-9a-f]{20}" (:NumCtrlSTR r1)))))
  (testing "SitLancSTR overridden via params"
    (let [msg {:num-ctrl-if "X" :ispb-if-debtd "Y"}
          r1  (str0008/r1-response msg {:SitLancSTR "REJEITADO"})]
      (is (= "REJEITADO" (:SitLancSTR r1)))))
  (testing "DtMovto reflects processing date, not original :dt-movto from request"
    (let [msg {:num-ctrl-if "A" :ispb-if-debtd "B" :dt-movto "20240115"}
          r1  (str0008/r1-response msg nil)
          now (.format fmt-date (.atZone (Instant/now) zone-br))]
      (is (= now (:DtMovto r1)))
      (is (not= (:dt-movto msg) (:DtMovto r1))))))

(deftest r2-response-test
  (testing "echoes ISPBIFDebtd, ISPBIFCredtd, VlrLanc, FinlddCli"
    (let [msg {:ispb-if-debtd  "00000000"
               :ispb-if-credtd "11111111"
               :vlr-lanc       "1500.00"
               :finldd-cli     "01"}
          r2  (str0008/r2-response msg nil)]
      (is (= "00000000" (:ISPBIFDebtd r2)))
      (is (= "11111111" (:ISPBIFCredtd r2)))
      (is (= "1500.00" (:VlrLanc r2)))
      (is (= "01" (:FinlddCli r2)))
      (is (= 20 (count (:NumCtrlSTR r2))))))
  (testing "DtHrBC present and aligned to current instant (yyyyMMddHHmmss)"
    (let [msg {:ispb-if-debtd "0" :ispb-if-credtd "1" :vlr-lanc "1" :finldd-cli "01"}
          r2  (str0008/r2-response msg nil)
          now (.format fmt-datetime (.atZone (Instant/now) zone-br))]
      (is (re-matches #"\d{14}" (:DtHrBC r2)))
      (is (= now (:DtHrBC r2))))))

(deftest rejection-response-and-xml-test
  (testing "missing MotivoRejeicao returns {:error :missing-motivo}"
    (let [msg {:num-ctrl-if "N1" :ispb-if-debtd "00000000"}]
      (is (= {:error :missing-motivo} (str0008/rejection-response msg nil)))
      (is (= {:error :missing-motivo} (str0008/rejection-response msg {})))
      (is (= {:error :missing-motivo} (str0008/rejection-response msg {:MotivoRejeicao ""})))))
  (testing "STR0008E XML with MotivoRejeicao"
    (let [msg  {:num-ctrl-if "IF1" :ispb-if-debtd "22222222"}
          flds (str0008/rejection-response msg {:MotivoRejeicao "AC09"})
          xml  (str0008/response->xml "STR0008E" flds)]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>"))
      (is (str/includes? xml "<NumCtrlIF>IF1</NumCtrlIF>"))
      (is (str/includes? xml "<ISPBIFDebtd>22222222</ISPBIFDebtd>")))))
