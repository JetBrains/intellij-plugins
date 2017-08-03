package com.intellij.flex.refactoring;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.refactoring.JSInlineVarOrFunctionTestBase;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexInlineVariableTest extends JSInlineVarOrFunctionTestBase {
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("as_refactoring/inlineVariable/");
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  private void ecmaTest() throws Exception {
    doTest(getTestName(false), "js2");
  }

  public void testClassMember() throws Exception {
    ecmaTest();
  }

  public void testClassMember2() throws Exception {
    ecmaTest();
  }

  public void testClassMember3() throws Exception {
    ecmaTest();
  }

  public void testInMxml() throws Exception {
    doTest(getTestName(false), "mxml");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk, JSTestOption.WithJsSupportLoader})
  public void testIntoAttribute() throws Exception {
    doTest(getTestName(false), "mxml");
  }

  public void testInlineOneFieldUsage() throws Exception {
    doTest(new String[]{getTestName(false) + ".js2"}, true);
  }

  public void testReportAccessibilityProblems() throws Exception {
    doTestConflicts(getTestName(false), "js2", new String[]{
      "Method Test.foo() with private visibility won't be accessible from file ReportAccessibilityProblems.js2"});
  }
}
