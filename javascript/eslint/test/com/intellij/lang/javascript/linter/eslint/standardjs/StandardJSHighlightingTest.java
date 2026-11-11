package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.eslint.EslintServiceFixture;
import com.intellij.lang.javascript.linter.eslint.EslintTestUtil;
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.intellij.lang.javascript.linter.eslint.EslintServiceFixture.eslintServiceFixture;

@TestApplication
public class StandardJSHighlightingTest {
  public static final String RELATIVE_TESTDATA_PATH = "/linter/standardjs/highlighting/";

  private final TestFixture<EslintServiceFixture> standard = eslintServiceFixture(
    JSTempDirFixtureOptions.forBasePath(EslintTestUtil.ESLINT_TEST_DATA_RELATIVE_PATH + RELATIVE_TESTDATA_PATH)
      .withMainPackage("standard")
      .withGlobalPackages(Map.of(
        "standard", "latest",
        "eslint-plugin-html", "latest",
        "@babel/eslint-parser", "latest")),
    () -> new StandardJSInspection());

  @Test
  public void testWithGlobalInPackageJson() {
    defaultHighlightingTest();
  }

  @Test
  public void testDumbMode() {
    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, standard.get().getTestRootDisposable());
    DumbModeTestUtils.runInDumbModeSynchronously(standard.get().getProject(), () -> {
      defaultHighlightingTest();
    });
  }

  @Test
  public void testWithPlugin() {
    defaultHighlightingTest();
  }

  @Test
  public void testWithHtmlPlugin() {
    doHighlightingTest("test.html");
  }

  @Test
  public void testWithIgnorePattern() {
    doHighlightingTest("test.js", "ignored.js");
  }

  @Test
  public void testWithBabelParser() {
    configureByFilename(".babelrc.js");
    doHighlightingTest("test.js");
  }

  private void defaultHighlightingTest() {
    doHighlightingTest("test.js");
  }

  private void configureByFilename(@NotNull String filename) {
    standard.get().getFixture().configureByFiles(standard.get().getTestName(true) + "/" + filename);
  }

  private void doHighlightingTest(final String... filenames) {
    configureByFilename(PackageJsonUtil.FILE_NAME);
    for (String filename : filenames) {
      standard.get().getFixture().testHighlighting(true, false, true, standard.get().getTestName(true) + "/" + filename);
    }
  }
}
