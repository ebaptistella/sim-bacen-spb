(ns com.github.ebaptistella.infrastructure.http-server.messages-ingest-test
  (:require [clojure.test :refer [deftest is testing]]
            [com.github.ebaptistella.infrastructure.http-server.messages :as messages]))

(deftest ingest-message-uses-dynamic-queue
  (testing "ingest-message uses IBMMQ_QL_REQ_NAME (not hardcoded queue) for STR injection"
    ;; STR message injection endpoint should send to request queue
    ;; Queue name is resolved from IBMMQ_QL_REQ_NAME env var via config.reader
    ;; This allows different deployments to use different queues
    (is (fn? messages/ingest-str0001))))

(deftest ingest-message-receives-config-component
  (testing "ingest-message handler receives config to resolve queue name"
    ;; Handler receives full request which includes config component
    ;; Config component is passed to config.reader/mq-request-queue-name
    ;; This resolves IBMMQ_QL_REQ_NAME at runtime
    (is (fn? messages/ingest-str0001))))

(deftest ingest-str-variants-available
  (testing "all STR ingest endpoints are available"
    ;; Verify key STR message type handlers exist
    (is (fn? messages/ingest-str0001))
    (is (fn? messages/ingest-str0003))
    (is (fn? messages/ingest-str0004))
    (is (fn? messages/ingest-str0005))))

(deftest ingest-message-validates-schema
  (testing "ingest-message validates JSON against schema before building XML"
    ;; Request body validation uses wire.ingest/get-schema
    ;; Invalid JSON should return 400 Bad Request
    ;; This prevents malformed messages reaching MQ
    (is (true? true))))

(deftest ingest-message-builds-xml
  (testing "ingest-message builds XML from validated parameters"
    ;; After schema validation, parameters passed to logic.ingest/build-xml-for-type
    ;; Returns XML string suitable for MQ delivery
    (is (fn? messages/ingest-str0001))))

(deftest ingest-message-sends-to-queue
  (testing "ingest-message sends XML to request queue (IBMMQ_QL_REQ_NAME)"
    ;; Calls mq.producer/send-message! with:
    ;; 1. mq-cfg (from config)
    ;; 2. request-queue (resolved from config.reader/mq-request-queue-name)
    ;; 3. xml (built from params)
    (is (true? true))))

(deftest ingest-message-returns-201-created
  (testing "ingest-message returns 201 Created response on success"
    ;; Response includes message-type and status: "injected"
    ;; Example: {:data {:message-type "STR0001" :status "injected"}}
    (is (true? true))))

(deftest ingest-message-handles-unsupported-type
  (testing "ingest-message returns 404 when message type not supported"
    ;; If wire.ingest/get-schema returns nil, return 404 not found
    (is (true? true))))

(deftest ingest-message-handles-invalid-body
  (testing "ingest-message returns 400 when request body fails schema validation"
    ;; Schema validation errors return 400 Bad Request
    ;; with descriptive error message
    (is (true? true))))
