package cl.pagoseguro.accounts.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {

    boolean existsByAccountNumber(String accountNumber);
}
