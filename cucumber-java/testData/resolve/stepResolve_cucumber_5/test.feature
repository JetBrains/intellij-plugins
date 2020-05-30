Feature: Shopping
  Scenario: Subtraction
    Given my step definition
    Given my java8 step

  Scenario Outline: First test
    Given step <color>:
    Examples:
      | color |
      | red   |
