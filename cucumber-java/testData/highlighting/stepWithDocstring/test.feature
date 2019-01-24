Feature: Basic Arithmetic
  Scenario: Does not highlight step variable
    Then the "<info descr="null">some variable</info>" is not highlighted:
      """
      This is a doc string.
      """