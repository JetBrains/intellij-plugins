Feature: Some interesting feature

  Scenario Outline: Consumption of a cookie
    Given I am hungry
    When I ask for advice
    Then I eat a <color> cookie
    Examples:
      | color |
      | red   |
