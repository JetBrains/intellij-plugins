package org.angularjs.codeInsight.messageFormat;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

/**
 * @author Irina.Chernushina on 12/3/2015.
 */
public class AngularMessageFormatAnnotatorTest extends LightPlatformCodeInsightFixtureTestCase {
  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(AngularMessageFormatAnnotatorTest.class);
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }

  public void testCase1() throws Exception {
    doTest("case1.html");
  }

  public void testCase2() throws Exception {
    doTest("case2.html");
  }

  public void testCase3() throws Exception {
    doTest("case3.html");
  }

  public void testCase4() throws Exception {
    doTest("case4.html");
  }

  @Test
  public void doTest(@NotNull final String fileName) throws Exception{
    try {
      System.setProperty("angular.js.parse.message.format", "true");
      myFixture.configureByFiles(fileName, "controller.js", "angular.js");
      myFixture.checkHighlighting();
    } finally {
      System.clearProperty("angular.js.parse.message.format");
    }
  }
}
