package com.ayushi.fincore.Dto;

import lombok.Data;

@Data
public class TransactionRequest {
    private Long accountId;
    private Double amount;
}
