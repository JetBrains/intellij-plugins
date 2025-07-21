Feature: Simple definition rename test with regex

  Scenario: First
    Given I am <caret>angry
    And I am furious

  Scenario: Second
    Given I am angry
    Then I am furious