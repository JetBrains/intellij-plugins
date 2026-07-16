Feature: Test
  Scenario Outline: Missed Example
    Given I have tomatos named
    """
    Tomate from <country>
    """
    Examples:
      | <caret>country |
