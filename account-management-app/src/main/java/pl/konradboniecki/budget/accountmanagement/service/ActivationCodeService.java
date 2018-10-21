package pl.konradboniecki.budget.accountmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.accountmanagement.model.ActivationCode;

import java.util.Optional;

@Service
public class ActivationCodeService {

    private final ActivationCodeRepository activationCodeRepository;

    @Autowired
    public ActivationCodeService(ActivationCodeRepository activationCodeRepository) {
        this.activationCodeRepository = activationCodeRepository;
    }

    public Optional<ActivationCode> findByAccountId(String id) {
        return activationCodeRepository.findByAccountId(id);
    }

    public void deleteById(String id) {
        activationCodeRepository.deleteById(id);
    }

    public ActivationCode save(ActivationCode activationCode) {
        return activationCodeRepository.save(activationCode);
    }
}
