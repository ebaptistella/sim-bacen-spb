(ns com.github.ebaptistella.logic.str.ingest-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.logic.str.ingest :as ingest]))

(deftest build-str0008-xml-test
  (testing "builds STR0008 XML with required fields"
    (let [params {:NumCtrlIF "NC-001"
                  :ISPBIFDebtd "00000000"
                  :ISPBIFCredtd "11111111"
                  :VlrLanc "1000.00"
                  :FinlddCli "10"
                  :DtMovto "20260328"}
          xml (ingest/build-str0008-xml params)]
      (is (string? xml))
      (is (clojure.string/includes? xml "<STR0008>"))
      (is (clojure.string/includes? xml "<CodMsg>STR0008</CodMsg>"))
      (is (clojure.string/includes? xml "<NumCtrlIF>NC-001</NumCtrlIF>"))
      (is (clojure.string/includes? xml "<VlrLanc>1000.00</VlrLanc>")))))

(deftest build-xml-for-type-test
  (testing "builds XML for STR0001"
    (let [params {:NumCtrlIF "NC-001"
                  :ISPBIFDebtd "00000000"
                  :DtRef "20260328"}
          xml (ingest/build-xml-for-type "STR0001" params)]
      (is (string? xml))
      (is (clojure.string/includes? xml "<STR0001>"))))

  (testing "throws for unknown type"
    (is (thrown? clojure.lang.ExceptionInfo
                 (ingest/build-xml-for-type "STR9999" {})))))
