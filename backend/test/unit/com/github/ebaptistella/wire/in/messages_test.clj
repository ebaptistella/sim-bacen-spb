(ns com.github.ebaptistella.wire.in.messages-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.wire.in.messages :refer [RespondBody]]
            [schema.core :as s])
  (:import [clojure.lang ExceptionInfo]))

(deftest respond-body-aceita-str0011r1
  (testing "STR0011R1 response-type is accepted"
    (is (= {:response-type "STR0011R1"}
           (s/validate RespondBody {:response-type "STR0011R1"})))))

(deftest respond-body-aceita-str0005r2
  (testing "STR0005R2 response-type is accepted"
    (is (= {:response-type "STR0005R2"}
           (s/validate RespondBody {:response-type "STR0005R2"})))))

(deftest respond-body-aceita-str0007r2-com-params
  (testing "STR0007R2 with optional :params map is accepted"
    (is (= {:response-type "STR0007R2" :params {:MotivoRejeicao "AC09"}}
           (s/validate RespondBody {:response-type "STR0007R2"
                                    :params        {:MotivoRejeicao "AC09"}})))))

(deftest respond-body-rejeita-sem-response-type
  (testing "missing :response-type key throws ExceptionInfo"
    (is (thrown? ExceptionInfo (s/validate RespondBody {})))))

(deftest respond-body-aceita-qualquer-string
  (testing "any string value for :response-type is accepted by schema (content validation is the controller's concern)"
    (is (= {:response-type "XPTO-QUALQUER"}
           (s/validate RespondBody {:response-type "XPTO-QUALQUER"})))))
