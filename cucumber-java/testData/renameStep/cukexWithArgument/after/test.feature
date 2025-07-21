Feature: Rename of cukex with argument.

  Scenario: First
    Given unrelated step
    And I possess the amount of 42 USD on my acc
    Then unrelated step

  Scenario: Second
    Given I possess the amount of 25 USD on my acc
    When unrelated step
    Then I possess the amount of 0 USD on my acc
