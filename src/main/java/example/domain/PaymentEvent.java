package example.domain;

import java.math.BigDecimal;

public record PaymentEvent(String tenantId, String paymentId, BigDecimal amount, String currency, boolean trustedDevice) {}
