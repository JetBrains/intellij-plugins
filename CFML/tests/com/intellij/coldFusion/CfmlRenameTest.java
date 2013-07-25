package com.intellij.coldFusion;

import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * @author vnikolaenko
 */
public class CfmlRenameTest extends CfmlCodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
      return "/rename";
  }

  private void doTest(final String newName) throws Throwable {
    myFixture.testRename(Util.getInputDataFileName(getTestName(true)),
                         Util.getExpectedDataFileName(getTestName(true)), newName);
  }

  public void testSimpleVariableRename() throws Throwable { doTest("newName"); }
  public void testComponentVariableRename() throws Throwable { doTest("newName"); }
  public void testScriptFunctionRename() throws Throwable { doTest("newName"); }
  public void testTagFunctionRename() throws Throwable { doTest("newName"); }
  public void testLeaveScopeUnchanged() throws Throwable { doTest("newName"); }
  public void testLeaveScopeUnchanged2() throws Throwable { doTest("newName"); }
}
