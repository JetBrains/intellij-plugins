package com.intellij.flex.generate;


import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import org.jetbrains.annotations.NonNls;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class ActionScriptGenerateTest extends JSDaemonAnalyzerTestCase {
  @NonNls
  static final String BASE_PATH = "/js2_highlighting";

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected String getExtension() {
    return "js2";
  }


  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testGenerateGetter() throws Exception {
    doGenerateTest("Generate.GetAccessor.JavaScript");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testGenerateSetter() throws Exception {
    doGenerateTest("Generate.SetAccessor.JavaScript");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testGenerateGetterAndSetter() throws Exception {
    doGenerateTest("Generate.GetSetAccessor.JavaScript");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testGenerateConstructor() throws Exception {
    doGenerateTest("Generate.Constructor.JavaScript");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testGenerateConstructor2() throws Exception {
    doGenerateTest("Generate.Constructor.JavaScript");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testGenerateConstructor3() throws Exception {
    doGenerateTest("Generate.Constructor.JavaScript");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testGenerateToString() throws Exception {
    doGenerateTest("Generate.ToString.Actionscript", "", "js2");
    doGenerateTest("Generate.ToString.Actionscript", "_2", "js2");
    doGenerateTest("Generate.ToString.Actionscript", "_3", "as");
  }

  @JSTestOptions({JSTestOption.WithJsSupportLoader})
  public void testGenerateToString2() throws Exception {
    doGenerateTest("Generate.ToString.Actionscript", "", "js2");
    doGenerateTest("Generate.ToString.Actionscript", "_2", "js2");
  }

  private void doGenerateTest(@NonNls final String actionId) throws Exception {
    doGenerateTest(actionId, "js2");
  }
}