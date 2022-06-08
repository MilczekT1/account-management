package pl.konradboniecki.budget.accountmanagement.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import pl.konradboniecki.budget.accountmanagement.model.ActivationCode;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ActivationCodeCleanupService {
    static final long ONE_DAY = 24 * 60 * 60 * 1000L;

    private final ActivationCodeRepository activationCodeRepository;

    @Autowired
    public ActivationCodeCleanupService(ActivationCodeRepository activationCodeRepository) {
        this.activationCodeRepository = activationCodeRepository;
    }

    @Scheduled(fixedRate = ONE_DAY)
    public void cleanData() {
        Instant now = Instant.now();
        List<ActivationCode> list = activationCodeRepository.findAll().stream()
                .filter(a ->
                        !Duration.between(a.getCreated(), now).minusDays(1).isNegative()
                )
                .collect(Collectors.toList());
        activationCodeRepository.deleteAll(list);
        log.info("Cleaned {} activation codes at: {}", list.size(), now);
    }
}
