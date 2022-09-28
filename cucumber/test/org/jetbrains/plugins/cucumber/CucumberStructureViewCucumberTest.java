package org.jetbrains.plugins.cucumber;

import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import static com.intellij.testFramework.PlatformTestUtil.assertTreeEqual;
import static com.intellij.testFramework.PlatformTestUtil.expandAll;

public class CucumberStructureViewCucumberTest extends CodeInsightFixtureTestCase {
  public void testGherkin4() {
    doTest(
      """
        Feature: Git Cherry-Pick When Auto-Commit is deselected

          Background:
            Given disabled auto-commit in the settings
            Given new committed files file.txt, a.txt, conflict.txt with initial content
            Given branch feature

            Given commit f5027a3 on branch feature
            ""\"
              fix #1
              Author: John Bro
              M file.txt "feature changes"
              ""\"

          Scenario: Simple cherry-pick
            When I cherry-pick the commit f5027a3
            Then commit dialog should be shown
            And active changelist is 'fix #1 (cherry picked from commit f5027a3)'


          Scenario: Simple cherry-pick, agree to commit
            When I cherry-pick the commit f5027a3 and commit
            Then the last commit is
            ""\"
              fix #1
              (cherry picked from commit f5027a3)
              ""\"
            And success notification is shown 'Cherry-pick successful'
            ""\"
               f5027a3 fix #1
               ""\"
            And no new changelists are created
        """,

      """
        -Feature: Git Cherry-Pick When Auto-Commit is deselected
         -Background
          Given: disabled auto-commit in the settings
          Given: new committed files file.txt, a.txt, conflict.txt with initial content
          Given: branch feature
          Given: commit f5027a3 on branch feature
         -Scenario: Simple cherry-pick
          When: I cherry-pick the commit f5027a3
          Then: commit dialog should be shown
          And: active changelist is 'fix #1 (cherry picked from commit f5027a3)'
         -Scenario: Simple cherry-pick, agree to commit
          When: I cherry-pick the commit f5027a3 and commit
          Then: the last commit is
          And: success notification is shown 'Cherry-pick successful'
          And: no new changelists are created"""
    );
  }

  public void testGherkin6() {
    doTest(
      """
        Feature: test
          Rule: test
            Example: myExample
              Given my step one
              And my step two
              When 2 ninjas meet, they will fight
              Then one ninja dies (but not me)
              And there is one ninja less alive

            Example: Only One -- One alive
              Given there is only 1 ninja alive
              Then he (or she) will live forever ;-)

          Rule: There can be Two (in some cases)
            Example: Two -- Dead and Reborn as Phoenix""",

      """
        -Feature: test
         -Rule: test
          -Example: myExample
           Given: my step one
           And: my step two
           When: 2 ninjas meet, they will fight
           Then: one ninja dies (but not me)
           And: there is one ninja less alive
          -Example: Only One -- One alive
           Given: there is only 1 ninja alive
           Then: he (or she) will live forever ;-)
         -Rule: There can be Two (in some cases)
          Example: Two -- Dead and Reborn as Phoenix"""
    );
  }

  protected void doTest(@NotNull String fileContent, @NotNull String expectedResult) {
    myFixture.configureByText("test.feature", fileContent);
    myFixture.testStructureView(component -> {
      expandAll(component.getTree());
      PlatformTestUtil.waitForPromise(component.select(component.getTreeModel().getCurrentEditorElement(), false));
      assertTreeEqual(component.getTree(), expectedResult);
    });
  }
}
