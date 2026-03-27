(ns com.github.ebaptistella.logic.str.str0005-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0005 :as str0005]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if    "NC-005"
   :ispb-if-debtd  "00000000"
   :ispb-if-credtd "11111111"
   :vlr-lanc       "2500.00"
   :finldd-cli     "0001"
   :dt-movto       "20260101"})

(deftest r1-response-test
  (testing "CodMsg is STR0005R1"
    (let [fields (str0005/r1-response base-msg nil)]
      (is (= :STR0005R1 (:CodMsg fields)))))
  (testing "SitLancSTR defaults to LQDADO when params is nil"
    (let [fields (str0005/r1-response base-msg nil)]
      (is (= "LQDADO" (:SitLancSTR fields)))))
  (testing "NumCtrlIF is echoed from msg"
    (let [fields (str0005/r1-response base-msg nil)]
      (is (= "NC-005" (:NumCtrlIF fields)))))
  (testing "ISPBIFDebtd is echoed from msg"
    (let [fields (str0005/r1-response base-msg nil)]
      (is (= "00000000" (:ISPBIFDebtd fields)))))
  (testing "NumCtrlSTR is present with 20 chars"
    (let [fields   (str0005/r1-response base-msg nil)
          num-ctrl (:NumCtrlSTR fields)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl)))))
  (testing "DtHrSit is present and not blank"
    (let [fields (str0005/r1-response base-msg nil)]
      (is (some? (:DtHrSit fields)))
      (is (not (str/blank? (:DtHrSit fields))))))
  (testing "DtMovto is echoed from :dt-movto in msg"
    (let [fields (str0005/r1-response base-msg nil)]
      (is (= "20260101" (:DtMovto fields))))))

(deftest r2-response-test
  (testing "CodMsg is STR0005R2"
    (let [fields (str0005/r2-response base-msg nil)]
      (is (= :STR0005R2 (:CodMsg fields)))))
  (testing "FinlddCli is echoed from msg in XML"
    (let [fields (str0005/r2-response base-msg nil)
          xml    (str0005/response->xml "STR0005R2" fields)]
      (is (str/includes? xml "<FinlddCli>0001</FinlddCli>"))))
  (testing "FinlddIF is not present in STR0005R2 XML"
    (let [fields (str0005/r2-response base-msg nil)
          xml    (str0005/response->xml "STR0005R2" fields)]
      (is (not (str/includes? xml "<FinlddIF>")))))
  (testing "ISPBIFCredtd is echoed from msg"
    (let [fields (str0005/r2-response base-msg nil)]
      (is (= "11111111" (:ISPBIFCredtd fields)))))
  (testing "VlrLanc is echoed from msg"
    (let [fields (str0005/r2-response base-msg nil)]
      (is (= "2500.00" (:VlrLanc fields)))))
  (testing "NumCtrlSTR is present with 20 chars"
    (let [fields   (str0005/r2-response base-msg nil)
          num-ctrl (:NumCtrlSTR fields)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl))))))

(deftest rejection-response-test
  (testing "nil params returns :missing-motivo error"
    (let [result (str0005/rejection-response base-msg nil)]
      (is (= {:error :missing-motivo} result))))
  (testing "empty params map returns :missing-motivo error"
    (let [result (str0005/rejection-response base-msg {})]
      (is (= {:error :missing-motivo} result))))
  (testing "params with nil MotivoRejeicao returns :missing-motivo error"
    (let [result (str0005/rejection-response base-msg {:MotivoRejeicao nil})]
      (is (= {:error :missing-motivo} result))))
  (testing "valid MotivoRejeicao AC09 appears in XML"
    (let [fields (str0005/rejection-response base-msg {:MotivoRejeicao "AC09"})
          xml    (str0005/response->xml "STR0005E" fields)]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>"))))
  (testing "CodMsg is STR0005E when MotivoRejeicao is provided"
    (let [fields (str0005/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= :STR0005E (:CodMsg fields))))))
