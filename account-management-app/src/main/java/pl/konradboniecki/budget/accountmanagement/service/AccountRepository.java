package pl.konradboniecki.budget.accountmanagement.service;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.konradboniecki.budget.accountmanagement.model.Account;

import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, String> {
    Optional<Account> findById(String id);

    Optional<Account> findByEmail(String email);

    Account save(Account entity);

    long count();

    @Transactional
    Long deleteAccountById(String aLong);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET enabled = 1 WHERE account_id = ?1", nativeQuery = true)
    void setEnabled(String accountId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE account SET password = ?1 WHERE account_id = ?2", nativeQuery = true)
    void changePassword(String newPassword, String accountId);

}
