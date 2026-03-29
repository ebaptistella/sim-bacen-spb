(ns com.github.ebaptistella.wire.in.slb.slb0005-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.wire.in.slb.slb0005 :refer [SLB0005Request]]
            [schema.core :as s]))

(deftest slb0005-schema-validation
  (testing "Valid SLB0005 request with all required fields"
    (is (s/validate SLB0005Request
          {:NumCtrlSTR "67890"
           :ISPBPart "00000000"
           :VlrLanc 2000.00
           :FIndddSLB "99"
           :NumCtrlSLB "11111"
           :DtVenc "20260430"})))

  (testing "SLB0005 with optional Hist field"
    (is (s/validate SLB0005Request
          {:NumCtrlSTR "67890"
           :ISPBPart "00000000"
           :VlrLanc 2000.00
           :FIndddSLB "99"
           :NumCtrlSLB "11111"
           :DtVenc "20260430"
           :Hist "Specific debit"})))

  (testing "Invalid: Missing required field NumCtrlSTR"
    (is (thrown? Exception
          (s/validate SLB0005Request
            {:ISPBPart "00000000"
             :VlrLanc 2000.00
             :FIndddSLB "99"
             :NumCtrlSLB "11111"
             :DtVenc "20260430"}))))

  (testing "Invalid: Missing required field VlrLanc"
    (is (thrown? Exception
          (s/validate SLB0005Request
            {:NumCtrlSTR "67890"
             :ISPBPart "00000000"
             :FIndddSLB "99"
             :NumCtrlSLB "11111"
             :DtVenc "20260430"}))))

  (testing "Invalid: Negative value"
    (is (thrown? Exception
          (s/validate SLB0005Request
            {:NumCtrlSTR "67890"
             :ISPBPart "00000000"
             :VlrLanc -500
             :FIndddSLB "99"
             :NumCtrlSLB "11111"
             :DtVenc "20260430"})))))
