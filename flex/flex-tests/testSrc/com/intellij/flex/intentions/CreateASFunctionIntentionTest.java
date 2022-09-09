// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.CreateJSFunctionIntentionTestBase;
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

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateFunctionAS() {
    doCompositeNameBeforeAfterTest("js2", false);
  }
  
  public void testCreateFunctionAS_2() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateFunctionAS_2Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateFunctionAS_2_2() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_3() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateFunctionAS_3Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_4() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_4Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_5() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateFunctionAS_5Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_6() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateFunctionAS_6Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_7() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_7_2() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_8() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateFunctionAS_8Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateFunctionAS_9() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_10() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_11() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_12() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_13() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_14() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_15() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_16() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_17() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_18() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_19() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_19_2() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_20() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_21() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_22() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateFunctionAS_Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateConstructor() {
    doTestTwoFiles();
  }

  private void doTestTwoFiles() {
    myFixture.enableInspections(new JSValidateTypesInspection(), new JSCheckFunctionSignaturesInspection());
    String name = getTestName(false);
    String first = name + '/' + name + ".as";
    String second = name + '/' + name + "_2.as";
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