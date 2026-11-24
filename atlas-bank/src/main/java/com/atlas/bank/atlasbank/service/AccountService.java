package com.atlas.bank.atlasbank.service;

import com.atlas.bank.atlasbank.model.Account;
import com.atlas.bank.atlasbank.model.Transaction;
import com.atlas.bank.atlasbank.repository.AccountRepository;
import com.atlas.bank.atlasbank.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public Account create(Account account) {
        return accountRepository.save(account);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account findById(Long id) {
        return accountRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Cuenta no encontrada")
        );
    }

    @Transactional
    public Transaction transfer(Long fromId, Long toId, BigDecimal amount) {

        //buscar cuenta
        Account from = accountRepository.findById(fromId).orElseThrow(
                () -> new RuntimeException("Cuenta no encontrada"));
        Account to = accountRepository.findById(toId).orElseThrow(
                () -> new RuntimeException("Cuenta no encontrada"));

        //validar la cuenta que esté activa
        if (!"ACTIVE".equals(from.getStatus())) {
            throw new RuntimeException("La cuenta origen no esta activa");
        }

        if (!"ACTIVE".equals(to.getStatus())) {
            throw new RuntimeException("La cuenta destino no esta activa");
        }

        //validar los fondos
        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Fondos insuficientes");
        }

        //calcular comisiones
        BigDecimal fee;
        if (!"SAVINGS".equals(from.getStatus())) {
            fee = amount.multiply(new BigDecimal("0.01"));
        } else if ("CHEKING".equals(from.getType())) {
            fee = amount.multiply(new BigDecimal("0.015"));
        } else {
            fee = BigDecimal.ZERO;
        }

        //Actualización de saldos
        from.setBalance(from.getBalance().subtract(amount).subtract(fee));
        to.setBalance(to.getBalance().add(amount));
        accountRepository.save(from);
        accountRepository.save(to);

        //crear transacción
        Transaction transaction = new Transaction();
        transaction.setType("TRANSFER");
        transaction.setSourceAccountId(fromId);
        transaction.setTargetAccountId(toId);
        transaction.setAmount(amount);
        transaction.setFee(fee);
        transaction.setStatus("EXECUTED");

        return transactionRepository.save(transaction);

    }

}
