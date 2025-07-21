Feature: Rename of cukex with argument.

  Scenario: First
    Given unrelated step
    And I have 42 EUR on my account
    Then unrelated step

  Scenario: Second
    Given I have<caret> 25 EUR on my account
    When unrelated step
    Then I have 0 EUR on my account
