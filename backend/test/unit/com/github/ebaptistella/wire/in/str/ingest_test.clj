(ns com.github.ebaptistella.wire.in.str.ingest-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.wire.in.str.ingest :as ingest]
            [schema.core :as s]))

(deftest str0008-schema-validation-test
  (testing "accepts valid STR0008 request"
    (let [valid {:NumCtrlIF "NC-001"
                 :ISPBIFDebtd "00000000"
                 :ISPBIFCredtd "11111111"
                 :VlrLanc "1000.00"
                 :FinlddCli "10"
                 :DtMovto "20260328"}]
      (is (= valid (s/validate ingest/STR0008IngestSchema valid)))))

  (testing "rejects missing required field"
    (let [invalid {:NumCtrlIF "NC-001"
                   :ISPBIFDebtd "00000000"
                   :ISPBIFCredtd "11111111"
                   ; missing VlrLanc
                   :FinlddCli "10"
                   :DtMovto "20260328"}]
      (is (thrown? clojure.lang.ExceptionInfo
                   (s/validate ingest/STR0008IngestSchema invalid)))))

  (testing "rejects invalid field format (bad ISPB)"
    (let [invalid {:NumCtrlIF "NC-001"
                   :ISPBIFDebtd "1234"  ;invalid - must be 8 digits
                   :ISPBIFCredtd "11111111"
                   :VlrLanc "1000.00"
                   :FinlddCli "10"
                   :DtMovto "20260328"}]
      (is (thrown? clojure.lang.ExceptionInfo
                   (s/validate ingest/STR0008IngestSchema invalid)))))

  (testing "rejects invalid field format (bad VlrLanc)"
    (let [invalid {:NumCtrlIF "NC-001"
                   :ISPBIFDebtd "00000000"
                   :ISPBIFCredtd "11111111"
                   :VlrLanc "not-a-number"  ;invalid
                   :FinlddCli "10"
                   :DtMovto "20260328"}]
      (is (thrown? clojure.lang.ExceptionInfo
                   (s/validate ingest/STR0008IngestSchema invalid))))))

(deftest str0001-schema-validation-test
  (testing "accepts valid STR0001 request"
    (let [valid {:NumCtrlIF "NC-001"
                 :ISPBIFDebtd "00000000"
                 :DtRef "20260328"}]
      (is (= valid (s/validate ingest/STR0001IngestSchema valid)))))

  (testing "accepts optional HrIni and HrFim"
    (let [valid {:NumCtrlIF "NC-001"
                 :ISPBIFDebtd "00000000"
                 :DtRef "20260328"
                 :HrIni "090000"
                 :HrFim "180000"}]
      (is (= valid (s/validate ingest/STR0001IngestSchema valid))))))

(deftest get-schema-test
  (testing "returns schema for known type"
    (is (some? (ingest/get-schema "STR0001")))
    (is (some? (ingest/get-schema "STR0008"))))

  (testing "returns nil for unknown type"
    (is (nil? (ingest/get-schema "STR9999")))))
