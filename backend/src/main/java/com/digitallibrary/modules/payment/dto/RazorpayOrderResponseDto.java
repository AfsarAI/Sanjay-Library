package com.digitallibrary.modules.payment.dto;

import java.math.BigDecimal;

public class RazorpayOrderResponseDto {

    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private Long paymentRecordId;

    public RazorpayOrderResponseDto() {
    }

    public RazorpayOrderResponseDto(String orderId, BigDecimal amount, String currency, String keyId, Long paymentRecordId) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
        this.paymentRecordId = paymentRecordId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public Long getPaymentRecordId() {
        return paymentRecordId;
    }

    public void setPaymentRecordId(Long paymentRecordId) {
        this.paymentRecordId = paymentRecordId;
    }
}
