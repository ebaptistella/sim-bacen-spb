(ns com.github.ebaptistella.wire.in.messages-schema-test
  (:require [clojure.test :refer [deftest is testing]]
            [schema.core :as s]
            [com.github.ebaptistella.wire.in.messages :refer [RespondBody]])
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
