package com.intellij.flex.intentions;

import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSIntentionTestCase;
import com.intellij.lang.javascript.inspections.JSUnresolvedReferenceInspection;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

public class CreateASVariableIntentionTest extends BaseJSIntentionTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
    myFixture.enableInspections(new JSUnresolvedReferenceInspection());
  }

  @NotNull
  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("") + "/createvariable_as";
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }


  public void testCreateVariableAS_() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_2() {
    doCompositeNameBeforeAfterTest("js2", false);
  }


  public void testCreateVariableAS_2Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_2_2() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_3() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_3Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_3_2() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateVariableAS_3_3() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_4() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testCreateVariableAS_4Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_5() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_5Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_6() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_7() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_8() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_9() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_10() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_10_2() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_11() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_12() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_13() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_14() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_14_2() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_15() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_16() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_17() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_18() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_19() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_20() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_21() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_21Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_22() {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_Mxml() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS__2() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS__3() {
    doCompositeNameBeforeAfterTest("mxml", false);
  }
}
