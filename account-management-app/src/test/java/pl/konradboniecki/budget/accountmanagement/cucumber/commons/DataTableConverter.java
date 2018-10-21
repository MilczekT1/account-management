package pl.konradboniecki.budget.accountmanagement.cucumber.commons;

import io.cucumber.java.DataTableType;
import pl.konradboniecki.budget.openapi.dto.model.OASAccountCreation;

import java.util.Map;

public class DataTableConverter {

    /**
     * | firstName | lastName | email            | password   |
     * | john      | doe      | johndoe@mail.com | passwdHash |
     *
     * @param entry
     * @return
     */
    @DataTableType
    public OASAccountCreation oasAccountCreationRow(Map<String, String> entry) {
        return new OASAccountCreation()
                .firstName(entry.get("firstName"))
                .lastName(entry.get("lastName"))
                .email(entry.get("email"))
                .password(entry.get("password"));
    }
}
