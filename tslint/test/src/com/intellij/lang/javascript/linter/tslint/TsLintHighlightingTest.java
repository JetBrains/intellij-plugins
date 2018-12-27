package com.intellij.lang.javascript.linter.tslint;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintExternalAnnotator;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLinterInput;
import com.intellij.lang.javascript.service.JSLanguageServiceExecutorImpl;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
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
    doTest("one", "one/one.ts", null);
  }

  public void testWithYamlConfig() {
    doTest("withYamlConfig", "withYamlConfig/main.ts", null);
  }

  public void testWithWarningSeverity() {
    doTest("withWarningSeverity", "withWarningSeverity/main.ts", null);
  }

  public void testNoAdditionalDirectory() {
    doTest("noAdditionalDirectory", "noAdditionalDirectory/data.ts", null);
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation("Could not find custom rule directory:", false, true);
  }

  public void testNoConfig() {
    final PsiFile[] files = myFixture.configureByFiles("noConfig/data.ts");
    final String configFilePath = FileUtil.toSystemDependentName(files[0].getVirtualFile().getParent().getPath() + "/tslint.json");

    doOnlyGlobalAnnotationTest("Config file was not found.", files[0], configFilePath);
  }

  public void testBadConfig() {
    final PsiFile[] files = myFixture.configureByFiles("badConfig/data.ts", "badConfig/tslint.json");
    final String configFilePath = FileUtil.toSystemDependentName(files[1].getVirtualFile().getPath());

    doOnlyGlobalAnnotationTest("Config file was not found.", files[0], configFilePath);
  }

  private void doOnlyGlobalAnnotationTest(@SuppressWarnings("SameParameterValue") final String expected,
                                          final PsiFile dataFile, final String configPath) {
    final TsLintConfiguration configuration = TsLintConfiguration.getInstance(getProject());
    final TsLintState.Builder builder = new TsLintState.Builder(configuration.getExtendedState().getState());
    builder.setCustomConfigFileUsed(true).setCustomConfigFilePath(configPath);
    final TsLinterInput input = new TsLinterInput(dataFile, builder.build(), null, null);
    final JSLinterAnnotationResult result = new TsLintExternalAnnotator().annotate(input);
    Assert.assertNotNull(result.getFileLevelError());
    Assert.assertEquals(expected, result.getFileLevelError().getMessage());
  }

  public void testLineSeparatorsWin() {
    doTest("lineSeparators", "lineSeparators/data.ts", LineSeparator.CRLF);
  }

  public void testTimeout() {
    JSLanguageServiceUtil.setTimeout(1, getTestRootDisposable());
    myExpectedGlobalAnnotation =
      new ExpectedGlobalAnnotation("TSLint: " + JSLanguageServiceExecutorImpl.LANGUAGE_SERVICE_EXECUTION_TIMEOUT, true, false);
    doTest("clean", "clean/clean.ts", null);
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
    String testDir = getTestName(true);
    doTest(testDir, testDir + "/" + mainFileName + ".ts", null);
    IntentionAction intention = myFixture.getAvailableIntention(intentionDescription);
    assertNotNull(String.format("Expected intention with description %s to be available", intentionDescription), intention);
    myFixture.launchAction(intention);
    myFixture.checkResultByFile(testDir + "/" + mainFileName + "_after.ts");
  }

  private void doTest(@NotNull String directoryToCopy, @NotNull String filePathToTest,
                      LineSeparator lineSeparator) {
    myFixture.copyDirectoryToProject(directoryToCopy, "");
    PsiFile fileToHighlight = myFixture.configureByFile(filePathToTest);
    if (lineSeparator != null) {
      ensureLineSeparators(fileToHighlight.getVirtualFile(), lineSeparator.getSeparatorString());
    }

    VirtualFile configVirtualFile = TslintUtil.lookupConfig(fileToHighlight.getVirtualFile());
    Assert.assertNotNull("Could not find config file", configVirtualFile);
    final TsLintConfiguration configuration = TsLintConfiguration.getInstance(getProject());
    final TsLintState.Builder builder = new TsLintState.Builder(configuration.getExtendedState().getState())
      .setCustomConfigFileUsed(true)
      .setCustomConfigFilePath(configVirtualFile.getPath());
    configuration.setExtendedState(true, builder.build());

    myFixture.testHighlighting(true, false, true);
  }
}
