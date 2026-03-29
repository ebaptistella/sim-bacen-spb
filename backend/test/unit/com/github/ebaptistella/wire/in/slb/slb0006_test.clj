(ns com.github.ebaptistella.wire.in.slb.slb0006-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.wire.in.slb.slb0006 :refer [SLB0006Request]]
            [schema.core :as s]))

(deftest slb0006-schema-validation
  (testing "Valid SLB0006 request with minimal fields"
    (is (s/validate SLB0006Request
          {:NumCtrlPart "54321"
           :ISPBPart "00000000"})))

  (testing "SLB0006 with all optional fields"
    (is (s/validate SLB0006Request
          {:NumCtrlPart "54321"
           :ISPBPart "00000000"
           :DtRef "20260329"
           :TpDeb_Cred "D"
           :NumCtrlSLB "22222"})))

  (testing "Invalid: Missing required field NumCtrlPart"
    (is (thrown? Exception
          (s/validate SLB0006Request
            {:ISPBPart "00000000"}))))

  (testing "Invalid: Missing required field ISPBPart"
    (is (thrown? Exception
          (s/validate SLB0006Request
            {:NumCtrlPart "54321"}))))

  (testing "Valid: With DtRef field"
    (is (s/validate SLB0006Request
          {:NumCtrlPart "54321"
           :ISPBPart "00000000"
           :DtRef "20260329"})))

  (testing "Valid: With TpDeb_Cred field"
    (is (s/validate SLB0006Request
          {:NumCtrlPart "54321"
           :ISPBPart "00000000"
           :TpDeb_Cred "C"}))))
