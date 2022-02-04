package com.intellij.flex.generate;


import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.generate.JSGenerateTestBase;
import com.intellij.testFramework.LightProjectDescriptor;
import org.jetbrains.annotations.NonNls;

public class ActionScriptGenerateTest extends JSGenerateTestBase {
  @NonNls
  static final String BASE_PATH = "/js2_highlighting";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FlexTestUtils.allowFlexVfsRootsFor(myFixture.getTestRootDisposable(), "");
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return FlexProjectDescriptor.DESCRIPTOR;
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath(getBasePath());
  }

  @Override
  protected String getExtension() {
    return "js2";
  }


  @JSTestOptions(JSTestOption.WithJsSupportLoader)
  public void testGenerateGetter() {
    doGenerateTest("Generate.GetAccessor.JavaScript");
  }

  @JSTestOptions(JSTestOption.WithJsSupportLoader)
  public void testGenerateSetter() {
    doGenerateTest("Generate.SetAccessor.JavaScript");
  }

  @JSTestOptions(JSTestOption.WithFlexFacet)
  public void testGenerateGetterAndSetter() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
    doGenerateTest("Generate.GetSetAccessor.JavaScript");
  }

  @JSTestOptions(JSTestOption.WithJsSupportLoader)
  public void testGenerateConstructor() {
    doGenerateTest("Generate.Constructor.JavaScript");
  }

  @JSTestOptions(JSTestOption.WithJsSupportLoader)
  public void testGenerateConstructor2() {
    doGenerateTest("Generate.Constructor.JavaScript");
  }

  @JSTestOptions(JSTestOption.WithJsSupportLoader)
  public void testGenerateConstructor3() {
    doGenerateTest("Generate.Constructor.JavaScript");
  }

  @JSTestOptions(JSTestOption.WithJsSupportLoader)
  public void testGenerateToString() {
    doGenerateTest("Generate.ToString.Actionscript", "", "js2");
    doGenerateTest("Generate.ToString.Actionscript", "_2", "js2");
    doGenerateTest("Generate.ToString.Actionscript", "_3", "as");
  }

  @JSTestOptions(JSTestOption.WithJsSupportLoader)
  public void testGenerateToString2() {
    doGenerateTest("Generate.ToString.Actionscript", "", "js2");
    doGenerateTest("Generate.ToString.Actionscript", "_2", "js2");
  }

  private void doGenerateTest(@NonNls final String actionId) {
    doGenerateTest(actionId, "js2");
  }
}