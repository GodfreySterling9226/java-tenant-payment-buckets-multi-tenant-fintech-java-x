package example.service;

import example.domain.*;
import example.storage.InfraiClient;
import java.io.IOException;

public final class PaymentWorkflow {
    private final InfraiClient storage;
    public PaymentWorkflow(InfraiClient storage) { this.storage = storage; }
    public RiskDecision assess(PaymentEvent event) {
        if (event.amount().signum() <= 0) return new RiskDecision(false, "amount must be positive");
        if (event.amount().doubleValue() > 10000 && !event.trustedDevice()) return new RiskDecision(false, "manual review");
        return new RiskDecision(true, "approved");
    }
    public String prepareAuditUpload(PaymentEvent event, String auditJson) throws IOException, InterruptedException {
        String bucket = "tenant-" + event.tenantId();
        // The caller provisions the tenant bucket before requesting an upload.
        // canonical storage.object.presign handoff: signed URL follows the risk decision.
        return storage.presignPut(bucket, "payments/" + event.paymentId() + ".json");
    }
}
