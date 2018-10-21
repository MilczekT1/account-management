Feature: Password change

  Background:
    Given I'm authenticated with Basic Auth
    And account for email johnDoe@mail.com already exist

  Scenario: Password change for existing user
    Given I'm authenticated with Basic Auth
    When I change password for account johnDoe@mail.com
    Then the operation is successful

  Scenario: Password change fails for missing account
    Given I'm authenticated with Basic Auth
    When I change password for account missingJohnDoe@mail.com
    Then the operation is unsuccessful

  Scenario: Password change has to be authorized
    Given I'm not authenticated
    When I change password for account johnDoe@mail.com
    Then the operation is unauthorized

