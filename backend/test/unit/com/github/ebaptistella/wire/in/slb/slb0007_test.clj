(ns com.github.ebaptistella.wire.in.slb.slb0007-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.wire.in.slb.slb0007 :refer [SLB0007Request]]
            [schema.core :as s]))

(deftest slb0007-schema-validation
  (testing "Valid SLB0007 request with all required fields"
    (is (s/validate SLB0007Request
          {:NumCtrlPart "11111"
           :ISPBPart "00000000"
           :VlrLanc 500.00})))

  (testing "SLB0007 with optional fields"
    (is (s/validate SLB0007Request
          {:NumCtrlPart "11111"
           :ISPBPart "00000000"
           :VlrLanc 500.00
           :DtMovto "20260329"
           :Hist "Specific debit"})))

  (testing "Invalid: Missing required field NumCtrlPart"
    (is (thrown? Exception
          (s/validate SLB0007Request
            {:ISPBPart "00000000"
             :VlrLanc 500.00}))))

  (testing "Invalid: Missing required field ISPBPart"
    (is (thrown? Exception
          (s/validate SLB0007Request
            {:NumCtrlPart "11111"
             :VlrLanc 500.00}))))

  (testing "Invalid: Missing required field VlrLanc"
    (is (thrown? Exception
          (s/validate SLB0007Request
            {:NumCtrlPart "11111"
             :ISPBPart "00000000"}))))

  (testing "Invalid: Negative value for VlrLanc"
    (is (thrown? Exception
          (s/validate SLB0007Request
            {:NumCtrlPart "11111"
             :ISPBPart "00000000"
             :VlrLanc -250}))))

  (testing "Valid: Large decimal value"
    (is (s/validate SLB0007Request
          {:NumCtrlPart "11111"
           :ISPBPart "00000000"
           :VlrLanc 999999.99}))))
