package com.ayushi.fincore.Controller;

import com.ayushi.fincore.Dto.TransactionRequest;
import com.ayushi.fincore.Dto.TransferRequest;
import com.ayushi.fincore.Model.Account;
import com.ayushi.fincore.Model.Transaction;
import com.ayushi.fincore.Service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public Account createAccount(Authentication authentication) {

        String email = authentication.getName();

        return accountService.createAccount(email);
    }
    @PostMapping("/deposit")
    public Account deposit(@RequestBody TransactionRequest request,
                           Authentication authentication) {

        String email = authentication.getName();

        return accountService.deposit(
                request.getAccountId(),
                request.getAmount(),
                email
        );
    }
    @PostMapping("/withdraw")
    public Account withdraw(@RequestBody TransactionRequest request,
                            Authentication authentication) {

        String email = authentication.getName();

        return accountService.withdraw(
                request.getAccountId(),
                request.getAmount(),
                email
        );
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferRequest request,
                           Authentication authentication) {


        return accountService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                authentication.getName(),
                request.getIdempotencyKey()
        );

    }


    @GetMapping("/transactions/{accountId}")
    public List<Transaction> getTransactions(@PathVariable Long accountId,
                                             Authentication authentication) {

        String email = authentication.getName();

        return accountService.getTransactions(accountId, email);
    }


    @GetMapping("/balance/{accountId}")
    public Double getBalance(@PathVariable Long accountId,
                             Authentication authentication) {

        return accountService.getBalance(accountId, authentication.getName());
    }
}
