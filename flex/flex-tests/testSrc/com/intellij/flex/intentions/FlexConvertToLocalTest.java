package com.intellij.flex.intentions;

import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.BaseJSIntentionTestCase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.inspections.actionscript.JSFieldCanBeLocalInspection;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NotNull;

public class FlexConvertToLocalTest extends BaseJSIntentionTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), myFixture.getTestRootDisposable());
    myFixture.enableInspections(new JSFieldCanBeLocalInspection());
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @NotNull
  @Override
  public String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("") + "/convertToLocal";
  }
  
  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testAll() {
    doTestAll();
  }
}

