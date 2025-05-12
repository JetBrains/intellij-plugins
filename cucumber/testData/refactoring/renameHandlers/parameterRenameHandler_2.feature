Feature: Withdraw money (table-based)

  Some feature description.

  Scenario Outline: Withdrawing money subtracts correct amount
    Given I have <start> SEK on my account
    When I withdraw <subtract_amount<caret>> SEK
    Then My account has <end> SEK left

    Examples:
      | start | subtract_amount | end |
      | 100   | 25              | 75  |
      | 150   | 50              | 100 |
