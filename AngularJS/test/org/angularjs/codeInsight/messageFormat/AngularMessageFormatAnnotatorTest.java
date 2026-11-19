package org.angularjs.codeInsight.messageFormat;

import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightFixture;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightProjectFixture;

@TestApplication
public class AngularMessageFormatAnnotatorTest {
  private final TestFixture<Project> projectFixture = codeInsightProjectFixture();
  private final TestFixture<CodeInsightTestFixture> codeInsight = codeInsightFixture(projectFixture);

  public static final String[] PLURAL = new String[]{"other", "one", "many", "few", "two", "zero", "=0", "=1", "=2", "=3"};

  @BeforeEach
  void setUpTestDataPath() {
    codeInsight.get().setTestDataPath(AngularTestUtil.getBaseTestDataPath(AngularMessageFormatAnnotatorTest.class));
  }

  @Test
  public void testCase1() {
    doTest("case1.html");
  }

  @Test
  public void testCase2() {
    doTest("case2.html");
  }

  @Test
  public void testCase3() {
    doTest("case3.html");
  }

  @Test
  public void testCase4() {
    doTest("case4.html");
  }

  @Test
  public void testCase5() {
    doTest("case5.html");
  }

  @Test
  public void testCompletion1() {
    doCompletionTest("completion1.html", PLURAL);
  }

  @Test
  public void testCompletion2() {
    doCompletionTest("completion2.html", "one", "many");
  }

  @Test
  public void testCompletion3() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletionTyping("completion3_before.html", "3\n", "completion3_after.html", "controller.js", "angular.js");
  }

  @Test
  public void testCompletion4() {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.testCompletionTyping("completion4_before.html", "3\n", "completion4_after.html", "controller.js", "angular.js");
  }

  private void doCompletionTest(final String fileName, final String... variants) {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles(fileName, "controller.js", "angular.js");
    myFixture.completeBasic();
    myFixture.assertPreferredCompletionItems(0, variants);
  }

  public void doTest(@NotNull final String fileName) {
    CodeInsightTestFixture myFixture = codeInsight.get();
    myFixture.configureByFiles(fileName, "controller.js", "angular.js");
    myFixture.checkHighlighting();
  }
}
