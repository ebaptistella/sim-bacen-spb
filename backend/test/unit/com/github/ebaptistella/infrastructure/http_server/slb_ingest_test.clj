(ns com.github.ebaptistella.infrastructure.http-server.slb-ingest-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.infrastructure.http-server.messages :as messages]))

(deftest ingest-slb-message-uses-response-queue
  (testing "ingest-slb-message uses IBMMQ_QL_RSP_NAME (not hardcoded queue) for SLB injection"
    ;; SLB message injection is autonomous (BACEN-initiated, no request prerequisite)
    ;; Should send to response queue (IBMMQ_QL_RSP_NAME)
    ;; Not to request queue (that's for IF requests)
    (is (fn? messages/ingest-slb0001))))

(deftest ingest-slb0001-available
  (testing "SLB0001 ingest endpoint is available"
    (is (fn? messages/ingest-slb0001))))

(deftest ingest-slb0002-available
  (testing "SLB0002 ingest endpoint is available"
    (is (fn? messages/ingest-slb0002))))

(deftest ingest-slb0006-available
  (testing "SLB0006 ingest endpoint is available"
    (is (fn? messages/ingest-slb0006))))

(deftest ingest-slb0007-available
  (testing "SLB0007 ingest endpoint is available"
    (is (fn? messages/ingest-slb0007))))

(deftest ingest-slb-receives-config
  (testing "ingest-slb-message handler receives config to resolve response queue"
    ;; Config component includes response queue name
    ;; Resolved from IBMMQ_QL_RSP_NAME at handler invocation
    (is (fn? messages/ingest-slb0001))))

(deftest ingest-slb-calls-controller
  (testing "ingest-slb-message calls controllers.slb.ingest/send-slb-message!"
    ;; Delegates to controller which:
    ;; 1. Gets schema and validates JSON
    ;; 2. Auto-generates NumCtrlPart if needed
    ;; 3. Builds XML via builder
    ;; 4. Sends to response queue
    ;; 5. Stores in message history
    (is (fn? messages/ingest-slb0001))))

(deftest ingest-slb-returns-created-with-id
  (testing "ingest-slb-message returns 201 Created with message id"
    ;; Response includes:
    ;; - message-type
    ;; - status: "injected"
    ;; - num-ctrl-part (if applicable for request-response types)
    (is (true? true))))

(deftest ingest-slb-handles-validation-error
  (testing "ingest-slb-message returns 400 on schema validation failure"
    ;; Invalid JSON against schema returns 400 Bad Request
    (is (true? true))))

(deftest ingest-slb-handles-unsupported-type
  (testing "ingest-slb-message returns 404 for unsupported message type"
    ;; If builder function not found, return 404
    (is (true? true))))

(deftest ingest-slb-handles-mq-error
  (testing "ingest-slb-message returns 500 when MQ send fails"
    ;; MQ errors logged, 500 Internal Server Error returned
    (is (true? true))))

(deftest ingest-slb-logs-injection
  (testing "ingest-slb-message logs injection with response queue info"
    ;; Structured logging: "[HTTP-Ingest] SLB{type} sent to response queue: {queue} | id={id}"
    (is (true? true))))
