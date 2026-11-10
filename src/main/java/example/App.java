package example;

import example.config.ServiceConfig;
import example.domain.PaymentEvent;
import example.service.PaymentWorkflow;
import example.storage.InfraiClient;
import java.math.BigDecimal;
import java.util.UUID;

public final class App {
    public static void main(String[] args) throws Exception {
        ServiceConfig config = ServiceConfig.fromEnvironment();
        InfraiClient storage = new InfraiClient(config.apiKey());
        PaymentWorkflow workflow = new PaymentWorkflow(storage);
        PaymentEvent event = new PaymentEvent("course-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16), "pay-1001", new BigDecimal("49.00"), "USD", true);
        System.out.println(workflow.assess(event));
        String bucket = "tenant-" + event.tenantId();
        storage.createBucket(bucket);
        System.out.println("Created bucket: " + bucket);
        try {
            System.out.println(workflow.prepareAuditUpload(event, "{\"status\":\"approved\"}"));
        } finally {
            storage.deleteBucket(bucket);
            storage.verifyBucketDeleted(bucket);
            System.out.println("Verified deleted bucket: " + bucket);
        }
    }
}
