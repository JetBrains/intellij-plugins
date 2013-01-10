Feature: Git Cherry-Pick When Auto-Commit is deselected

  Background:
    Given disabled auto-commit in the settings
    Given new committed files file.txt, a.txt, conflict.txt with initial content
    Given branch feature

    Given commit f5027a3 on branch feature
    """
      fix #1
      Author: John Bro
      M file.txt "feature changes"
      """

  Scenario: Simple cherry-pick
    When I cherry-pick the commit f5027a3
    Then commit dialog should be shown
    And active changelist is 'fix #1 (cherry picked from commit f5027a3)'


  Scenario: Simple cherry-pick, agree to commit
    When I cherry-pick the commit f5027a3 and commit
    Then the last commit is
    """
      fix #1
      (cherry picked from commit f5027a3)
      """
    And success notification is shown 'Cherry-pick successful'
    """
       f5027a3 fix #1
       """
    And no new changelists are created
