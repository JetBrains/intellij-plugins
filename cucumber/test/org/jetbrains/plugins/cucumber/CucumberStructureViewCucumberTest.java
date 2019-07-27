package org.jetbrains.plugins.cucumber;

import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import static com.intellij.testFramework.PlatformTestUtil.assertTreeEqual;
import static com.intellij.testFramework.PlatformTestUtil.expandAll;

public class CucumberStructureViewCucumberTest extends CodeInsightFixtureTestCase {
  public void testGherkin4() {
    doTest(
      "Feature: Git Cherry-Pick When Auto-Commit is deselected\n" +
      "\n" +
      "  Background:\n" +
      "    Given disabled auto-commit in the settings\n" +
      "    Given new committed files file.txt, a.txt, conflict.txt with initial content\n" +
      "    Given branch feature\n" +
      "\n" +
      "    Given commit f5027a3 on branch feature\n" +
      "    \"\"\"\n" +
      "      fix #1\n" +
      "      Author: John Bro\n" +
      "      M file.txt \"feature changes\"\n" +
      "      \"\"\"\n" +
      "\n" +
      "  Scenario: Simple cherry-pick\n" +
      "    When I cherry-pick the commit f5027a3\n" +
      "    Then commit dialog should be shown\n" +
      "    And active changelist is 'fix #1 (cherry picked from commit f5027a3)'\n" +
      "\n" +
      "\n" +
      "  Scenario: Simple cherry-pick, agree to commit\n" +
      "    When I cherry-pick the commit f5027a3 and commit\n" +
      "    Then the last commit is\n" +
      "    \"\"\"\n" +
      "      fix #1\n" +
      "      (cherry picked from commit f5027a3)\n" +
      "      \"\"\"\n" +
      "    And success notification is shown 'Cherry-pick successful'\n" +
      "    \"\"\"\n" +
      "       f5027a3 fix #1\n" +
      "       \"\"\"\n" +
      "    And no new changelists are created\n",

      "-Feature: Git Cherry-Pick When Auto-Commit is deselected\n" +
      " -Background\n" +
      "  Given: disabled auto-commit in the settings\n" +
      "  Given: new committed files file.txt, a.txt, conflict.txt with initial content\n" +
      "  Given: branch feature\n" +
      "  Given: commit f5027a3 on branch feature\n" +
      " -Scenario: Simple cherry-pick\n" +
      "  When: I cherry-pick the commit f5027a3\n" +
      "  Then: commit dialog should be shown\n" +
      "  And: active changelist is 'fix #1 (cherry picked from commit f5027a3)'\n" +
      " -Scenario: Simple cherry-pick, agree to commit\n" +
      "  When: I cherry-pick the commit f5027a3 and commit\n" +
      "  Then: the last commit is\n" +
      "  And: success notification is shown 'Cherry-pick successful'\n" +
      "  And: no new changelists are created"
    );
  }

  public void testGherkin6() {
    doTest(
      "Feature: test\n" +
      "  Rule: test\n" +
      "    Example: myExample\n" +
      "      Given my step one\n" +
      "      And my step two\n" +
      "      When 2 ninjas meet, they will fight\n" +
      "      Then one ninja dies (but not me)\n" +
      "      And there is one ninja less alive\n" +
      "\n" +
      "    Example: Only One -- One alive\n" +
      "      Given there is only 1 ninja alive\n" +
      "      Then he (or she) will live forever ;-)\n" +
      "\n" +
      "  Rule: There can be Two (in some cases)\n" +
      "    Example: Two -- Dead and Reborn as Phoenix",

      "-Feature: test\n" +
      " -Rule: test\n" +
      "  -Example: myExample\n" +
      "   Given: my step one\n" +
      "   And: my step two\n" +
      "   When: 2 ninjas meet, they will fight\n" +
      "   Then: one ninja dies (but not me)\n" +
      "   And: there is one ninja less alive\n" +
      "  -Example: Only One -- One alive\n" +
      "   Given: there is only 1 ninja alive\n" +
      "   Then: he (or she) will live forever ;-)\n" +
      " -Rule: There can be Two (in some cases)\n" +
      "  Example: Two -- Dead and Reborn as Phoenix"
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
