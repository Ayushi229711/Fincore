package com.ayushi.fincore.dto;

import lombok.Data;

@Data
public class TransactionRequest {
    private Long accountId;
    private Double amount;
}
