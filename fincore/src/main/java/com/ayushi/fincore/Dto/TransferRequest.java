package com.ayushi.fincore.Dto;

import lombok.Data;

@Data
public class TransferRequest {
    private Long fromAccountId;//sender
    private Long toAccountId;//receiver
    private Double amount;
    private String idempotencyKey;
}
