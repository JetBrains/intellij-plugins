package com.intellij.lang.javascript;

import com.intellij.flex.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NonNls;

public class CreateASFunctionIntentionTest extends CreateJSFunctionIntentionTestBase {

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  @NonNls
  public String getBasePath() {
    return "/createfunction_as";
  }

  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testAll() throws Exception {
    doTestAll();
  }
}
