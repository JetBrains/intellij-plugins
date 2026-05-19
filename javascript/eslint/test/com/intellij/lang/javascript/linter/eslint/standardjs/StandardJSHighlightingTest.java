package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.javascript.linter.eslint.EslintServiceTestBase;
import com.intellij.lang.javascript.linter.eslint.EslintTestUtil;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSInspection;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StandardJSHighlightingTest extends EslintServiceTestBase {
  public static final String RELATIVE_TESTDATA_PATH = "/linter/standardjs/highlighting/";

  @Override
  protected String getBasePath() {
    return EslintTestUtil.ESLINT_TEST_DATA_RELATIVE_PATH + RELATIVE_TESTDATA_PATH;
  }

  @Override
  protected @NotNull InspectionProfileEntry getInspection() {
    return new StandardJSInspection();
  }

  @Override
  protected @NotNull String getPackageName() {
    return "standard";
  }

  @Override
  protected @NotNull Map<String, String> getGlobalPackageVersionsToInstall() {
    return Map.of(
      "standard", "latest",
      "eslint-plugin-html", "latest",
      "@babel/eslint-parser", "latest");
  }

  public void testWithGlobalInPackageJson() {
    defaultHighlightingTest();
  }

  public void testDumbMode() {
    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, getTestRootDisposable());
    DumbModeTestUtils.runInDumbModeSynchronously(getProject(), () -> {
      defaultHighlightingTest();
    });
  }

  public void testWithPlugin() {
    defaultHighlightingTest();
  }

  public void testWithHtmlPlugin() {
    doHighlightingTest("test.html");
  }

  public void testWithIgnorePattern() {
    doHighlightingTest("test.js", "ignored.js");
  }

  public void testWithBabelParser() {
    configureByFilename(".babelrc.js");
    doHighlightingTest("test.js");
  }

  private void defaultHighlightingTest() {
    doHighlightingTest("test.js");
  }

  private void configureByFilename(@NotNull String filename) {
    myFixture.configureByFiles(getTestName(true) + "/" + filename);
  }

  private void doHighlightingTest(final String... filenames) {
    configureByFilename(PackageJsonUtil.FILE_NAME);
    for (String filename : filenames) {
      myFixture.testHighlighting(true, false, true, getTestName(true) + "/" + filename);
    }
  }
}
