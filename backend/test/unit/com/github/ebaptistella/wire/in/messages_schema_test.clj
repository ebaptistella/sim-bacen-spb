(ns com.github.ebaptistella.wire.in.messages-schema-test
  (:require [clojure.test :refer [deftest is testing]]
            [schema.core :as s]
            [com.github.ebaptistella.wire.in.messages :refer [RespondBody OutboundBody]])
  (:import [clojure.lang ExceptionInfo]))

(deftest respond-body-schema-test
  (testing "valid body without params"
    (is (nil? (s/check RespondBody {:response-type "STR0008R1"})))
    (is (= {:response-type "STR0008R1"}
           (s/validate RespondBody {:response-type "STR0008R1"}))))

  (testing "valid body with params"
    (is (nil? (s/check RespondBody {:response-type "STR0008R2"
                                    :params        {:SitLancSTR "LQDADO"}})))
    (is (= {:response-type "STR0008R2"
            :params        {:SitLancSTR "LQDADO"}}
           (s/validate RespondBody {:response-type "STR0008R2"
                                    :params        {:SitLancSTR "LQDADO"}}))))

  (testing "invalid — missing response-type"
    (is (some? (s/check RespondBody {})))
    (try
      (s/validate RespondBody {})
      (is false "should throw")
      (catch ExceptionInfo e
        (is (= :schema.core/error (:type (ex-data e)))))))

  (testing "invalid — response-type outside enum"
    (is (some? (s/check RespondBody {:response-type "STR9999X"})))
    (try
      (s/validate RespondBody {:response-type "STR9999X"})
      (is false "should throw")
      (catch ExceptionInfo e
        (is (= :schema.core/error (:type (ex-data e))))))))

(deftest outbound-body-schema-test
  (testing "outbound-body-valid-str0015 — type and participant only"
    (is (nil? (s/check OutboundBody {:type "STR0015" :participant "12345678"}))))

  (testing "outbound-body-valid-with-params — optional params map accepted"
    (is (nil? (s/check OutboundBody {:type    "STR0016"
                                     :participant "00000000"
                                     :params  {:saldo "500.00"}}))))

  (testing "outbound-body-invalid-type — unknown STR type rejected"
    (is (some? (s/check OutboundBody {:type "STR9999" :participant "12345678"}))))

  (testing "outbound-body-invalid-participant-letters — non-digit participant rejected"
    (is (some? (s/check OutboundBody {:type "STR0017" :participant "ABCDEFGH"}))))

  (testing "outbound-body-invalid-participant-short — 7-digit participant rejected"
    (is (some? (s/check OutboundBody {:type "STR0017" :participant "1234567"}))))

  (testing "outbound-body-missing-type — absent type rejected"
    (is (some? (s/check OutboundBody {:participant "12345678"}))))

  (testing "outbound-body-missing-participant — absent participant rejected"
    (is (some? (s/check OutboundBody {:type "STR0015"})))))
