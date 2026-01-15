Feature: Escaped Braces

  Scenario: Escaped parameter placeholder
    Then I have 1 cucumber in my belly {int} oops
    Then I have 0 cucumbers in my belly {int} oops
