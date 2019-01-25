package com.intellij.lang.javascript.linter.tslint;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.lang.javascript.service.JSLanguageServiceExecutorImpl;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    return "tslint";
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
    doEditorHighlightingTest("data.ts",
                             () -> ensureLineSeparators(myFixture.getFile().getVirtualFile(), LineSeparator.CRLF.getSeparatorString()));
  }

  public void testTimeout() {
    JSLanguageServiceUtil.setTimeout(1, getTestRootDisposable());
    myExpectedGlobalAnnotation =
      new ExpectedGlobalAnnotation("TSLint: " + JSLanguageServiceExecutorImpl.LANGUAGE_SERVICE_EXECUTION_TIMEOUT, true, false);
    doEditorHighlightingTest("ts.ts");
  }

  public void testHighlightJsFiles() {
    doEditorHighlightingTest("test.js");
  }

  public void testSuppressNoConfigFileForJs() {
    //similarly to tslint **/*.*, if there are no jsRules, there shouldn't be an error for .js files
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
    doFixTest("main", JSBundle.message("javascript.linter.suppress.rules.for.line.description", "'quotemark'"));
  }

  public void testSuppressRuleForLineAddsToExistingComment() {
    doFixTest("main", JSBundle.message("javascript.linter.suppress.rules.for.line.description", "'quotemark'"));
  }

  public void testSuppressRuleForFileAddsToExistingComment() {
    doFixTest("main", JSBundle.message("javascript.linter.suppress.rules.for.file.description", "'quotemark'"));
  }

  public void testSuppressAllRulesForLine() {
    doFixTest("main", JSBundle.message("javascript.linter.suppress.rules.for.line.description", "all TSLint rules"));
  }

  public void _testAllRulesAreInConfig() throws Exception {
    myFixture.configureByFile(getTestName(true) + "/tslint.json");
    final Set<String> fromConfig =
      Arrays.stream(myFixture.completeBasic()).map(lookup -> StringUtil.unquoteString(lookup.getLookupString()))
        .collect(Collectors.toSet());

    final Path rulesDir = Paths.get(getNodePackage().getSystemDependentPath()).resolve("lib").resolve("rules");
    Assert.assertTrue(Files.exists(rulesDir));
    final Set<String> fromDir = Files.list(rulesDir).map(path -> path.toFile().getName())
      .filter(name -> name.endsWith("Rule.js"))
      .map(name -> {
        name = name.substring(0, name.length() - 7);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
          final char ch = name.charAt(i);
          if (Character.isUpperCase(ch)) {
            sb.append("-").append(Character.toLowerCase(ch));
          }
          else {
            sb.append(ch);
          }
        }
        return sb.toString();
      })
      .collect(Collectors.toSet());

    final List<String> outdated = fromConfig.stream().filter(name -> !fromDir.contains(name)).sorted().collect(Collectors.toList());
    final List<String> newRules = fromDir.stream().filter(name -> !fromConfig.contains(name)).sorted().collect(Collectors.toList());

    if (!outdated.isEmpty() || !newRules.isEmpty()) {
      Assert.fail(String.format("Outdated: (%d)\n%s\nMissing: (%d)\n%s\n", outdated.size(), outdated, newRules.size(), newRules));
    }
  }

  private void doFixTest(String mainFileName, String intentionDescription) {
    String testDir = getTestName(false);
    doEditorHighlightingTest(mainFileName + ".ts");

    IntentionAction intention = myFixture.getAvailableIntention(intentionDescription);
    assertNotNull(String.format("Expected intention with description %s to be available", intentionDescription), intention);
    myFixture.launchAction(intention);
    myFixture.checkResultByFile(testDir + "/" + mainFileName + "_after.ts");
  }
}
