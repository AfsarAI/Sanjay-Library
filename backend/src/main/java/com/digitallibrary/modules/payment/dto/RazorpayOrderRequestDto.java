package com.digitallibrary.modules.payment.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class RazorpayOrderRequestDto {

    @NotNull(message = "Library ID is required")
    private Long libraryId;

    private Long subscriptionId;

    private BigDecimal amount;

    public RazorpayOrderRequestDto() {
    }

    public Long getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(Long libraryId) {
        this.libraryId = libraryId;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
