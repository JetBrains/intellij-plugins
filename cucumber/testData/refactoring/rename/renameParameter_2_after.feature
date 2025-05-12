Feature: Withdraw money (table-based)

  Some description

  Scenario Outline: Withdrawing money subtracts correct amount
    Given I have <start> USD on my account
    When I withdraw <subtract_amount> USD
    Then My account has <end> USD left

    Examples:
      | start | subtract_amount | end |
      | 100   | 25              | 75  |
      | 150   | 50              | 100 |
