// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint;

import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.LinterHighlightingFixture;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection;
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions;
import com.intellij.lang.javascript.nodejs.library.yarn.YarnPnpIntegrationFixture;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.LineSeparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static com.intellij.lang.javascript.linter.LinterHighlightingFixture.linterHighlightingFixture;
import static com.intellij.lang.javascript.linter.tslint.TsLintTestUtil.BASE_TEST_DATA_PATH;

@TestApplication
public class TsLintHighlightingTest {
  // Pinned to 5.x: typescript@latest is now 7.x (the native rewrite), whose runtime API no longer
  // exposes members such as ts.ScriptTarget, so tslint@6.x crashes ("Cannot read properties of undefined").
  private final TestFixture<LinterHighlightingFixture> tslint = linterHighlightingFixture(
    new JSTempDirFixtureOptions(BASE_TEST_DATA_PATH + "/highlighting/")
      .withMainPackage(TslintUtil.PACKAGE_NAME)
      .withGlobalPackages(Map.of("tslint", "latest",
                                 "typescript", "5.9.3")),
    () -> new TsLintInspection());

  @Test
  public void testOne() {
    tslint.get().doEditorHighlightingTest("one.ts");
  }

  @Test
  public void testWithCustomConfigFile() {
    tslint.get().doEditorHighlightingTest("ts.ts", () -> {
      TsLintConfiguration configuration = TsLintConfiguration.getInstance(tslint.get().getProject());
      TsLintState newState = configuration.getExtendedState().getState().builder()
        .setCustomConfigFileUsed(true)
        .setCustomConfigFilePath(tslint.get().getFixture().getTempDirPath() + "/tslint-base.json")
        .build();
      configuration.setExtendedState(true, newState);
    });
  }

  @Test
  public void testWithYamlConfig() {
    tslint.get().doEditorHighlightingTest("main.ts");
  }

  @Test
  public void testWithWarningSeverity() {
    tslint.get().doEditorHighlightingTest("main.ts");
  }

  @Test
  public void testNoAdditionalDirectory() {
    tslint.get().doEditorHighlightingTest("data.ts");
    tslint.get().setExpectedGlobalAnnotation(new LinterHighlightingFixture.ExpectedGlobalAnnotation("Could not find custom rule directory:", false, true));
  }

  @Test
  public void testNoConfig() {
    tslint.get().doEditorHighlightingTest("data.ts");
    tslint.get().setExpectedGlobalAnnotation(new LinterHighlightingFixture.ExpectedGlobalAnnotation("Config file was not found.", false, true));
  }

  @Test
  public void testNoConfigWithCustomConfigFileSet() {
    tslint.get().doEditorHighlightingTest("data.ts", () -> {
      TsLintConfiguration configuration = TsLintConfiguration.getInstance(tslint.get().getProject());
      TsLintState newState = configuration.getExtendedState()
        .getState()
        .builder()
        .setCustomConfigFilePath(tslint.get().getFixture().getTempDirPath() + "/tslint-nonexistent.json")
        .build();
      configuration.setExtendedState(configuration.isEnabled(), newState);
    });
    tslint.get().setExpectedGlobalAnnotation(new LinterHighlightingFixture.ExpectedGlobalAnnotation("Config file was not found.", false, true));
  }

  @Test
  public void testSuppressMissingConfigFileWithAutodetectPackage() {
    tslint.get().configureLinterForPackage(AutodetectLinterPackage.INSTANCE);
    AutodetectLinterPackage.setTestAutodetectedPackage(tslint.get().getProject(), tslint.get().getNodePackage(), tslint.get().getTestRootDisposable());
    tslint.get().doEditorHighlightingTest("data.ts");
  }

  @Test
  public void testLineSeparatorsWin() {
    tslint.get().doEditorHighlightingTest("data.ts", () -> JSTestUtils.ensureLineSeparators(tslint.get().getFixture().getFile(), LineSeparator.CRLF));
  }

  @Test
  public void testHighlightJsFiles() {
    tslint.get().doEditorHighlightingTest("test.js", () -> {
      TsLintConfiguration configuration = TsLintConfiguration.getInstance(tslint.get().getProject());
      TsLintState newState = configuration.getExtendedState()
        .getState()
        .builder()
        .setAllowJs(true)
        .build();
      configuration.setExtendedState(configuration.isEnabled(), newState);
    });
  }

  @Test
  public void testNoJsFilesByDefault() {
    tslint.get().doEditorHighlightingTest("test.js");
  }

  @Test
  public void testYarnPnpTsLintExample() {
    tslint.get().doEditorHighlightingTest("app.ts", () -> {
      VirtualFile root = Objects.requireNonNull(tslint.get().getFixture().findFileInTempDir("."));
      NodePackage yarnPkg = YarnPnpIntegrationFixture.installYarnGlobally(tslint.get().getNodeJsAppRule());
      YarnPnpIntegrationFixture.configureYarnBerryAndRunYarnInstall(tslint.get().getProject(), yarnPkg, tslint.get().getNodeJsAppRule(), root);
      YarnPnpNodePackage tslintPkg = YarnPnpNodePackage.create(tslint.get().getProject(),
                                                               PackageJsonUtil.findChildPackageJsonFile(root),
                                                               tslint.get().getPackageName(), false, false);
      Assertions.assertNotNull(tslintPkg);
      tslint.get().configureLinterForPackage(NodePackageRef.create(tslintPkg));
    });
  }

  @Test
  public void testFixFile() {
    doFixTest("fix", "TSLint: Fix current file");
  }

  @Test
  public void testFixFileInDumbMode() {
    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, tslint.get().getTestRootDisposable());
    DumbModeTestUtils.runInDumbModeSynchronously(tslint.get().getProject(), () -> {
      doFixTest("fix", "TSLint: Fix current file");
    });
  }

  @Test
  public void testFixSingleError() {
    doFixTest("fix", "TSLint: Fix 'quotemark'");
  }

  @Test
  public void testFixArrowReturnShorthand() {
    doFixTest("main", "TSLint: Fix 'arrow-return-shorthand'");
  }

  @Test
  public void testSuppressRuleForLine() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rule.for.line.description", "quotemark"));
  }

  @Test
  public void testSuppressRuleForLineAddsToExistingComment() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rule.for.line.description", "quotemark"));
  }

  @Test
  public void testSuppressRuleForFileAddsToExistingComment() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.rule.for.file.description", "quotemark"));
  }

  @Test
  public void testSuppressAllRulesForLine() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.all.rules.for.line.description", "TSLint"));
  }

  @Test
  public void testSuppressAllRulesForLineOverwritesExistingSuppressionForRule() {
    doFixTest("main", JavaScriptBundle.message("javascript.linter.suppress.all.rules.for.line.description", "TSLint"));
  }

  private void doFixTest(String mainFileName, String intentionDescription) {
    tslint.get().doFixTestForDirectory(mainFileName, ".ts", intentionDescription);
  }
}
