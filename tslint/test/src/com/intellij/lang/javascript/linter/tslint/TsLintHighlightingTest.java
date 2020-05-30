package com.intellij.lang.javascript.linter.tslint;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public class TsLintHighlightingTest extends LinterHighlightingTest {
  @Override
  protected String getBasePath() {
    return TsLintTestUtil.getTestDataRelativePath() + "/highlighting/";
  }

  @NotNull
  @Override
  protected InspectionProfileEntry getInspection() {
    return new TsLintInspection();
  }

  @NotNull
  @Override
  protected String getPackageName() {
    return TslintUtil.PACKAGE_NAME;
  }

  public void testOne() {
    doEditorHighlightingTest("one.ts");
  }

  public void testWithCustomConfigFile() {
    doEditorHighlightingTest("ts.ts", () -> {
      TsLintConfiguration configuration = TsLintConfiguration.getInstance(myFixture.getProject());
      TsLintState newState = configuration.getExtendedState().getState().builder()
        .setCustomConfigFileUsed(true)
        .setCustomConfigFilePath(myFixture.getTempDirPath() + "/tslint-base.json")
        .build();
      configuration.setExtendedState(true, newState);
    });
  }

  public void testWithYamlConfig() {
    doEditorHighlightingTest("main.ts");
  }

  public void testWithWarningSeverity() {
    doEditorHighlightingTest("main.ts");
  }

  public void testNoAdditionalDirectory() {
    doEditorHighlightingTest("data.ts");
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation("Could not find custom rule directory:", false, true);
  }

  public void testNoConfig() {
    doEditorHighlightingTest("data.ts");
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation("Config file was not found.", false, true);
  }

  public void testNoConfigWithCustomConfigFileSet() {
    doEditorHighlightingTest("data.ts", () -> {
      TsLintConfiguration configuration = TsLintConfiguration.getInstance(myFixture.getProject());
      TsLintState newState = configuration.getExtendedState()
        .getState()
        .builder()
        .setCustomConfigFilePath(myFixture.getTempDirPath() + "/tslint-nonexistent.json")
        .build();
      configuration.setExtendedState(configuration.isEnabled(), newState);
    });
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation("Config file was not found.", false, true);
  }

  public void testSuppressMissingConfigFileWithAutodetectPackage() {
    configureLinterForPackage(AutodetectLinterPackage.INSTANCE);
    AutodetectLinterPackage.setTestAutodetectedPackage(myFixture.getProject(), getNodePackage(), myFixture.getTestRootDisposable());
    doEditorHighlightingTest("data.ts");
  }

  public void testLineSeparatorsWin() {
    doEditorHighlightingTest("data.ts",() -> JSTestUtils.ensureLineSeparators(myFixture.getFile(), LineSeparator.CRLF));
  }

  public void testTimeout() {
    JSLanguageServiceUtil.setTimeout(1, getTestRootDisposable());
    String expectedMessage = "TSLint: " + JSLanguageServiceUtil.getTimeoutMessage("ts.ts");
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation(expectedMessage, true, false);
    doEditorHighlightingTest("ts.ts");
  }

  public void testHighlightJsFiles() {
    doEditorHighlightingTest("test.js", () -> {
      TsLintConfiguration configuration = TsLintConfiguration.getInstance(myFixture.getProject());
      TsLintState newState = configuration.getExtendedState()
        .getState()
        .builder()
        .setAllowJs(true)
        .build();
      configuration.setExtendedState(configuration.isEnabled(), newState);
    });
  }

  public void testNoJsFilesByDefault() {
    doEditorHighlightingTest("test.js");
  }

  public void testFixFile() {
    doFixTest("fix", "TSLint: Fix current file");
  }

  public void testFixSingleError() {
    doFixTest("fix", "TSLint: Fix 'quotemark'");
  }

  public void testFixArrowReturnShorthand() {
    doFixTest("main", "TSLint: Fix 'arrow-return-shorthand'");
  }

  public void testSuppressRuleForLine() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rules.for.line.description", "'quotemark'"));
  }

  public void testSuppressRuleForLineAddsToExistingComment() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rules.for.line.description", "'quotemark'"));
  }

  public void testSuppressRuleForFileAddsToExistingComment() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rules.for.file.description", "'quotemark'"));
  }

  public void testSuppressAllRulesForLine() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rules.for.line.description", "all TSLint rules"));
  }

  public void testSuppressAllRulesForLineOverwritesExistingSuppressionForRule() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rules.for.line.description", "all TSLint rules"));
  }

  private void doFixTest(String mainFileName, String intentionDescription) {
    doFixTestForDirectory(mainFileName, ".ts", intentionDescription);
  }
}
