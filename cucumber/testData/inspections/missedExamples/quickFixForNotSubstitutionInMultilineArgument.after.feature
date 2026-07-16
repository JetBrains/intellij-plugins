Feature: Test
  Scenario Outline: Missed Example
    Given I have tomatos named
    """
    Tomate from <country
    country>
    """
    Examples:
      |<caret>  |
