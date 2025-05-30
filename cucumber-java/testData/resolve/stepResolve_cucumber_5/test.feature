Feature: Shopping

  Scenario: Subtraction
    Given my step definition
    Given my java8 step


  Scenario: my test
    Given first regex
    Given second regex

  Scenario Outline: First test
    Given step <color>:
    Examples:
      | color |
      | red   |

  Scenario Outline: Single caret test
    Given my another step definition with param "<param>"
    Examples:
      | param |
      | hello |
      | there |

  Scenario Outline: Double caret test
    Given my another step definition with param "<<param>>"
    Examples:
      | param |
      | hello |
      | there |
