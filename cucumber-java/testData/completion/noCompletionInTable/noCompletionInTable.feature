Feature: Suggestion in tabular data

  Scenario:
    Given a step that takes a table
      | foo | bar |
      | 42  | <caret>  |
---
BASIC 1 EQUALS
