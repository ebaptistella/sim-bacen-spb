(ns com.github.ebaptistella.logic.str.str0048-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0048 :as str0048]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if      "NC-048"
   :num-ctrl-str-or  "NC-047"
   :ispb-if-debtd    "00000000"
   :ispb-if-credtd   "11111111"
   :vlr-lanc         "2000.00"
   :cod-dev-transf   "MD06"
   :dt-movto         "20240115"
   :ispb-if-devedora "22222222"})

(deftest r1-response-test
  (testing "CodMsg is STR0048R1"
    (is (= :STR0048R1 (:CodMsg (str0048/r1-response base-msg nil)))))
  (testing "SitLancSTR defaults to LQDADO"
    (is (= "LQDADO" (:SitLancSTR (str0048/r1-response base-msg nil)))))
  (testing "NumCtrlIF echoed"
    (is (= "NC-048" (:NumCtrlIF (str0048/r1-response base-msg nil)))))
  (testing "ISPBIFDebtd echoed"
    (is (= "00000000" (:ISPBIFDebtd (str0048/r1-response base-msg nil)))))
  (testing "NumCtrlSTR is 20 chars"
    (let [ncs (:NumCtrlSTR (str0048/r1-response base-msg nil))]
      (is (some? ncs))
      (is (= 20 (count ncs)))))
  (testing "DtMovto echoed"
    (is (= "20240115" (:DtMovto (str0048/r1-response base-msg nil))))))

(deftest r2-response-test
  (testing "CodMsg is STR0048R2"
    (is (= :STR0048R2 (:CodMsg (str0048/r2-response base-msg nil)))))
  (testing "ISPBIFDebtd and ISPBIFCredtd echoed"
    (let [fields (str0048/r2-response base-msg nil)]
      (is (= "00000000" (:ISPBIFDebtd fields)))
      (is (= "11111111" (:ISPBIFCredtd fields)))))
  (testing "VlrLanc and CodDevTransf echoed"
    (let [fields (str0048/r2-response base-msg nil)]
      (is (= "2000.00" (:VlrLanc fields)))
      (is (= "MD06" (:CodDevTransf fields)))))
  (testing "NumCtrlSTROr echoed"
    (is (= "NC-047" (:NumCtrlSTROr (str0048/r2-response base-msg nil)))))
  (testing "NumCtrlSTR from params takes precedence"
    (is (= "NCTRL12345678901234" (:NumCtrlSTR (str0048/r2-response base-msg {:NumCtrlSTR "NCTRL12345678901234"}))))))

(deftest r3-response-test
  (testing "CodMsg is STR0048R3"
    (is (= :STR0048R3 (:CodMsg (str0048/r3-response base-msg nil)))))
  (testing "ISPBIFDevedora from msg"
    (is (= "22222222" (:ISPBIFDevedora (str0048/r3-response base-msg nil)))))
  (testing "ISPBIFDevedora from params overrides nil in msg"
    (let [msg-no-devedora (dissoc base-msg :ispb-if-devedora)
          fields          (str0048/r3-response msg-no-devedora {:ISPBIFDevedora "33333333"})]
      (is (= "33333333" (:ISPBIFDevedora fields)))))
  (testing "NumCtrlSTR from params takes precedence"
    (is (= "NCTRL12345678901234" (:NumCtrlSTR (str0048/r3-response base-msg {:NumCtrlSTR "NCTRL12345678901234"})))))
  (testing "DtHrBC is present"
    (is (some? (:DtHrBC (str0048/r3-response base-msg nil))))))

(deftest rejection-response-test
  (testing "nil params → :missing-motivo"
    (is (= {:error :missing-motivo} (str0048/rejection-response base-msg nil))))
  (testing "valid MotivoRejeicao"
    (let [fields (str0048/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= :STR0048E (:CodMsg fields)))
      (is (= "AC09" (:MotivoRejeicao fields))))))

(deftest response->xml-test
  (testing "STR0048R1 XML structure"
    (let [xml (str0048/response->xml :STR0048R1 (str0048/r1-response base-msg nil))]
      (is (str/starts-with? xml "<STR0048R1>"))
      (is (str/ends-with? xml "</STR0048R1>"))
      (is (str/includes? xml "<CodMsg>STR0048R1</CodMsg>"))))
  (testing "STR0048R2 includes CodDevTransf"
    (let [xml (str0048/response->xml :STR0048R2 (str0048/r2-response base-msg nil))]
      (is (str/includes? xml "<CodDevTransf>MD06</CodDevTransf>"))))
  (testing "STR0048R3 includes ISPBIFDevedora"
    (let [xml (str0048/response->xml :STR0048R3 (str0048/r3-response base-msg nil))]
      (is (str/includes? xml "<ISPBIFDevedora>22222222</ISPBIFDevedora>"))))
  (testing "STR0048E includes MotivoRejeicao"
    (let [xml (str0048/response->xml :STR0048E (str0048/rejection-response base-msg {:MotivoRejeicao "AC09"}))]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>")))))
