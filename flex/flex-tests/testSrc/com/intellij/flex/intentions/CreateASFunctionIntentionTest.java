package com.intellij.flex.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.CreateJSFunctionIntentionTestBase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSCheckFunctionSignaturesInspection;
import com.intellij.lang.javascript.inspections.JSValidateTypesInspection;
import com.intellij.psi.formatter.xml.XmlCodeStyleSettings;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

public class CreateASFunctionIntentionTest extends CreateJSFunctionIntentionTestBase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @NotNull
  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("") + "/createfunction_as";
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testCreateFunctionAS() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }
  
  public void testCreateFunctionAS_2() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testCreateFunctionAS_2Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testCreateFunctionAS_2_2() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_3() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testCreateFunctionAS_3Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_4() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_4Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_5() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testCreateFunctionAS_5Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_6() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testCreateFunctionAS_6Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_7() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_7_2() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_8() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testCreateFunctionAS_8Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_9() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_10() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_11() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_12() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_13() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_14() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_15() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_16() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_17() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_18() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_19() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_19_2() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_20() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_21() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_22() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateConstructor() {
    doTestTwoFiles();
  }

  private void doTestTwoFiles() {
    myFixture.enableInspections(new JSValidateTypesInspection(), new JSCheckFunctionSignaturesInspection());
    String name = getTestName(false);
    String directory = "/" + name;
    String first = directory + "/" + name + ".as";
    String secondName = name + "_2.as";
    String second = directory + "/" + secondName;
    doTestForAndCheckLastFile(first, second);
  }

  @Override
  protected void performIntention(String testName, IntentionAction action) {
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), settings -> {
      XmlCodeStyleSettings xmlSettings = settings.getCustomSettings(XmlCodeStyleSettings.class);
      if (testName.endsWith("8.mxml")) {
        xmlSettings.XML_WHITE_SPACE_AROUND_CDATA = XmlCodeStyleSettings.WS_AROUND_CDATA_NEW_LINES;
      }
      super.performIntention(testName, action);
    });
  }
}
