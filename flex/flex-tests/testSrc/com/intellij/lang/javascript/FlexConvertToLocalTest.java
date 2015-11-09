package com.intellij.lang.javascript;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.inspections.actionscript.JSFieldCanBeLocalInspection;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NonNls;

public class FlexConvertToLocalTest extends BaseJSIntentionTestCase {

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected LocalInspectionTool[] configureLocalInspectionTools() {
    return new LocalInspectionTool[]{new JSFieldCanBeLocalInspection()};
  }

  @Override
  @NonNls
  public String getBasePath() {
    return "/convertToLocal";
  }

  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testAll() throws Exception {
    doTestAll();
  }
}

