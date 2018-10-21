package pl.konradboniecki.budget.accountmanagement.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "activation_code")
public class ActivationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "activation_code_id")
    private String id;
    @Column(name = "account_id")
    private String accountId;
    @Column(name = "activation_code")
    private String activationCode;
    @Column(name = "creation_time")
    private Instant created;

    public ActivationCode(String accountId, String activationCode) {
        setCreated(Instant.now());
        setAccountId(accountId);
        setActivationCode(activationCode);
    }
}
