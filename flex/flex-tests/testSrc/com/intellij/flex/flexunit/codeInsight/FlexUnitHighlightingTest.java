package com.intellij.flex.flexunit.codeInsight;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.flex.util.ActionScriptDaemonAnalyzerTestCase;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.util.FlexUnitLibs;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.flexunit.inspections.FlexUnitClassInProductSourceInspection;
import com.intellij.lang.javascript.flex.flexunit.inspections.FlexUnitInspectionToolProvider;
import com.intellij.lang.javascript.inspections.JSMethodCanBeStaticInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class FlexUnitHighlightingTest extends ActionScriptDaemonAnalyzerTestCase implements FlexUnitLibs {

  private static final Collection<String> CHECK_TEST_IN_PRODUCT_SOURCES_FOR =
    Arrays.asList("ClassInProductSource1", "ClassInProductSource2");

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "flexUnit");
    super.setUp();
  }

  @Override
  protected String getBasePath() {
    return "/highlighting/";
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @Override
  protected String getExtension() {
    return "as";
  }

  @Override
  protected LocalInspectionTool[] configureLocalInspectionTools() {
    ArrayList<LocalInspectionTool> tools = new ArrayList<>(Arrays.asList(super.configureLocalInspectionTools()));
    new FlexUnitInspectionToolProvider();
    for (Class aClass : FlexUnitInspectionToolProvider.getInspectionClasses()) {
      try {
        if (aClass != FlexUnitClassInProductSourceInspection.class || CHECK_TEST_IN_PRODUCT_SOURCES_FOR.contains(getTestName(false))) {
          tools.add((LocalInspectionTool)aClass.newInstance());
        }
      }
      catch (InstantiationException | IllegalAccessException e) {
        fail(e.getMessage());
      }
    }
    return tools.toArray(LocalInspectionTool.EMPTY_ARRAY);
  }

  @Override
  protected void doCommitModel(@NotNull ModifiableRootModel rootModel) {
    super.doCommitModel(rootModel);

    FlexTestUtils.addFlexUnitLib(getClass(), getTestName(false), getModule(), getTestDataPath(), FLEX_UNIT_0_9_SWC, FLEX_UNIT_4_SWC);
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("flexUnit");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testMethods1() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Remove static modifier", "as", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNoUnusedHighlighting() throws Exception {
    enableInspectionTool(new JSUnusedGlobalSymbolsInspection() {
    });
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testMethods2() throws Exception {
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testMethods3() throws Exception {
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet})
  public void testMethods1NoFlexUnit() throws Exception {
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testClassInProductSource1() throws Exception {
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testClassInProductSource2() throws Exception {
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testEmptyClass() throws Exception {
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNonPublicClass1() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Make class '" + getTestName(false) + "' public", "as", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNonPublicClass2() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Make class '" + getTestName(false) + "' public", "as", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNonPublicClass3() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Make class '" + getTestName(false) + "' public", "as", getTestName(false) + ".as");
  }


  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNonPublicMethod1() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Make method 'foo' public", "as", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit1})
  public void testNonPublicMethod2() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Make method 'testFoo' public", "as", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNonPublicMethod3() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Make method 'testFoo' public", "as", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNonVoidMethod1() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Make 'foo' return 'void'", "as", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNonVoidMethod2() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Make 'foo' return 'void'", "as", getTestName(false) + ".as");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNonVoidMethod3() throws Exception {
    doHighlightingWithInvokeFixAndCheckResult("Make 'foo' return 'void'", "as", getTestName(false) + ".as");
  }

  private void checkNoFixFor(String methodName) {
    final Collection<HighlightInfo> infoCollection = doTestFor(true, getTestName(false) + ".as");
    IntentionAction action = findIntentionAction(infoCollection, "Make method '" + methodName + "' return 'void'", myEditor, myFile);
    assertNull(action);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNonVoidMethod4() {
    checkNoFixFor("foo");
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testMethodInSuite() {
    configureByFiles(null, getBasePath() + getTestName(false) + ".as", getBasePath() + getTestName(false) + "_2.as");
    doDoTest(true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testMethodInSuite2() throws Exception {
    JSMethodCanBeStaticInspection inspection = new JSMethodCanBeStaticInspection();
    JSTestUtils.setInspectionHighlightLevel(myProject, inspection, HighlightDisplayLevel.WARNING, getTestRootDisposable());
    inspection.myOnlyPrivate = false;
    enableInspectionTool(inspection);
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testMethodInSuite3() throws Exception {
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testMethodsWithCustomRunner() throws Exception {
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testEmptySuite() {
    configureByFiles(null, getBasePath() + getTestName(false) + ".as", getBasePath() + getTestName(false) + "_2.as");
    doDoTest(true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testSuiteWithNoRunner1() {
    configureByFiles(null, getBasePath() + getTestName(false) + ".as", getBasePath() + getTestName(false) + "_2.as");
    doDoTest(true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testSuiteWithNoRunner2() {
    configureByFiles(null, getBasePath() + getTestName(false) + ".as", getBasePath() + getTestName(false) + "_2.as");
    doDoTest(true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testSuiteWithNoRunner3() {
    configureByFiles(null, getBasePath() + getTestName(false) + ".as", getBasePath() + getTestName(false) + "_2.as");
    doDoTest(true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testSuiteWithWrongCustomRunner() {
    configureByFiles(null, getBasePath() + getTestName(false) + ".as", getBasePath() + "WrongCustomRunner.as");
    doDoTest(true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testNonEmptyClass() throws Exception {
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithFlexUnit4})
  public void testMethodInStaticBlock() throws Exception {
    doTest(getBasePath() + getTestName(false) + ".as", true, false, true);
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }
}
