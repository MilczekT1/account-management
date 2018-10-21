package pl.konradboniecki.budget.accountmanagement.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.konradboniecki.budget.accountmanagement.model.ActivationCode;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivationCodeRepository extends CrudRepository<ActivationCode, String> {
    Optional<ActivationCode> findById(String aLong);

    Optional<ActivationCode> findByAccountId(String aLong);

    List<ActivationCode> findAll();

    ActivationCode save(ActivationCode entity);
//    long count();
//    void deleteById(Long aLong);
//    boolean existsById(Long id);
}
