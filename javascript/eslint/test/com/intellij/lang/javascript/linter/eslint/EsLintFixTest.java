package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.inspections.JSConsecutiveCommasInArrayLiteralInspection;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.testFramework.utils.ActionsOnSaveTestUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.util.LineSeparator;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EsLintFixTest extends EslintServiceTestBase {
  @Override
  protected @NotNull InspectionProfileEntry getInspection() {
    return new EslintInspection();
  }

  @Override
  protected @NotNull String getPackageName() {
    return "eslint";
  }

  @Override
  protected @NotNull Map<String, String> getGlobalPackageVersionsToInstall() {
    return Map.of("eslint", "8.57.0",
                  "eslint-plugin-html", "8.1.4",
                  "eslint-plugin-vue", "10.9.2",
                  "vue-eslint-parser", "10.4.1",
                  "eslint-plugin-react", "7.37.5",
                  "typescript", "5.9.3",
                  "@typescript-eslint/parser", "8.63.0");
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final String basePath = EslintTestUtil.getEslintTestDataPath() + "/linter/eslint/quickfix/";
    myFixture.setTestDataPath(basePath);
  }

  public void testFixFileWorks() {
    doTestQuickFix("ESLint: Fix current file");
  }

  public void testFixFileInDumbWorks() {
    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, getTestRootDisposable());
    DumbModeTestUtils.runInDumbModeSynchronously(getProject(), () -> {
      doTestQuickFix("ESLint: Fix current file", ".js", ".eslintrc", true);
    });
  }

  public void testFixFileWithWindowsLineSeparator() throws IOException {
    if (!SystemInfo.isWindows) return;
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".js", LineSeparator.CRLF);
    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(getFile().getVirtualFile())));
  }

  public void testFixFileWithConvertLineSeparators() throws IOException {
    if (!SystemInfo.isWindows) return;
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".js", LineSeparator.LF);
    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(getFile().getVirtualFile())));
  }

  public void testFixSingleError() {
    String description = JavaScriptBundle.message("eslint.fix.problems.text.with.error.code",
                                                  EslintBundle.message("settings.javascript.linters.eslint.configurable.name"),
                                                  "no-multiple-empty-lines");
    doQuickFixTestForDirectory(description, "test", ".js", null);
  }

  public void testFixSingleErrorWithWindowsLineSeparators() {
    if (!SystemInfo.isWindows) return;
    String description = JavaScriptBundle.message("eslint.fix.problems.text.with.error.code",
                                                  EslintBundle.message("settings.javascript.linters.eslint.configurable.name"),
                                                  "no-multiple-empty-lines");
    doQuickFixTestForDirectory(description, "test", ".js", LineSeparator.CRLF);
  }

  public void testNoFixFileActionForNonFixableErrors() {
    String dir = getTestName(false);
    configure(dir + "/test.js", dir + "/.eslintrc", true);
    myFixture.checkHighlighting();
    assertEmpty(myFixture.filterAvailableIntentions("ESLint: Fix current file"));
  }

  public void testFixWithSearchForConfig() {
    doTestQuickFix("ESLint: Fix current file", ".js", ".eslintrc", false);
  }

  public void testScriptsInHtmlFile() {
    configure(getTestName(false) + ".html", ".eslintrc-with-html.json", true);

    List<IntentionAction> fixActions =
      ContainerUtil.filter(myFixture.getAvailableIntentions(), action -> "ESLint: Fix current file".equals(action.getText()));
    assertEmpty(fixActions);
  }

  public void testSuppressByLineComment() {
    doTestQuickFix("Suppress 'comma-spacing' for current line");
  }

  public void testSuppressForLineInNestedScopeWithIndent() {
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), settings -> {
      CommonCodeStyleSettings commonSettings = settings.getCommonSettings(JavascriptLanguage.INSTANCE);
      commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = false;
      doTestQuickFix("Suppress 'comma-spacing' for current line");
    });
  }

  public void testSuppressForLineInNestedScopeAtLineStart() {
    JSTestUtils.testWithTempCodeStyleSettings(getProject(), settings -> {
      CommonCodeStyleSettings commonSettings = settings.getCommonSettings(JavascriptLanguage.INSTANCE);
      commonSettings.LINE_COMMENT_AT_FIRST_COLUMN = true;
      doTestQuickFix("Suppress 'comma-spacing' for current line");
    });
  }

  public void testSuppressByFileComment() {
    doTestQuickFix("Suppress 'comma-spacing' for current file");
  }

  public void testAddSuppressionToExistingLineComment() {
    doTestQuickFix("Suppress 'comma-spacing' for current line");
  }

  public void testAddSuppressionToExistingFileComment() {
    doTestQuickFix("Suppress 'comma-spacing' for current file");
  }

  public void testSuppressAllRulesForFile() {
    doTestQuickFix("Suppress all ESLint rules for current file");
  }

  public void testSuppressAllRulesForFileWithExistingComment() {
    doTestQuickFix("Suppress all ESLint rules for current file");
  }

  public void testSuppressForLineInJSXTagContent() {
    doQuickFixTestForDirectory("Suppress 'react/self-closing-comp' for current line", "test", ".jsx", null);
  }

  public void testSuppressForLineInJSXTagContent2() {
    doQuickFixTestForDirectory("Suppress 'jsx-quotes' for current line", "test", ".jsx", null);
  }

  public void testSuppressForLineInJSXTagContentAddsToExistingComment() {
    doQuickFixTestForDirectory("Suppress 'react/no-unknown-property' for current line", "test", ".jsx", null);
  }

  public void testSuppressForLineInJSXTagAttributes() {
    doQuickFixTestForDirectory("Suppress 'react/jsx-curly-brace-presence' for current line", "test", ".jsx", null);
  }

  public void testSuppressForFileInHtml() {
    doTestQuickFix("Suppress 'no-multiple-empty-lines' for current file", ".html", ".eslintrc-with-html.json", true);
  }

  public void testSuppressMultiLinesByLineComment() {
    doTestQuickFix("Suppress 'no-multiple-empty-lines' for current line", ".js", ".eslintrc-with-html.json", true);
  }

  public void testSuppressForLineInHtml() {
    doTestQuickFix("Suppress 'no-multiple-empty-lines' for current line", ".html", ".eslintrc-with-html.json", true);
  }

  public void testFixWorksInJsx() {
    doTestQuickFix("ESLint: Fix current file", ".jsx", ".eslintrc.json", true);
  }

  public void testInHtmlWithHtmlPluginExplicitName() {
    doTestQuickFix("ESLint: Fix current file", ".html", ".eslintrc-with-html-explicit-name.json", true);
  }

  public void testFixInHtml() {
    doTestQuickFix("ESLint: Fix current file", ".html", ".eslintrc-with-html.json", true);
  }

  public void testFixFileInVue() {
    assertNotNull("This test must be run with intellij.vuejs module in classpath", Language.findLanguageByID("Vue"));
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".vue", null);
  }

  public void testFixFileInVueTs() {
    assertNotNull("This test must be run with intellij.vuejs module in classpath", Language.findLanguageByID("Vue"));
    doQuickFixTestForDirectory("ESLint: Fix current file", "test", ".vue", null);
  }

  public void testRunEslintFixOnSave() {
    EslintConfiguration configuration = EslintConfiguration.getInstance(getProject());
    EslintState eslintState = configuration.getExtendedState().getState();
    boolean origEnabled = configuration.isEnabled();
    boolean origRunOnSave = eslintState.isRunOnSave();
    configuration.setExtendedState(true, new EslintState.Builder(eslintState).setRunOnSave(true).build());
    try {
      myFixture.addFileToProject("eslint.config.js", "module.exports = {rules: {\"semi\": \"error\"}}");
      myFixture.configureByText("foo.js", "var a = ''");
      myFixture.type(' ');
      myFixture.performEditorAction("SaveAll");
      ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(myFixture.getProject());

      myFixture.checkResult(" var a = '';");
    }
    finally {
      configuration.setExtendedState(origEnabled, new EslintState.Builder(eslintState).setRunOnSave(origRunOnSave).build());
    }
  }

  public void testSuppressQuickFixGoesAfterInspectionFix() {
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection.class);
    doEditorHighlightingTest("test.js", null);

    List<IntentionAction> quickFixes = myFixture.getAvailableIntentions();
    List<String> quickFixNames = ContainerUtil.map(quickFixes, IntentionAction::getText);
    assertContainsOrdered(quickFixNames,
                        "Remove unused variable 'foo'",
                        "Edit inspection profile setting",
                        "Run inspection on…",
                        "Disable highlighting, keep fix",
                        "Disable inspection",
                        "Suppress for file",
                        "Suppress for statement",
                        "Suppress 'no-unused-vars' for current line",
                        "Edit inspection profile setting",
                        "Run inspection on…",
                        "Disable highlighting, keep fix",
                        "Disable inspection",
                        "Suppress 'no-unused-vars' for current file",
                        "Suppress all ESLint rules for current file",
                        "Suppress 'no-unused-vars' for current line",
                        "Suppress all ESLint rules for current line");
  }

  public void testEsLintQuickFixGoesAfterInspectionFix() {
    myFixture.enableInspections(JSConsecutiveCommasInArrayLiteralInspection.class);
    doEditorHighlightingTest("test.js", null);

    List<IntentionAction> quickFixes = myFixture.getAvailableIntentions();
    List<String> quickFixNames = ContainerUtil.map(quickFixes, IntentionAction::getText);
    assertContainsOrdered(quickFixNames,
                        "Insert 'undefined'",
                        "Edit inspection profile setting",
                        "Fix all 'Consecutive commas in array literal' problems in file",
                        "Run inspection on…",
                        "Disable highlighting, keep fix",
                        "Disable inspection",
                        "Suppress for file",
                        "Suppress for statement",
                        "Remove unneeded comma",
                        "Edit inspection profile setting",
                        "Fix all 'Consecutive commas in array literal' problems in file",
                        "Run inspection on…",
                        "Disable highlighting, keep fix",
                        "Disable inspection",
                        "Suppress for file",
                        "Suppress for statement",
                        "ESLint: Fix 'comma-spacing'",
                        "Edit inspection profile setting",
                        "Run inspection on…",
                        "Disable highlighting, keep fix",
                        "Disable inspection",
                        "Suppress 'comma-spacing' for current file",
                        "Suppress all ESLint rules for current file",
                        "Suppress 'comma-spacing' for current line",
                        "Suppress all ESLint rules for current line",
                        "ESLint: Fix current file",
                        "Edit inspection profile setting",
                        "Run inspection on…",
                        "Disable highlighting, keep fix",
                        "Disable inspection",
                        "Suppress 'comma-spacing' for current file",
                        "Suppress all ESLint rules for current file",
                        "Suppress 'comma-spacing' for current line",
                        "Suppress all ESLint rules for current line",
                        "Flip ',' (may change semantics)",
                        "Edit intention settings",
                        "Disable 'Flip comma'",
                        "Assign shortcut…",
                        "Split into declaration and initialization",
                        "Edit intention settings",
                        "Disable 'Split declaration and initialization'",
                        "Assign shortcut…",
                        "Disable option 'Variables and fields' for 'Type annotations' inlay hints",
                        "Put comma-separated elements on multiple lines",
                        "Edit intention settings",
                        "Disable 'Put elements on multiple lines'",
                        "Assign shortcut…");
  }

  private void doQuickFixTestForDirectory(String description, String fileToHighlightName,
                                          String extension,
                                          @Nullable LineSeparator lineSeparator) {
    doFixTestForDirectory(fileToHighlightName, extension, description, () -> {
      if (lineSeparator != null) {
        JSTestUtils.ensureLineSeparators(myFixture.getFile(), lineSeparator);
      }
    });
  }

  /**
   * Consider using {@link #doQuickFixTestForDirectory} instead.
   * This reuses the same ESLint config file for multiple tests, which may lead to unexpected results if it is modified
   */
  private void doTestQuickFix(String description) {
    doTestQuickFix(description, ".js", ".eslintrc", true);
  }

  /**
   * Consider using {@link #doQuickFixTestForDirectory} instead.
   * This reuses the same ESLint config file for multiple tests, which may lead to unexpected results if it is modified
   */
  private void doTestQuickFix(String description, String extension, String config, boolean customConfig) {
    configure(getTestName(false) + extension, config, customConfig);
    myFixture.launchAction(myFixture.getAvailableIntention(description));
    myFixture.checkResultByFile(getTestName(false) + "_after" + extension);
  }

  private void configure(String fileToHighlightPath, String configPath, boolean customConfig) {
    PsiFile configPsiFile = myFixture.configureByFile(configPath);

    final EslintConfiguration configuration = EslintConfiguration.getInstance(getProject());
    final EslintState.Builder builder =
      new EslintState.Builder(configuration.getExtendedState().getState()).setCustomConfigFileUsed(customConfig);
    if (customConfig) {
      builder.setCustomConfigFilePath(VfsUtilCore.virtualToIoFile(configPsiFile.getVirtualFile()).getAbsolutePath());
    }

    configuration.setExtendedState(true, builder.build());
    myFixture.configureByFile(fileToHighlightPath);
  }
}
