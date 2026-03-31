(ns com.github.ebaptistella.config.reader-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [com.github.ebaptistella.config.reader :as config.reader]))

(defn- set-env-var [key value]
  (System/setProperty key value))

(defn- clear-env-var [key]
  (System/clearProperty key))

(deftest mq-request-queue-name-resolves-from-env
  (testing "mq-request-queue-name resolves IBMMQ_QL_REQ_NAME from environment"
    (set-env-var "IBMMQ_QL_REQ_NAME" "QL.REQ.CUSTOM.01")
    (try
      (let [queue-name (config.reader/mq-request-queue-name {})]
        (is (= "QL.REQ.CUSTOM.01" queue-name)))
      (finally
        (clear-env-var "IBMMQ_QL_REQ_NAME")))))

(deftest mq-response-queue-name-resolves-from-env
  (testing "mq-response-queue-name resolves IBMMQ_QL_RSP_NAME from environment"
    (set-env-var "IBMMQ_QL_RSP_NAME" "QL.RSP.CUSTOM.01")
    (try
      (let [queue-name (config.reader/mq-response-queue-name {})]
        (is (= "QL.RSP.CUSTOM.01" queue-name)))
      (finally
        (clear-env-var "IBMMQ_QL_RSP_NAME")))))

(deftest mq-request-queue-name-returns-nil-when-not-set
  (testing "mq-request-queue-name returns nil when IBMMQ_QL_REQ_NAME is not set"
    (clear-env-var "IBMMQ_QL_REQ_NAME")
    (let [queue-name (config.reader/mq-request-queue-name {})]
      (is (nil? queue-name)))))

(deftest mq-response-queue-name-returns-nil-when-not-set
  (testing "mq-response-queue-name returns nil when IBMMQ_QL_RSP_NAME is not set"
    (clear-env-var "IBMMQ_QL_RSP_NAME")
    (let [queue-name (config.reader/mq-response-queue-name {})]
      (is (nil? queue-name)))))

(deftest mq-request-queue-name-multiple-calls-consistent
  (testing "multiple calls to mq-request-queue-name return same value"
    (set-env-var "IBMMQ_QL_REQ_NAME" "QL.REQ.TEST.01")
    (try
      (let [call1 (config.reader/mq-request-queue-name {})
            call2 (config.reader/mq-request-queue-name {})]
        (is (= call1 call2))
        (is (= "QL.REQ.TEST.01" call1)))
      (finally
        (clear-env-var "IBMMQ_QL_REQ_NAME")))))

(deftest mq-response-queue-name-multiple-calls-consistent
  (testing "multiple calls to mq-response-queue-name return same value"
    (set-env-var "IBMMQ_QL_RSP_NAME" "QL.RSP.TEST.01")
    (try
      (let [call1 (config.reader/mq-response-queue-name {})
            call2 (config.reader/mq-response-queue-name {})]
        (is (= call1 call2))
        (is (= "QL.RSP.TEST.01" call1)))
      (finally
        (clear-env-var "IBMMQ_QL_RSP_NAME")))))
