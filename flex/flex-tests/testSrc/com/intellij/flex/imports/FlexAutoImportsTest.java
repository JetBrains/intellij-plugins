package com.intellij.flex.imports;

import com.intellij.application.options.CodeStyle;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.flex.FlexTestOption;
import com.intellij.flex.FlexTestOptions;
import com.intellij.flex.util.FlexModuleFixtureBuilder;
import com.intellij.flex.util.FlexModuleFixtureBuilderImpl;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.util.containers.ContainerUtil;

import java.util.List;

public class FlexAutoImportsTest extends CodeInsightFixtureTestCase<FlexModuleFixtureBuilder> {
  private static final String EXPECTED_RESULT_FILE_SUFFIX = "expected";
  private static final String INPUT_DATA_FILE_SUFFIX = "input";
  private static final String MXML_FILE_EXTENSION = "mxml";
  private static final String AS_FILE_EXTENSION = "as";

  @Override
  protected Class<FlexModuleFixtureBuilder> getModuleBuilderClass() {
    return FlexModuleFixtureBuilder.class;
  }

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");

    IdeaTestFixtureFactory.getFixtureFactory().registerFixtureBuilder(FlexModuleFixtureBuilder.class, FlexModuleFixtureBuilderImpl.class);
    super.setUp();
    JSTestUtils.initJSIndexes(getProject());
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), myFixture.getProjectDisposable());
    myFixture.enableInspections(JSUnresolvedVariableInspection.class);
    myFixture.setTestDataPath(FlexTestUtils.getTestDataPath("imports/auto"));
  }

  public void testVarStatement() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInPackage() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInPackageWithComments() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInClass() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInClassWithComments() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInPackageInClass() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInPackageInClassWithComments() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testOtherImportsPresent() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testOtherImportsPresentNoPackage() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testOtherImportsPresentNoClass() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testOtherImportsOnIncorrectPlace() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testCdataSeveralSections() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(MXML_FILE_EXTENSION, "com.test.Foo");
  }

  public void testCdataOtherImportsPresent() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(MXML_FILE_EXTENSION, "com.test.Foo");
  }

  public void testCdataOtherImportsOnIncorrectPlace() {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(MXML_FILE_EXTENSION, "com.test.Foo");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testAnonymousHandler1() {
    launchImportIntention(MXML_FILE_EXTENSION, "mx.utils.Base64Decoder");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testBeforePackage() {
    launchImportIntention(AS_FILE_EXTENSION, "mx.utils.Base64Decoder");
  }

  @FlexTestOptions(FlexTestOption.WithFlexSdk)
  public void testWrapImportStatement() {
    CodeStyleSettings currentSettings = CodeStyle.getSettings(myFixture.getProject());
    int m = currentSettings.getDefaultRightMargin();
    currentSettings.setDefaultRightMargin(20);
    try {
      JSTestUtils.addClassesToProject(myFixture, true, "wwwwww.wwwwwwwwwwwww.wwwwwwwww.Wwwwwwwwwwwwwwwwwwww");
      launchImportIntention(AS_FILE_EXTENSION, "wwwwww.wwwwwwwwwwwww.wwwwwwwww.Wwwwwwwwwwwwwwwwwwww");
    }
    finally {
      currentSettings.setDefaultRightMargin(m);
    }
  }

  //@JSTestOptions({JSTestOption.WithFlexSdk})
  //public void testMxmlAttributeRequiresImport() throws Throwable {
  //  JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
  //  launchImportIntention(MXML_FILE_EXTENSION);
  //}

  //public void testMxmlAttributeRequiresImportCdataExists() throws Throwable {
  //  JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
  //  launchImportIntention(MXML_FILE_EXTENSION);
  //}

  //public void testMxmlAttributeRequiresImportNoCdataInMxScript() throws Throwable {
  //  JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
  //  launchImportIntention(MXML_FILE_EXTENSION);
  //}

  static String getInputDataFileName(final String testName, final String fileExtension) {
    return testName + "." + INPUT_DATA_FILE_SUFFIX + "." + fileExtension;
  }

  static String getExpectedResultFileName(final String testName, final String fileExtension) {
    return testName + "." + EXPECTED_RESULT_FILE_SUFFIX + "." + fileExtension;
  }

  private String getThisTestExpectedResultFileName(final String fileExtension) {
    return getExpectedResultFileName(getTestName(true), fileExtension);
  }

  private String getThisTestInputFileName(final String fileExtension) {
    return getInputDataFileName(getTestName(true), fileExtension);
  }

  private void launchImportIntention(final String fileExtension, String className) {
    String hint = className + "?";
    List<IntentionAction> list = getIntentions(getThisTestInputFileName(fileExtension), hint);
    myFixture.launchAction(assertOneElement(list));
    myFixture.checkResultByFile(getThisTestExpectedResultFileName(fileExtension));
  }


  private List<IntentionAction> getIntentions(final String fileName, final String hint) {

    List<IntentionAction> list = ContainerUtil.findAll(myFixture.getAvailableIntentions(fileName),
                                                       intentionAction -> intentionAction.getText().startsWith(hint));

    ApplicationManager.getApplication().runReadAction(() -> {
      Document document = myFixture.getEditor().getDocument();
      int offset = myFixture.getEditor().getCaretModel().getOffset();
      int line = document.getLineNumber(offset);
      boolean hasHint = !DaemonCodeAnalyzerEx.processHighlights(document, getProject(), HighlightSeverity.ERROR,
                                                                document.getLineStartOffset(line),
                                                                document.getLineEndOffset(line), info -> !info.hasHint());
      if (!hasHint) {
        fail(
          "Auto import fix not found: " + DaemonCodeAnalyzerImpl.getHighlights(document, HighlightSeverity.INFORMATION, getProject()));
      }
    });

    return list;
  }
}
