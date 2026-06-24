package com.intellij.lang.javascript.linter.eslint;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.ide.trustedProjects.TrustedProjects;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.AutodetectLinterPackage;
import com.intellij.lang.javascript.linter.InstallNpmModules;
import com.intellij.lang.javascript.linter.JSLinterAnnotationResult;
import com.intellij.lang.javascript.linter.JSLinterFileLevelAnnotation;
import com.intellij.lang.javascript.linter.eslint.service.EslintLanguageServiceManager;
import com.intellij.lang.javascript.nodejs.library.yarn.AbstractYarnPnpIntegrationTest;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.profile.codeInspection.InspectionProfileManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.InspectionTestUtil;
import com.intellij.testFramework.InspectionsKt;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.testFramework.fixtures.impl.GlobalInspectionContextForTests;
import com.intellij.util.Function;
import com.intellij.util.LineSeparator;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static com.intellij.lang.javascript.inspections.JSInspection.calcShortNameFromClass;

public class ESLintHighlightingTest extends EslintServiceTestBase {
  @Override
  protected String getBasePath() {
    return EslintTestUtil.ESLINT_TEST_DATA_RELATIVE_PATH + "/linter/eslint/highlighting/";
  }

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
                  "vue-eslint-parser", "latest",
                  "@typescript-eslint/parser", "latest",
                  "typescript", "latest");
  }

  @Override
  protected @Nullable String getAnnotationText() {
    var file = myFixture.getFile().getVirtualFile();
    var state = EslintConfiguration.getInstance(getProject()).getExtendedState().getState();
    var annotation = EslintLanguageServiceManager.getInstance(getProject()).useService(file, state.getNodePackageRef(), service -> {
      return service != null ? service.getFileLevelAnnotation() : null;
    });

    return annotation != null ? annotation.getMessage() : null;
  }

  public void testWarningsAndErrors() {
    doTest("warn.js");
  }

  // WEB-78439: an untrusted project must not be able to start/use the project-local ESLint language service (a Node process).
  // warn.js would be flagged (no-console / no-debugger), but no highlighting is expected because the project is untrusted.
  public void testNoLintingForUntrustedProject() {
    TrustedProjects.setProjectTrusted(getProject(), false);
    try {
      doEditorHighlightingTest("warn.js");
    }
    finally {
      TrustedProjects.setProjectTrusted(getProject(), true);
    }
  }

  public void testDumbMode() {
    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, getTestRootDisposable());
    DumbModeTestUtils.runInDumbModeSynchronously(getProject(), () -> {
      doEditorHighlightingTest("warn.js");
    });
    doBatchInspectionTest();
  }

  public void testMultilineError() {
    doTest("test.js");
  }

  public void testOverrideConfigSeverityFromInspection() {
    JSTestUtils.doWithChangedInspectionHighlightLevel(getProject(), calcShortNameFromClass(EslintInspection.class),
                                                      HighlightDisplayLevel.WEAK_WARNING, () -> {
        EslintInspection inspection = ((EslintInspection)InspectionProfileManager
          .getInstance(getProject())
          .getCurrentProfile()
          .getInspectionTool(calcShortNameFromClass(EslintInspection.class), getProject()).getTool());
        inspection.useSeverityFromConfigFile = false;
        doEditorHighlightingTest("test.js");
      });
  }

  public void testEolLastNever() {
    doTest("test.js");
  }

  public void testReportAboutWrongParser() {
    doTest("test.js");
  }

  public void testLineSeparatorsWin() {
    doEditorHighlightingTest("test.js", () -> JSTestUtils.ensureLineSeparators(myFixture.getFile(), LineSeparator.CRLF));
    doBatchInspectionTest();
  }

  public void testESLintGlobalFatalError() {
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation("ESLint: Error: Failed to load parser 'babel'", true, false);
    doEditorHighlightingTest("test.jsx");
  }

  public void testTimeout() {
    // Verify ESLint timeout handling (a slow analysis -> file-level timeout annotation) deterministically, WITHOUT
    // spawning a real node service. See EslintServiceTestBase#highlightWithNeverRespondingService for the rationale
    // (the real-service version was disposed mid-startup at tear-down and leaked its startup wait / reader thread,
    // WEB-67172).
    myFixture.copyDirectoryToProject(getTestName(false), "");
    PsiFile psiFile = myFixture.configureByFile("test.js");
    JSLanguageServiceUtil.setTimeout(1, getTestRootDisposable());

    JSLinterAnnotationResult result = highlightWithNeverRespondingService(psiFile);

    JSLinterFileLevelAnnotation annotation = result.getFileLevelError();
    assertNotNull("Expected a file-level timeout annotation", annotation);
    String expected = JSLanguageServiceUtil.getTimeoutMessage("test.js", EslintUtil.getTimeout());
    assertTrue("Actual annotation: " + annotation.getMessage(), annotation.getMessage().contains(expected));
  }

  public void testESLintLocalFatalError() {
    doTest("test.js");
  }

  public void testFileIgnored() {
    doTest("testIgnored.js");
  }

  public void testTypescript() {
    doEditorHighlightingTest("ts.ts");
  }

  public void testFileIgnoredByCommandLineOption() {
    doEditorHighlightingTest("testIgnored.js", () -> updateConfiguration(builder -> builder.setExtraOptions("--ignore-pattern '*.js'")));
  }

  public void testFileIgnoredWithPackageJsonOption() {
    //WEB-30783
    doEditorHighlightingTest("src/ignoredDir/test.js");
  }

  public void testMissingConfigErrorReported() {
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation("ESLint: " + "Error: No ESLint configuration found", true, false);
    doEditorHighlightingTest("test.js");
  }

  public void testSuppressMissingConfigErrorWithAutodetectPackage() {
    configureLinterForPackage(AutodetectLinterPackage.INSTANCE);
    AutodetectLinterPackage.setTestAutodetectedPackage(myFixture.getProject(), getNodePackage(), myFixture.getTestRootDisposable());
    doEditorHighlightingTest("test.js");
  }

  public void testSuppressMissingConfigErrorForTypescript() {
    doEditorHighlightingTest("test.ts");
  }

  public void testEslintignoreInSubpackageAndParent() {
    //WEB-36096
    doEditorHighlightingTest("packages/inner/js.js");
  }

  public void testWithRulesInOptions() {
    doEditorHighlightingTest("test.js", () -> updateConfiguration(builder -> builder.setExtraOptions("--rule 'no-console: 1'")));
    FileDocumentManager.getInstance().saveAllDocuments();
    doEditorHighlightingTest("test.js", () -> updateConfiguration(builder -> builder.setExtraOptions("--rule \"no-console: 'warn'\"")));
  }

  public void testCanDisableIgnoreFilesWithCommandLineOption() {
    doEditorHighlightingTest("test.js", () -> updateConfiguration(builder -> builder.setExtraOptions("--no-ignore")));
  }

  public void testCanAutodetectLocalPackage() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("js.js");
  }

  public void testCanAutodetectLocalPackageInParentNodeModules() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("workspaces/a/js.js");
  }

  public void testCanAutodetectInstalledPackageWithoutExplicitDependency() {
    //Eslint could have been installed as a transitive dependency, for example, by create-react-app
    configureLinterForPackage(AutodetectLinterPackage.INSTANCE);
    doEditorHighlightingTest("js.js", () -> performNpmInstallWithArguments("", "--no-save", "eslint@latest"));
  }

  public void testWithCustomRulesDirectories() {
    //custom rule directories from command line options and from 'additional rules directory' input should be merged
    doEditorHighlightingTest("js.js", () -> updateConfiguration(builder -> {
      TempDirTestFixture tempDirFixture = myFixture.getTempDirFixture();
      return builder.setAdditionalRulesDirPath(tempDirFixture.getFile("customRules1").getPath())
        .setExtraOptions("--rulesdir " + tempDirFixture.getFile("customRules2").getPath());
    }));
  }

  public void testEslintignoreWithRelativePathInProjectSubPackage() {
    configureLinterForPackage(AutodetectLinterPackage.INSTANCE);
    //TODO. use yarn because this causes indirect dependencies to be indexed so test is much slower
    //this emulates package install location being hoisted to root of project by yarn/lerna while we don't use real yarn in these tests
    ThrowableRunnable<RuntimeException> setup = () -> performNpmInstallWithArguments("", "--no-save", "eslint@8.57.0");
    doEditorHighlightingTest("packages/with-eslint-ignore/src/ignored.js", setup);
  }

  public void testEslintIgnoreWithRelativePathInProjectSubDirectory() {
    doEditorHighlightingTest("packages/foo/bar/src/ignored.js");
  }

  public void testVueFile() {
    assertNotNull("This test must be run with intellij.vuejs module in classpath", Language.findLanguageByID("Vue"));
    doEditorHighlightingTest("vue.vue");
  }

  public void testVueTsFile() {
    assertNotNull("This test must be run with intellij.vuejs module in classpath", Language.findLanguageByID("Vue"));
    doEditorHighlightingTest("vue.vue");
  }

  public void testTypescriptWithVueParserAbsolutePath() throws IOException {
    File parserFile = new File(getNodePackage().getSystemDependentPath(), "../@typescript-eslint/parser/dist/parser.js").getCanonicalFile();
    assertTrue(parserFile.toString(), parserFile.exists());
    myFixture.setCaresAboutInjection(false);
    myFixture.configureByText(
      ".eslintrc.json",
      "{\n" +
      "  \"parserOptions\": {\n" +
      "    \"parser\": \"" + parserFile.getPath().replace('\\', '/') + "\"\n" +
      "  },\n" +
      "  \"parser\": \"vue-eslint-parser\",\n" +
      "  \"rules\": {\n" +
      "    \"no-console\": \"error\"\n" +
      "  }\n" +
      "}");
    myFixture.configureByText("ts.ts", "<error descr=\"ESLint: Unexpected console statement. (no-console)\">console.log</error>('hello')");
    myFixture.testHighlighting(true, false, true);
  }

  public void testConfigReferencesLocalFiles() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("packages/a/js.js");
  }

  public void testEslintRcInSubpackage() {
    doEditorHighlightingTest("packages/inner/js.js");
  }

  public void testImplicitDependencyButEslintConfigInSubpackage() {
    // inner package.json contains "eslintConfig" -> we check that eslint is taken from the inner node_modules, although it doesn't have
    // implicit eslint dependency
    myExpectedGlobalAnnotation = new ExpectedGlobalAnnotation("packages/inner/node_modules/eslint/lib/options", false, true);
    configureLinterForPackage(AutodetectLinterPackage.INSTANCE);
    doEditorHighlightingTest("packages/inner/js.js");
  }

  public void testSubpackageContainsOnlyLinkToParentEslint() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("packages/inner/js.js");
  }

  public void testYarnPnpEslintExample() throws Exception {
    doEditorHighlightingTest("app.js", () -> {
      VirtualFile root = Objects.requireNonNull(myFixture.findFileInTempDir("."));
      NodePackage yarnPkg = AbstractYarnPnpIntegrationTest.installYarnGlobally(getNodeJsAppRule());
      VfsRootAccess.allowRootAccess(getTestRootDisposable(), yarnPkg.getSystemIndependentPath());
      AbstractYarnPnpIntegrationTest.configureYarnBerryAndRunYarnInstall(getProject(), yarnPkg, getNodeJsAppRule(), root);
      YarnPnpNodePackage yarnEslintPkg = YarnPnpNodePackage.create(getProject(),
                                                                   PackageJsonUtil.findChildPackageJsonFile(root),
                                                                   getPackageName(), false, false);
      assertNotNull(yarnEslintPkg);
      configureLinterForPackage(NodePackageRef.create(yarnEslintPkg));
    });
  }

  public void testNextEslintVersion() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("index.js");
  }

  public void testFlatConfigOneDir() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("index.js");
  }

  public void testFlatConfigSubdirs() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("src/sub/dir/index.js");
  }

  public void testFlatConfigNoHtmlPlugin() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("index.html");
  }

  public void testFlatTypescriptConfigOneDir() {
    updateConfiguration(builder -> builder.setExtraOptions("--flag unstable_ts_config"));
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("index.js");
  }

  public void testFlatTypescriptConfigSubDirs() {
    updateConfiguration(builder -> builder.setExtraOptions("--flag unstable_ts_config"));
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("src/index.js");
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("src/sub/index.js");
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("src/sub/dir/index.js");
  }

  public void testFlatMixedConfigSubDirs() {
    updateConfiguration(builder -> builder.setExtraOptions("--flag unstable_ts_config"));
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("src/index.js");
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("src/sub/index.js");
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("src/sub/dir/index.js");
  }

  public void testCustomFlatConfig() {
    updateConfiguration(builder -> {
      String dir = getTestName(false);
      var configPsiFile = myFixture.configureByFile(dir + "/eslint.config.fast.mjs");
      return builder
        .setCustomConfigFileUsed(true)
        .setCustomConfigFilePath(VfsUtilCore.virtualToIoFile(configPsiFile.getVirtualFile()).getAbsolutePath());
    });
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("index.js");
  }

  public void testCustomLegacyConfig() {
    updateConfiguration(builder -> {
      String dir = getTestName(false);
      var configPsiFile = myFixture.configureByFile(dir + "/.eslintrc.fast.json");
      return builder
        .setCustomConfigFileUsed(true)
        .setCustomConfigFilePath(VfsUtilCore.virtualToIoFile(configPsiFile.getVirtualFile()).getAbsolutePath());
    });
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("index.js");
  }

  public void testFallbackToLegacyConfig() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("index.js");
  }


  public void testHtmlFileFlatConfig() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("index.html");
  }

  public void testHtmlFileFlatConfigFromLib() {
    doEditorHighlightingTestWithLocalNpmInstallFromPackageJson("index.html");
  }

  private void doTest(@NotNull String mainFileRelativePath) {
    doEditorHighlightingTest(mainFileRelativePath);
    doBatchInspectionTest();
  }

  private void doEditorHighlightingTestWithLocalNpmInstallFromPackageJson(@NotNull String mainFileRelativePath) {
    configureLinterForPackage(AutodetectLinterPackage.INSTANCE);
    doEditorHighlightingTest(mainFileRelativePath, () -> performNpmInstallForPackageJson("package.json"));
  }

  @SuppressWarnings("SameParameterValue")
  private void performNpmInstallWithArguments(String relativeDir, String... arguments) {
    withBatchChange(
      () -> new InstallNpmModules(getNodeJsAppRule()).runNpmInstallCommand(myFixture.findFileInTempDir(relativeDir), arguments));
  }

  private void doBatchInspectionTest() {
    doBatchInspectionTest(getTestName(false));
  }

  private void updateConfiguration(Function<? super EslintState.Builder, ? extends EslintState.Builder> configure) {
    final EslintConfiguration configuration = EslintConfiguration.getInstance(getProject());
    EslintState.Builder builder = new EslintState.Builder(configuration.getExtendedState().getState());
    builder = configure.fun(builder);
    configuration.setExtendedState(true, builder.build());
  }

  // this we need to additionally test the conversion Annotation -> ProblemDescriptor
  private void doBatchInspectionTest(String directoryName) {
    final LocalInspectionToolWrapper toolWrapper = new LocalInspectionToolWrapper(new EslintInspection());

    AnalysisScope scope = new AnalysisScope(myFixture.getProject());
    scope.invalidate();

    final GlobalInspectionContextForTests globalContext = InspectionsKt
      .createGlobalContextForTool(scope, getProject(), Collections.<InspectionToolWrapper<?, ?>>singletonList(toolWrapper));

    InspectionTestUtil.runTool(toolWrapper, scope, globalContext);
    InspectionTestUtil.compareToolResults(globalContext, toolWrapper, false, new File(getTestDataPath(), directoryName).getPath());
  }
}
