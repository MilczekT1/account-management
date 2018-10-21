@B2B
Feature: Account Creation

  Scenario: Account creation without password in response
    Given I'm authenticated with Basic Auth
    When I create an account with properties:
      | firstName | lastName | email            | password   |
      | john      | doe      | johnDoe@mail.com | passwdHash |
    Then the operation is successful
    And created account does not contain password

  Scenario: Account creation conflict
    Given I'm authenticated with Basic Auth
    And account for email johnDoe@mail.com already exist
    When I create an account with properties:
      | firstName | lastName | email            | password   |
      | john      | doe      | johnDoe@mail.com | passwdHash |
    Then the operation is unsuccessful

  Scenario: Account creation has to be authorized
    Given I'm not authenticated
    When I create an account with properties:
      | firstName | lastName | email            | password   |
      | john      | doe      | johnDoe@mail.com | passwdHash |
    Then the operation is unauthorized
