# sim-bacen-spb

SPB (Sistema de Pagamentos Brasileiros) simulator implementing Brazilian financial message protocols.

## IBM MQ Queue Configuration

The simulator implements explicit queue separation for IBM MQ message routing:

### Request Queue (IBMMQ_QL_REQ_NAME)
- **Purpose:** Inbound financial institution requests
- **Consumers:** MQ worker (continuous polling)
- **Publishers:** Test endpoints (/api/ingest/str*), IF via external MQ
- **Typical value:** `QL.REQ.00000000.99999999.01`
- **Environment variable:** `IBMMQ_QL_REQ_NAME`

The MQ worker polls this queue continuously and processes:
- STR (Structured Transfer Request) messages from financial institutions
- Test/injection messages from development endpoints

### Response Queue (IBMMQ_QL_RSP_NAME)
- **Purpose:** Outbound responses and autonomous BACEN messages
- **Consumers:** External systems monitoring BACEN responses
- **Publishers:** Response handlers, SLB ingest (/api/ingest/slb*), producer
- **Typical value:** `QL.RSP.00000000.99999999.01`
- **Environment variable:** `IBMMQ_QL_RSP_NAME`

The response queue is where:
- Reply messages (R1, R2, R3 responses to STR requests) are published
- Autonomous SLB messages (SLB0001, SLB0002, SLB0006, SLB0007) are published
- External systems consume BACEN responses

### Configuration at Startup

Queue names are resolved from environment variables **once at component startup**, not on each message:

```bash
# Backend component initialization
IBMMQ_QL_REQ_NAME=QL.REQ.00000000.99999999.01 \
IBMMQ_QL_RSP_NAME=QL.RSP.00000000.99999999.01 \
java -jar backend.jar
```

All subsequent polling and sending uses the initialized values. Changing env vars after startup has no effect on running components.

### Graceful Degradation

If a queue doesn't exist (MQRC 2085):
- **Producer:** Returns `false`, logs warning with queue name and timestamp. No exception thrown.
- **Consumer:** Logs warning, returns empty message list, continues polling at interval.
- **Service:** Remains operational. Fix the queue and resume normal operation.

### Code Architecture

- **Consumer** (`infrastructure/mq/consumer.clj`): Accepts `request-queue-name` as parameter
- **Producer** (`infrastructure/mq/producer.clj`): Accepts `queue-name` as parameter
- **MQ Worker** (`components/mq_worker.clj`): Resolves both queue names via config.reader at startup
- **Config Reader** (`config/reader.clj`): Defines `mq-request-queue-name` and `mq-response-queue-name` functions
- **SLB Ingest** (`controllers/slb/ingest.clj`): Uses `mq-response-queue-name` for autonomous messages
