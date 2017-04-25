package com.intellij.flex.imports;

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.flex.util.FlexModuleFixtureBuilder;
import com.intellij.flex.util.FlexModuleFixtureBuilderImpl;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.inspections.JSUnresolvedVariableInspection;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.util.containers.ContainerUtil;

import java.util.List;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

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
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));

    IdeaTestFixtureFactory.getFixtureFactory().registerFixtureBuilder(FlexModuleFixtureBuilder.class, FlexModuleFixtureBuilderImpl.class);
    super.setUp();
    JSTestUtils.initJSIndexes(getProject());
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
    myFixture.enableInspections(JSUnresolvedVariableInspection.class);
    myFixture.setTestDataPath(FlexTestUtils.getTestDataPath("imports/auto"));
  }

  public void testVarStatement() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInPackage() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInPackageWithComments() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInClass() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInClassWithComments() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInPackageInClass() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testVarStatementInPackageInClassWithComments() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testOtherImportsPresent() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testOtherImportsPresentNoPackage() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testOtherImportsPresentNoClass() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testOtherImportsOnIncorrectPlace() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(AS_FILE_EXTENSION, "com.test.Foo");
  }

  public void testCdataSeveralSections() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(MXML_FILE_EXTENSION, "com.test.Foo");
  }

  public void testCdataOtherImportsPresent() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(MXML_FILE_EXTENSION, "com.test.Foo");
  }

  public void testCdataOtherImportsOnIncorrectPlace() throws Throwable {
    JSTestUtils.addClassesToProject(myFixture, true, "com.test.Foo");
    launchImportIntention(MXML_FILE_EXTENSION, "com.test.Foo");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testAnonymousHandler1() throws Throwable {
    launchImportIntention(MXML_FILE_EXTENSION, "mx.utils.Base64Decoder");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testBeforePackage() throws Throwable {
    launchImportIntention(AS_FILE_EXTENSION, "mx.utils.Base64Decoder");
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testWrapImportStatement() throws Throwable {
    CodeStyleSettings currentSettings = CodeStyleSettingsManager.getSettings(myFixture.getProject());
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

  private void launchImportIntention(final String fileExtension, String className) throws Throwable {
    String hint = className + "?";
    List<IntentionAction> list = getIntentions(getThisTestInputFileName(fileExtension), hint);
    myFixture.launchAction(assertOneElement(list));
    myFixture.checkResultByFile(getThisTestExpectedResultFileName(fileExtension));
  }


  private List<IntentionAction> getIntentions(final String fileName, final String hint) throws Throwable {

    List<IntentionAction> list = ContainerUtil.findAll(myFixture.getAvailableIntentions(fileName),
                                                       intentionAction -> intentionAction.getText().startsWith(hint));

    final AccessToken l = ReadAction.start();
    try {
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
    }
    finally {
      l.finish();
    }

    return list;
  }
}
