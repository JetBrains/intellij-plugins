Feature: Withdraw money
  As a customer
  In order to avoid going to the bank
  I want to withdraw money from an ATM

  Scenario: Withdraw less money than the account has
    Given I have 200 EUR on my account
    When I withdraw 100 EUR
    Then I get 100 EUR from the ATM
    And My account has 100 EUR left
    And I am happy

  Scenario: Withdraw more money than the account has
    Given I have 50 EUR on my account
    When I withdraw 100 EUR
    Then I get 0 EUR from the ATM
    And My account has 50 EUR left
    And error message about the lack of money is displayed
    And I am an<caret>gry

  Scenario: Withdraw negative amount from the account
    Given I have 50 EUR on my account
    When I withdraw -1 EUR
    Then I get 0 EUR from the ATM
    And My account has 50 EUR left
    And error message about incorrect amount is displayed
    And I am angry