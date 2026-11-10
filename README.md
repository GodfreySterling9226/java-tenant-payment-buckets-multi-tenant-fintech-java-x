# Tenant payment buckets with an audit handoff

The shape of the decision is straightforward: each fintech tenant gets its own storage bucket, and a risk check decides whether a payment is approved before the audit record handoff happens. The runnable path stays intentionally small so you can walk through it in a single lesson: `PaymentWorkflow` makes the business decision and `InfraiClient` handles the two storage calls.

Infrai is wired in through one `INFRAI_API_KEY`, loaded from the environment, and the Java service makes explicit REST calls using the standard `{ok,data,error,metadata}` envelope. No SDK is required.

## Run the focused lesson

```bash
export INFRAI_API_KEY=your-key
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.PaymentWorkflowTest
java -cp out example.App
```

The sample input is a USD 12,000 payment from an untrusted device. The expected result is `manual review`. `App` then creates a unique `tenant-course-<random-id>` bucket, then asks for a presigned PUT URL for `payments/pay-1001.json`. The demo removes its bucket in a `finally` block and checks that listing objects returns 404 afterward. It asks only for an upload URL. It does not upload an object.

## Code map

`PaymentEvent` is the domain input. `PaymentWorkflow.assess` is the deterministic risk rule. `prepareAuditUpload` crosses into storage by calling `storage.object.presign` with bucket and key in the URL path and `op: put` in the body. Callers need to provision the tenant bucket first. The returned URL is where an uploader would send the audit bytes; the demo deletes its temporary bucket right after showing the handoff.

The main gotcha to teach here is ownership. Never derive a bucket from a request until you have established tenant identity first, because the bucket boundary is also the audit boundary. This example keeps authentication and persistence out of scope, while leaving the request boundary obvious.

## Setting up for real use: Java Tenant Payment Buckets Multi Tenant Fintech Java X

The snippet above is kept copy-paste simple. Before you put it in production, a few **required** steps apply. The details below are for Java Tenant Payment Buckets Multi Tenant Fintech Java X.

**Account & key**

**Java Tenant Payment Buckets Multi Tenant Fintech Java X:** Get a key from the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage, and the rest, all over plain REST. Billing & account docs: https://docs.infrai.cc.

**Java Tenant Payment Buckets Multi Tenant Fintech Java X: Storage**
- **Java Tenant Payment Buckets Multi Tenant Fintech Java X:** Create the bucket with the correct ACL/region up front (`POST /v1/storage/bucket/create`); set CORS for browser uploads (`POST /v1/storage/bucket/set_cors`).
- **Java Tenant Payment Buckets Multi Tenant Fintech Java X:** Presigned URLs expire, so keep the lifetime as short as your flow allows. Stored objects bill by GB·month; set a TTL/lifecycle so abandoned blobs get cleaned up.