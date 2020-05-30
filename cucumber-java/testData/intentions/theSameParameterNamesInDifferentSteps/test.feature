Feature: test
  Scenario: tes<caret>t
    When I am on the help page
    Then I should see "What is a mention?"
    When I follow "Mentions"
    Then I should see "What is a mention?" within ".faq_question_mentions .question"
