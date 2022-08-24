// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.lang.javascript.nodejs.library.yarn.AbstractYarnPnpIntegrationTest;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

import static com.intellij.lang.javascript.linter.tslint.TsLintTestUtil.BASE_TEST_DATA_PATH;

public class TsLintHighlightingTest extends LinterHighlightingTest {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(BASE_TEST_DATA_PATH + "/highlighting/");
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

  @Override
  protected @NotNull Map<String, String> getGlobalPackageVersionsToInstall() {
    return Map.of("tslint", "latest",
                  "typescript", "latest");
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
    doEditorHighlightingTest("data.ts", () -> JSTestUtils.ensureLineSeparators(myFixture.getFile(), LineSeparator.CRLF));
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

  public void testYarnPnpTsLintExample() throws Exception {
    doEditorHighlightingTest("app.ts", () -> {
      VirtualFile root = Objects.requireNonNull(myFixture.findFileInTempDir("."));
      NodePackage yarnPkg = AbstractYarnPnpIntegrationTest.installYarnGlobally(getNodeJsAppRule());
      AbstractYarnPnpIntegrationTest.configureYarnBerryAndRunYarnInstall(getProject(), yarnPkg, getNodeJsAppRule(), root);
      YarnPnpNodePackage tslintPkg = YarnPnpNodePackage.create(getProject(),
                                                               PackageJsonUtil.findChildPackageJsonFile(root),
                                                               getPackageName(), false, false);
      assertNotNull(tslintPkg);
      configureLinterForPackage(NodePackageRef.create(tslintPkg));
    });
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
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rule.for.line.description", "quotemark"));
  }

  public void testSuppressRuleForLineAddsToExistingComment() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rule.for.line.description", "quotemark"));
  }

  public void testSuppressRuleForFileAddsToExistingComment() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rule.for.file.description", "quotemark"));
  }

  public void testSuppressAllRulesForLine() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.all.rules.for.line.description", "TSLint"));
  }

  public void testSuppressAllRulesForLineOverwritesExistingSuppressionForRule() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.all.rules.for.line.description", "TSLint"));
  }

  private void doFixTest(String mainFileName, String intentionDescription) {
    doFixTestForDirectory(mainFileName, ".ts", intentionDescription);
  }
}
