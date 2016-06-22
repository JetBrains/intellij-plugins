Feature: Cucumber stock keeping
  Scenario Outline: eating
    Given there are <start> cucumbers
    Then there are <total> cucumbers

    Examples:
      | start | total |
      |  12   |   13  |
      |  20   |   20  |
      |       |    1  |
      |       |       |
