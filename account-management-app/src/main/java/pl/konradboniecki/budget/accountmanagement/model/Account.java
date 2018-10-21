package pl.konradboniecki.budget.accountmanagement.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.regex.Pattern;

@Entity
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Account implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "account_id")
    private String id;

    @Column(name = "family_id")
    private String familyId;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "created")
    private Instant created;

    @Column(name = "enabled")
    private boolean enabled;

    public boolean hasFamily() {
        return familyId != null;
    }

    public static boolean isEmailValid(String email) {
        if (StringUtils.isEmpty(email))
            return false;
        return Pattern.matches("(\\w||\\.)+@\\w+.[a-zA-Z]+", email);
    }
}
