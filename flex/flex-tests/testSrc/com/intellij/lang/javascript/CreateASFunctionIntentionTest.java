package com.intellij.lang.javascript;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NonNls;

public class CreateASFunctionIntentionTest extends CreateJSFunctionIntentionTestBase {

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  @NonNls
  public String getBasePath() {
    return "/highlighting/intention/createfunction_as";
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testAll() throws Exception {
    doTestAll();
  }
}
