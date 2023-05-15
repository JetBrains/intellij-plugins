package org.angularjs.codeInsight.messageFormat;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

public class AngularMessageFormatAnnotatorTest extends BasePlatformTestCase {
  public static final String[] PLURAL = new String[]{"other", "one", "many", "few", "two", "zero", "=0", "=1", "=2", "=3"};

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(AngularMessageFormatAnnotatorTest.class);
  }

  public void testCase1() {
    doTest("case1.html");
  }

  public void testCase2() {
    doTest("case2.html");
  }

  public void testCase3() {
    doTest("case3.html");
  }

  public void testCase4() {
    doTest("case4.html");
  }

  public void testCase5() {
    doTest("case5.html");
  }

  public void testCompletion1() {
    doCompletionTest("completion1.html", PLURAL);
  }

  public void testCompletion2() {
    doCompletionTest("completion2.html", "one", "many");
  }

  public void testCompletion3() {
    myFixture.testCompletionTyping("completion3_before.html", "3\n", "completion3_after.html", "controller.js", "angular.js");
  }

  public void testCompletion4() {
    myFixture.testCompletionTyping("completion4_before.html", "3\n", "completion4_after.html", "controller.js", "angular.js");
  }

  private void doCompletionTest(final String fileName, final String... variants) {
    myFixture.configureByFiles(fileName, "controller.js", "angular.js");
    myFixture.completeBasic();
    myFixture.assertPreferredCompletionItems(0, variants);
  }

  public void doTest(@NotNull final String fileName) {
    myFixture.configureByFiles(fileName, "controller.js", "angular.js");
    myFixture.checkHighlighting();
  }
}
