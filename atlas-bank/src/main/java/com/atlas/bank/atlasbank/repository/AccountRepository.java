package com.atlas.bank.atlasbank.repository;

import com.atlas.bank.atlasbank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
