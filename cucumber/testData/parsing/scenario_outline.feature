Feature: Cucumber stock keeping
  Scenario Outline: eating
    Given there are <start> cucumbers

    Examples: Good
      | start |
      |  12   |
      |  20   |

    Examples: Bad
      | start |
      | 15 |