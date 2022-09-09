package com.intellij.flex.generate;


import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.editor.FlexProjectDescriptor;
import com.intellij.flex.util.FlexTestUtils;
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

  public void testGenerateGetter() {
    doGenerateTest("Generate.GetAccessor.JavaScript");
  }

  public void testGenerateSetter() {
    doGenerateTest("Generate.SetAccessor.JavaScript");
  }

  @FlexTestOptions(FlexTestOption.WithFlexFacet)
  public void testGenerateGetterAndSetter() {
    FlexTestUtils.setupFlexSdk(getModule(), getTestName(false), getClass(), myFixture.getTestRootDisposable());
    doGenerateTest("Generate.GetSetAccessor.JavaScript");
  }

  public void testGenerateConstructor() {
    doGenerateTest("Generate.Constructor.JavaScript");
  }

  public void testGenerateConstructor2() {
    doGenerateTest("Generate.Constructor.JavaScript");
  }

  public void testGenerateConstructor3() {
    doGenerateTest("Generate.Constructor.JavaScript");
  }

  public void testGenerateToString() {
    doGenerateTest("Generate.ToString.Actionscript", "", "js2");
    doGenerateTest("Generate.ToString.Actionscript", "_2", "js2");
    doGenerateTest("Generate.ToString.Actionscript", "_3", "as");
  }

  public void testGenerateToString2() {
    doGenerateTest("Generate.ToString.Actionscript", "", "js2");
    doGenerateTest("Generate.ToString.Actionscript", "_2", "js2");
  }

  private void doGenerateTest(@NonNls final String actionId) {
    doGenerateTest(actionId, "js2");
  }
}