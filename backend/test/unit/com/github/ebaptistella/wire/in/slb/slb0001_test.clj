(ns com.github.ebaptistella.wire.in.slb.slb0001-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.wire.in.slb.slb0001 :refer [SLB0001Request]]
            [schema.core :as s]))

(deftest slb0001-schema-validation
  (testing "Valid SLB0001 request"
    (is (s/validate SLB0001Request
          {:NumCtrlSLB "NC-001"
           :ISPBPart "12345678"
           :DtVenc "20260328"
           :VlrLanc 1000.50
           :FIndddSLB "05"})))

  (testing "SLB0001 with optional fields"
    (is (s/validate SLB0001Request
          {:NumCtrlSLB "NC-001"
           :ISPBPart "12345678"
           :DtVenc "20260328"
           :VlrLanc 1000.50
           :FIndddSLB "05"
           :Hist "Payment notification"})))

  (testing "Valid: Only ISPBPart (other fields optional for broadcasts)"
    (is (s/validate SLB0001Request
          {:ISPBPart "12345678"})))

  (testing "Invalid: Invalid ISPB format"
    (is (thrown? Exception
          (s/validate SLB0001Request
            {:NumCtrlSLB "NC-001"
             :ISPBPart "INVALID"
             :DtVenc "20260328"
             :VlrLanc 1000.50
             :FIndddSLB "05"}))))

  (testing "Invalid: Negative value"
    (is (thrown? Exception
          (s/validate SLB0001Request
            {:NumCtrlSLB "NC-001"
             :ISPBPart "12345678"
             :DtVenc "20260328"
             :VlrLanc -100
             :FIndddSLB "05"})))))
