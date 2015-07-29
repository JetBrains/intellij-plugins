package com.intellij.lang.javascript;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NonNls;

public class CreateASVariableIntentionTest extends BaseJSIntentionTestCase {
  @Override
  protected LocalInspectionTool[] configureLocalInspectionTools() {
    return new LocalInspectionTool[]{new JSUnresolvedVariableInspection()};
  }

  @Override
  @NonNls
  public String getBasePath() {
    return "/highlighting/intention/createvariable_as";
  }

  @Override
  public String getTestDataPath() {
    return JSTestUtils.getTestDataPath();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testAll() throws Exception {
    doTestAll();
  }
}
