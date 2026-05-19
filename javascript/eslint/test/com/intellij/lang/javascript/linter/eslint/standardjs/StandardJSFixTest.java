package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.javascript.linter.eslint.EslintBundle;
import com.intellij.lang.javascript.linter.eslint.EslintServiceTestBase;
import com.intellij.lang.javascript.linter.eslint.EslintTestUtil;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSInspection;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StandardJSFixTest extends EslintServiceTestBase {
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
    return Map.of("standard", "latest");
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    String basePath = EslintTestUtil.getEslintTestDataPath() + "/linter/standardjs/";
    myFixture.setTestDataPath(basePath);
  }

  public void testFixSimple() {
    doFixTestForDirectory(getTestName(false), ".js", JavaScriptBundle.message("javascript.linter.action.fix.problems.file.text",
                                                                              EslintBundle.message("standardjs.name")));
  }


  public void testFixSimpleInDumbMode() {
    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, getTestRootDisposable());
    DumbModeTestUtils.runInDumbModeSynchronously(getProject(), () -> {
      doFixTestForDirectory(getTestName(false), ".js", JavaScriptBundle.message("javascript.linter.action.fix.problems.file.text",
                                                                                EslintBundle.message("standardjs.name")));
    });
  }

  public void testSuppressForLine() {
    doFixTestForDirectory(getTestName(false), ".js",
                          JavaScriptBundle.message("javascript.linter.suppress.rule.for.line.description", "semi"));
  }
}