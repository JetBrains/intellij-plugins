Feature: Resolve from Java 8 step defs to step usages.

  Scenario: First
    Given I am happy
    And I am angry
    Then I am happy

  Scenario: Second
    Given I am happy
    Then unrelated step
