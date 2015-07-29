package com.intellij.lang.javascript;


import com.intellij.flex.FlexTestUtils;

/**
 * @author Konstantin.Ulitin
 */
public class FlexExtendSelectionTest extends JSBaseEditorTestCase {

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_editor/");
  }

  public void testSyntaxSelection3() throws Exception {
    String testName = getTestName(false);
    configureByFile(testName + ".js2");
    performSyntaxSelectionAction();
    checkResultByFile(testName + "_after.js2");

    performSyntaxSelectionAction();
    checkResultByFile(testName + "_after2.js2");

    performSyntaxSelectionAction();
    checkResultByFile(testName + "_after3.js2");

    configureByFile(testName + "_2.js2");
    performSyntaxSelectionAction();
    checkResultByFile(testName + "_2_after.js2");

    configureByFile(testName + "_3.js2");
    performSyntaxSelectionAction();
    checkResultByFile(testName + "_3_after.js2");
  }

  public void testSyntaxSelection5() throws Exception {
    String testName = getTestName(false);
    configureByFile(testName + ".js2");
    performSyntaxSelectionAction();
    checkResultByFile(testName + "_after.js2");

    performSyntaxSelectionAction();
    checkResultByFile(testName + "_after2.js2");

    performSyntaxSelectionAction();
    checkResultByFile(testName + "_after3.js2");

    configureByFile(testName + "_2.js2");
    performSyntaxSelectionAction();
    checkResultByFile(testName + "_2_after.js2");
  }

  public void testSyntaxSelection6() throws Exception {
    String testName = getTestName(false);
    configureByFile(testName + ".js2");
    performSyntaxSelectionAction();
    checkResultByFile(testName + "_after.js2");
  }

  public void testSyntaxSelection6_2() throws Exception {
    String testName = getTestName(false);
    configureByFile(testName + ".js2");
    performSyntaxSelectionAction();
    checkResultByFile(testName + "_after.js2");
  }
}
