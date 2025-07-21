Feature: Rename of cukex with argument and alternative text.

  Scenario: First
    Given unrelated step
    And I <caret>have few feelings about that
    Then unrelated step
    And I have few feelings about that

  Scenario: Second
    Given I have no feeling about this
    When unrelated step
    And I have many feelings about that
