package example;

import example.domain.*;
import example.service.PaymentWorkflow;
import example.storage.InfraiClient;
import java.math.BigDecimal;

public final class PaymentWorkflowTest {
    public static void main(String[] args) {
        PaymentWorkflow workflow = new PaymentWorkflow(new InfraiClient("test-key"));
        RiskDecision decision = workflow.assess(new PaymentEvent("school", "p1", new BigDecimal("12000"), "USD", false));
        if (decision.allowed() || !decision.reason().equals("manual review")) throw new AssertionError(decision);
        System.out.println("risk rule passed");
    }
}
