package com.ayushi.fincore.Service;

import com.ayushi.fincore.Model.Account;
import com.ayushi.fincore.Model.Transaction;
import com.ayushi.fincore.Model.User;
import com.ayushi.fincore.Repository.AccountRepository;
import com.ayushi.fincore.Repository.TransactionRepository;
import com.ayushi.fincore.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RedisTemplate<String, Object> redisTemplate;


//CREATE ACCOUNT
    public Account createAccount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = Account.builder()
                .accountNumber(UUID.randomUUID().toString())
                .balance(0.0)
                .user(user)
                .build();

        return accountRepository.save(account);
    }

    //DEPOSIT MONEY
    @Transactional
    public Account deposit(Long accountId, Double amount, String email) {
        if (amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        Account account = accountRepository
                .findByIdAndUserEmail(accountId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));


        account.setBalance(account.getBalance() + amount);

        Account saved = accountRepository.save(account);
        redisTemplate.opsForValue()
                .set("BALANCE_" + accountId, account.getBalance());



        //adding history
        transactionRepository.save(Transaction.builder()
                .fromAccountId(null)
                .toAccountId(accountId)
                .amount(amount)
                .type("DEPOSIT")
                .timestamp(LocalDateTime.now())
                .build());

        return saved;

    }

    //WITHDRAW MONEY
    @Transactional
    public Account withdraw(Long accountId, Double amount, String email) {
        if (amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        Account account = accountRepository
                .findByIdAndUserEmail(accountId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (account.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }
        account.setBalance(account.getBalance() - amount);

        redisTemplate.opsForValue()
                .set("BALANCE_" + accountId, account.getBalance());

        Account saved = accountRepository.save(account);
//adding history
        transactionRepository.save(Transaction.builder()
                .fromAccountId(accountId)
                .toAccountId(null)
                .amount(amount)
                .type("WITHDRAW")
                .timestamp(LocalDateTime.now())
                .build());

        return saved;
    }


// FOR UPI TRANSACTIONS
    @Transactional // if smt fails rollback for ATOMICITY
    public String transfer(Long fromId, Long toId, Double amount, String email, String idempotencyKey) {
        if (amount <= 0 || amount == null) {
            throw new RuntimeException("Invalid amount");
        }
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            throw new RuntimeException("Idempotency key required");
        }

        String txnKey = "TXN_" + idempotencyKey;

        // 🔥 Step 1: Check duplicate request
        if (Boolean.TRUE.equals(redisTemplate.hasKey(txnKey))) {
            return "Duplicate transaction ignored";
        }

        // Get sender (secure) by id and email both
        Account sender = accountRepository
                .findByIdAndUserEmail(fromId, email)
                .orElseThrow(() -> new RuntimeException("Sender account not found or access denied"));

        //  Get receiver
        Account receiver = accountRepository
                .findById(toId)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        //Prevent self-transfer
        if (fromId.equals(toId)) {
            throw new RuntimeException("Cannot transfer to same account");
        }
        //  Validate balance
        if (sender.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        // Deduct + Add
        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        //  Save both
        accountRepository.save(sender);
        accountRepository.save(receiver);
        //update redis
        redisTemplate.opsForValue().set("BALANCE_" + fromId, sender.getBalance());
        redisTemplate.opsForValue().set("BALANCE_" + toId, receiver.getBalance());

        transactionRepository.save(Transaction.builder()
                .fromAccountId(fromId)
                .toAccountId(toId)
                .amount(amount)
                .type("TRANSFER")
                .timestamp(LocalDateTime.now())
                .build());

        //store key in redis
        redisTemplate.opsForValue().set(txnKey, "DONE");

        return "transfer successful";

    }


//get transactions history
    public List<Transaction> getTransactions(Long accountId, String email) {

        // ownership check
        accountRepository.findByIdAndUserEmail(accountId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        return transactionRepository
                .findByFromAccountIdOrToAccountId(accountId, accountId);
    }

    //GET BALANCE
    public Double getBalance(Long accountId, String email) {

        String key = "BALANCE_" + accountId;

        Object cached = redisTemplate.opsForValue().get(key);

        //cache hit
        if (cached != null) {
            return (Double) cached;
        }

        // cache miss: Fetch from DB
        Account account = accountRepository
                .findByIdAndUserEmail(accountId, email)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        Double balance = account.getBalance();

        // Store in Redis
        redisTemplate.opsForValue().set(key, balance);

        return balance;
    }

}
