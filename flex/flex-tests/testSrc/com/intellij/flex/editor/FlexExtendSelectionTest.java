package com.intellij.flex.editor;


import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSBaseEditorTestCase;

public class FlexExtendSelectionTest extends JSBaseEditorTestCase {
  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "as_editor/");
    super.setUp();
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_editor/");
  }

  public void testSyntaxSelection3() {
    String testName = getTestName(false);
    myFixture.configureByFile(testName + ".js2");
    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after.js2");

    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after1.5.js2");

    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after2.js2");

    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after3.js2");

    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after4.js2");

    myFixture.configureByFile(testName + "_2.js2");
    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_2_after.js2");

    myFixture.configureByFile(testName + "_3.js2");
    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_3_after.js2");
  }

  public void testSyntaxSelection5() {
    String testName = getTestName(false);
    myFixture.configureByFile(testName + ".js2");
    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after.js2");

    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after1.5.js2");

    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after2.js2");

    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after3.js2");

    myFixture.configureByFile(testName + "_2.js2");
    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_2_after.js2");
  }

  public void testSyntaxSelection6() {
    String testName = getTestName(false);
    myFixture.configureByFile(testName + ".js2");
    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after.js2");
  }

  public void testSyntaxSelection6_2() {
    String testName = getTestName(false);
    myFixture.configureByFile(testName + ".js2");
    performSyntaxSelectionAction();
    myFixture.checkResultByFile(testName + "_after.js2");
  }
}
