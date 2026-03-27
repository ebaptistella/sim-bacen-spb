(ns com.github.ebaptistella.logic.str.str0011-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.logic.str.str0011 :as str0011]
            [schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(def ^:private base-msg
  {:num-ctrl-if "NC-011" :ispb-if-debtd "00000000"})

(deftest r1-response-cancelado-test
  (testing "CodMsg is STR0011R1"
    (let [fields (str0011/r1-response base-msg nil)]
      (is (= "STR0011R1" (:CodMsg fields)))))
  (testing "SitLancSTR is CANCELADO"
    (let [fields (str0011/r1-response base-msg nil)]
      (is (= "CANCELADO" (:SitLancSTR fields)))))
  (testing "NumCtrlIF is echoed from msg"
    (let [fields (str0011/r1-response base-msg nil)]
      (is (= "NC-011" (:NumCtrlIF fields)))))
  (testing "ISPBIFDebtd is echoed from msg"
    (let [fields (str0011/r1-response base-msg nil)]
      (is (= "00000000" (:ISPBIFDebtd fields)))))
  (testing "NumCtrlSTR is present with 20 alphanumeric chars"
    (let [fields      (str0011/r1-response base-msg nil)
          num-ctrl    (:NumCtrlSTR fields)]
      (is (some? num-ctrl))
      (is (= 20 (count num-ctrl)))))
  (testing "DtHrSit is present and not blank"
    (let [fields (str0011/r1-response base-msg nil)]
      (is (some? (:DtHrSit fields)))
      (is (not (str/blank? (:DtHrSit fields))))))
  (testing "DtMovto is not present in R1 field-ordering"
    (let [xml (str0011/response->xml :STR0011R1 (str0011/r1-response base-msg nil))]
      (is (not (str/includes? xml "<DtMovto>"))))))

(deftest rejection-response-sem-motivo-test
  (testing "nil params returns :missing-motivo error"
    (let [result (str0011/rejection-response base-msg nil)]
      (is (= {:error :missing-motivo} result))))
  (testing "empty params map returns :missing-motivo error"
    (let [result (str0011/rejection-response base-msg {})]
      (is (= {:error :missing-motivo} result))))
  (testing "params with nil MotivoRejeicao returns :missing-motivo error"
    (let [result (str0011/rejection-response base-msg {:MotivoRejeicao nil})]
      (is (= {:error :missing-motivo} result)))))

(deftest rejection-response-com-motivo-test
  (testing "valid MotivoRejeicao AC09 appears in XML"
    (let [fields (str0011/rejection-response base-msg {:MotivoRejeicao "AC09"})
          xml    (str0011/response->xml :STR0011E fields)]
      (is (str/includes? xml "<MotivoRejeicao>AC09</MotivoRejeicao>"))))
  (testing "CodMsg is STR0011E"
    (let [fields (str0011/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= "STR0011E" (:CodMsg fields)))))
  (testing "NumCtrlIF is echoed from msg"
    (let [fields (str0011/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= "NC-011" (:NumCtrlIF fields)))))
  (testing "ISPBIFDebtd is echoed from msg"
    (let [fields (str0011/rejection-response base-msg {:MotivoRejeicao "AC09"})]
      (is (= "00000000" (:ISPBIFDebtd fields))))))

(deftest response-xml-test
  (testing "response->xml STR0011R1 starts and ends with correct tags"
    (let [fields (str0011/r1-response base-msg nil)
          xml    (str0011/response->xml :STR0011R1 fields)]
      (is (str/starts-with? xml "<STR0011R1>"))
      (is (str/ends-with? xml "</STR0011R1>"))))
  (testing "response->xml STR0011E contains CodMsg STR0011E"
    (let [fields (str0011/rejection-response base-msg {:MotivoRejeicao "AC09"})
          xml    (str0011/response->xml :STR0011E fields)]
      (is (str/includes? xml "<CodMsg>STR0011E</CodMsg>")))))
