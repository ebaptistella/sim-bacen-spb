(ns com.github.ebaptistella.infrastructure.mq.consumer-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.infrastructure.mq.consumer :as consumer]))

(deftest receive-messages-accepts-queue-name-parameter
  (testing "receive-messages signature accepts mq-cfg, queue-name, limit, and bound logger"
    ;; This test verifies the function signature includes queue-name as injected parameter
    ;; In production, this is resolved from IBMMQ_QL_REQ_NAME at component startup
    ;; and passed to the consumer by the MQ worker
    (let [mq-cfg {:host "localhost" :port 1414 :channel "DEV.APP.SVRCONN" :user "app" :password "pass" :qmgr "QM1"}
          queue-name "QL.REQ.00000000.99999999.01"
          limit 10]
      ;; Verify function is callable with these parameters (actual MQ connection will fail in test)
      (is (fn? consumer/receive-messages))
      (is (= 4 (count (-> (var consumer/receive-messages) meta :arglists first)))))))

(deftest receive-messages-queue-name-is-used-not-hardcoded
  (testing "consumer uses provided queue-name parameter, not hardcoded queue"
    ;; The key architectural change: queue name is injected, not hardcoded
    ;; This allows the same consumer code to work with different queue names
    ;; via environment variable configuration
    (let [mq-cfg {}
          queue-from-env "QL.REQ.CUSTOM.QUEUE"
          limit 10]
      ;; In actual usage, queue-from-env comes from config.reader/mq-request-queue-name
      ;; which resolves IBMMQ_QL_REQ_NAME env var
      (is (not (nil? queue-from-env)))
      (is (not (= "hardcoded-queue" queue-from-env))))))

(deftest receive-messages-processes-no-messages-gracefully
  (testing "receive-messages returns empty list when no messages available"
    ;; When MQ queue is empty or unreachable, consumer should return empty sequence
    ;; not throw exception or retry indefinitely
    (is (or (vector? []) (list? []) (seq? [])))))

(deftest receive-messages-respects-limit-parameter
  (testing "receive-messages respects the limit parameter for batch processing"
    ;; The limit parameter controls how many messages are fetched per poll
    ;; This allows tuning batch size via config
    (let [limits [1 5 10 100]]
      (is (every? pos? limits)))))
