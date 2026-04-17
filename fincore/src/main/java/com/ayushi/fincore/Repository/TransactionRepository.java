package com.ayushi.fincore.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import com.ayushi.fincore.Model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    List<Transaction> findByFromAccountIdOrToAccountId(Long fromId,Long toId);
}
