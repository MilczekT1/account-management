Feature: Account Activation

  Background:
    Given I'm authenticated with Basic Auth
    And I create an account with properties:
      | firstName | lastName | email            | password   |
      | john      | doe      | johnDoe@mail.com | passwdHash |
    And account for email johnDoe@mail.com already exist

    # Activation code

  Scenario: Creation of activation code for account
    Given I'm authenticated with Basic Auth
    When I create activation code for account johnDoe@mail.com
    Then the operation is successful
    And response contains activation code

  Scenario: Creation of activation code for missing account
    Given I'm authenticated with Basic Auth
    When I create activation code for missing account missingJohnDoe@mail.com
    Then account is not found

  Scenario: Creation of activation code has to be authorized
    Given I'm not authenticated
    When I create activation code for account johnDoe@mail.com
    Then the operation is unauthorized

    # Account activation

  Scenario: Activation of an account
    Given I'm authenticated with Basic Auth
    And I create activation code for account johnDoe@mail.com
    When I'm not authenticated
    And I activate account johnDoe@mail.com
    Then I'm redirected to login page
    And account johnDoe@mail.com is enabled

  Scenario: Activation of already enabled account
    Given new activation code and enabled account johnDoe@mail.com
    And I'm not authenticated
    When I activate account johnDoe@mail.com
    Then I'm redirected to login page

  Scenario: Missing account activation redirects to registration form
    Given I'm not authenticated
    When I activate missing account missingJohnDoe@mail.com
    Then I'm redirected to registration form
