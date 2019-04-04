package com.intellij.flex.intentions;

import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSIntentionTestCase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

public class CreateASVariableIntentionTest extends BaseJSIntentionTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), myFixture.getTestRootDisposable());
    myFixture.enableInspections(new JSUnresolvedVariableInspection());
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


  public void testCreateVariableAS_() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_2() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }


  public void testCreateVariableAS_2Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_2_2() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_3() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_3Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_3_2() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testCreateVariableAS_3_3() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_4() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testCreateVariableAS_4Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_5() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_5Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_6() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_7() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_8() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_9() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_10() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_10_2() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_11() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_12() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_13() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_14() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_14_2() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_15() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_16() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_17() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_18() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_19() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_20() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_21() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_21Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS_22() throws Exception {
    doCompositeNameBeforeAfterTest("js2", false);
  }

  public void testCreateVariableAS_Mxml() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS__2() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }

  public void testCreateVariableAS__3() throws Exception {
    doCompositeNameBeforeAfterTest("mxml", false);
  }
}
