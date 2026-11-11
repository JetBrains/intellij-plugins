package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.linter.eslint.EslintServiceFixture;
import com.intellij.lang.javascript.linter.eslint.EslintTestUtil;
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.intellij.lang.javascript.linter.eslint.EslintServiceFixture.eslintServiceFixture;

@TestApplication
public class StandardJSFixTest {
  private final TestFixture<EslintServiceFixture> standard = eslintServiceFixture(
    new JSTempDirFixtureOptions(EslintTestUtil.getEslintTestDataPath() + "/linter/standardjs/")
      .withMainPackage("standard")
      .withGlobalPackages(Map.of("standard", "latest")),
    () -> new StandardJSInspection());

  @Test
  public void testFixSimple() {
    EslintServiceFixture fixture = standard.get();
    fixture.doFixTestForDirectory(fixture.getTestName(false), ".js", JavaScriptBundle.message("javascript.linter.action.fix.problems.file.text",
                                                                                            EslintBundle.message("standardjs.name")));
  }


  @Test
  public void testFixSimpleInDumbMode() {
    EslintServiceFixture fixture = standard.get();
    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, fixture.getTestRootDisposable());
    DumbModeTestUtils.runInDumbModeSynchronously(fixture.getProject(), () -> {
      fixture.doFixTestForDirectory(fixture.getTestName(false), ".js", JavaScriptBundle.message("javascript.linter.action.fix.problems.file.text",
                                                                                              EslintBundle.message("standardjs.name")));
    });
  }

  @Test
  public void testSuppressForLine() {
    EslintServiceFixture fixture = standard.get();
    fixture.doFixTestForDirectory(fixture.getTestName(false), ".js",
                                  JavaScriptBundle.message("javascript.linter.suppress.rule.for.line.description", "semi"));
  }
}
