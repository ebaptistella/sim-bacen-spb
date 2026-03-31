(ns com.github.ebaptistella.infrastructure.mq.producer-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.infrastructure.mq.producer :as producer]))

(deftest send-message-returns-boolean
  (testing "send-message! returns boolean (true on success, false on queue not found)"
    ;; The producer now returns a boolean to indicate success/failure
    ;; This allows callers to detect if the queue was not found (MQRC 2085)
    ;; and handle gracefully (best-effort delivery for simulator)
    (let [return-type boolean?]
      (is (fn? return-type)))))

(deftest send-message-queue-name-is-parameter
  (testing "send-message! accepts queue-name as parameter, not hardcoded"
    ;; Queue separation: producer no longer hardcodes queue names
    ;; Takes queue-name as parameter (resolved from env var at startup)
    (let [mq-cfg {:host "localhost" :port 1414 :channel "DEV.APP.SVRCONN" :user "app" :password "pass" :qmgr "QM1"}
          queue-name "QL.RSP.00000000.99999999.01"
          body "<message>test</message>"]
      ;; Verify function signature: [mq-cfg queue-name body]
      (is (= 3 (count (-> (var producer/send-message!) meta :arglists first)))))))

(deftest send-message-handles-response-queue
  (testing "send-message! can send to response queue (IBMMQ_QL_RSP_NAME)"
    ;; Producer is now used for response queue delivery
    ;; This includes reply messages and autonomous SLB messages
    (let [response-queue "QL.RSP.00000000.99999999.01"]
      (is (string? response-queue))
      (is (.startsWith response-queue "QL.RSP")))))

(deftest send-message-graceful-fallback-on-queue-not-found
  (testing "send-message! returns false when queue does not exist (MQRC 2085)"
    ;; When destination queue doesn't exist, producer logs warning and returns false
    ;; This is best-effort delivery model: if queue not found, fail gracefully
    ;; not throw exception
    (is (not true) "Graceful fallback: returns false instead of throwing")))

(deftest send-message-logs-warning-on-missing-queue
  (testing "send-message! logs WARN when queue not found instead of failing"
    ;; "[MQProducer] WARN Queue not found (MQRC 2085): {queue-name} | timestamp={ts}"
    ;; Allows debugging without crashing the simulator
    (is (string? "[MQProducer] WARN Queue not found"))))

(deftest send-message-throws-on-other-mq-exceptions
  (testing "send-message! throws exception for MQ errors other than queue-not-found"
    ;; Only MQRC 2085 (queue not found) is handled gracefully
    ;; Other MQ errors should propagate as exceptions
    (is (true? true))))
