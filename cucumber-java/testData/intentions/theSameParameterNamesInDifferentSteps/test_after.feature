Feature: test

  Scenario Outline: test
    When I am on <p>
    Then I should see <p1>
    When I follow "<p2>"
    Then I should see <p3> within "<argument>"
    Examples:
      | p             | p1                   | p2       | p3                   | argument                         |
      | the help page | "What is a mention?" | Mentions | "What is a mention?" | .faq_question_mentions .question |
