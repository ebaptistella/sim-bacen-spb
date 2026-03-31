# e2e/features/ibmmq_queue_separation.feature
Feature: IBM MQ Queue Separation Refactor
  As a simulator operator
  I want to ensure that IBM MQ messages are routed to the correct queues
  So that request flows (IF → BACEN) and autonomous flows (BACEN-initiated) are separated

  Background:
    Given the simulator is running with configured env vars
    And the request queue "IBMMQ_QL_REQ_NAME" is available
    And the response queue "IBMMQ_QL_RSP_NAME" is available

  # Consumer Tests
  Scenario: Consumer polls from configured request queue
    Given the MQ worker is initialized
    When a request message arrives in the configured request queue
    Then the message is consumed successfully
    And the message is routed to the appropriate handler

  Scenario: Consumer handles empty request queue
    Given the MQ worker is polling
    When no messages are available in the request queue
    Then the consumer returns without error
    And polling continues at the scheduled interval

  Scenario: Consumer configuration is read from environment at startup
    Given the environment variable "IBMMQ_QL_REQ_NAME" is set to "QL.REQ.08253539.00038166.01"
    When the MQ worker initializes
    Then it resolves the request queue from the environment variable
    And all subsequent polls target that queue
    And the queue name is not re-read on each poll

  # Producer Response Queue Tests
  Scenario: Producer sends response to configured response queue
    Given a response handler is triggered
    When a response message is generated
    Then the destination queue is resolved from "IBMMQ_QL_RSP_NAME"
    And the message is sent to that queue
    And the message delivery succeeds

  Scenario: Producer sends error message to response queue
    Given an operation has failed
    When the error handler generates an error message
    Then the destination queue is resolved from "IBMMQ_QL_RSP_NAME"
    And the error message is sent to that queue

  Scenario: Producer handles gracefully when response queue does not exist
    Given the environment variable "IBMMQ_QL_RSP_NAME" points to a non-existent queue
    When the producer attempts to send a message
    Then a warning is logged with queue name, message ID, and error code 2085
    And the operation completes without crashing
    And the message is not lost (queued or persisted locally)

  Scenario: Producer code contains no hardcoded QR.* queue names
    Given the producer module is being reviewed
    When code inspection is performed
    Then no literal strings matching "QR\.REQ", "QR\.RSP", or "QR\.REP" are found
    And all queue names come from environment variables or configuration

  # SLB Ingest & Autonomous Messages
  Scenario: SLB0002 autonomous message sent via HTTP to response queue
    Given the HTTP ingest endpoint for SLB0002 is invoked
    When a valid SLB0002 JSON payload is provided
    Then an SLB0002 XML message is built
    And the message is sent to the queue specified by "IBMMQ_QL_RSP_NAME"
    And the HTTP response is 200 OK

  Scenario: SLB0006 autonomous message sent via HTTP to response queue
    Given the HTTP ingest endpoint for SLB0006 is invoked
    When a valid SLB0006 JSON payload is provided
    Then an SLB0006 XML message is built
    And the message is sent to the queue specified by "IBMMQ_QL_RSP_NAME"
    And the HTTP response is 200 OK

  Scenario: SLB0007 autonomous message sent via HTTP to response queue
    Given the HTTP ingest endpoint for SLB0007 is invoked
    When a valid SLB0007 JSON payload is provided
    Then an SLB0007 XML message is built
    And the message is sent to the queue specified by "IBMMQ_QL_RSP_NAME"
    And the HTTP response is 200 OK

  Scenario: SLB ingest code contains no hardcoded request queue names
    Given the SLB builder and ingest controller modules are being reviewed
    When code inspection is performed
    Then no literal strings matching "QL\.REQ\.00000000\.99999999" are found
    And all queue destinations come from environment variables

  Scenario: SLB autonomous message is independent of request flow
    Given the HTTP ingest endpoint for SLB0002 is invoked
    When a valid SLB0002 payload is provided
    And no corresponding request message exists in the request queue
    Then the SLB0002 message is sent to the response queue successfully
    And no error or missing-dependency condition is raised
    And the HTTP response is 200 OK

  # End-to-End Integration Tests
  Scenario: Request-response flow remains functional after refactoring
    Given the simulator has environment vars configured
    And a financial institution sends a request via the request queue
    When the consumer polls the configured request queue
    Then the message is received successfully
    And the message is processed by the handler
    And a response is generated
    And the response is sent to the configured response queue
    And the entire flow completes without errors

  Scenario: Autonomous SLB flow works independently
    Given the HTTP ingest endpoint is accessible
    When POST /api/ingest/slb0002 is invoked with valid payload
    Then the SLB0002 XML message is built
    And the message is sent to the response queue
    And messages sent to the response queue are readable by external consumers
    And the HTTP response indicates success

  Scenario: Multiple SLB autonomous messages can be sent in sequence
    Given the HTTP ingest endpoints are available
    When I send POST /api/ingest/slb0002 with valid payload
    And I send POST /api/ingest/slb0006 with valid payload
    And I send POST /api/ingest/slb0007 with valid payload
    Then all three messages are sent to the response queue
    And all HTTP responses indicate success
    And all messages are independently processable

  Scenario: Full E2E suite passes without regression
    Given the refactored simulator is running
    When the full E2E test suite is executed
    Then all tests pass
    And no regressions are detected in request-response flows
    And no regressions are detected in autonomous message flows
    And all message routing uses environment-configured queues

  Scenario: Queue not found scenario is handled gracefully
    Given docker-compose is modified to point to a non-existent queue
    When the simulator attempts to send a message
    Then logs contain a warning with the non-existent queue name
    And logs contain the MQ error code 2085
    And the simulator service does not crash
    And normal operation can resume once the queue is corrected

  # Edge Cases & Validation
  Scenario: Consumer validates that env vars are set at startup
    Given the environment variable "IBMMQ_QL_REQ_NAME" is empty or missing
    When the MQ worker initializes
    Then a startup log warning is generated
    And the initialization completes with fallback or explicit error

  Scenario: Producer validates that env vars are set at startup
    Given the environment variable "IBMMQ_QL_RSP_NAME" is empty or missing
    When the producer component initializes
    Then a startup log warning is generated
    And the initialization completes with fallback or explicit error

  Scenario: Environment variables are immutable after component startup
    Given the MQ worker has initialized with "IBMMQ_QL_REQ_NAME"
    When the environment variable is changed after startup
    Then the consumer continues to use the original queue name
    And the changed environment variable is not re-read mid-stream

  Scenario: SLB ingest respects configured response queue throughout request lifetime
    Given the HTTP ingest endpoint receives a request
    When the request is being processed
    Then the response queue is not re-resolved on each step
    And the same queue name is used from start to finish
