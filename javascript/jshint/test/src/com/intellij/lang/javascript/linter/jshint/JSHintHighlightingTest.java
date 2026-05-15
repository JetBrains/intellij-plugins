package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import org.jetbrains.annotations.NotNull;

public class JSHintHighlightingTest extends BasePlatformTestCase {

  @Override
  protected String getBasePath() {
    return "/highlighting/";
  }

  @Override
  protected String getTestDataPath() {
    return JSHintTestUtil.BASE_TEST_DATA_PATH + getBasePath();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new JSHintInspection());
  }

  public void testSample() {
    JSHintState state = new JSHintState.Builder()
      .setOptionsState(
        new JSHintOptionsState.Builder()
          .put(JSHintOption.STRICT, false)
          .put(JSHintOption.FORIN, true)
          .put(JSHintOption.UNDEF, true)
          .put(JSHintOption.PREDEF, "foo:true, bar")
          .build())
      .build();

    JSHintConfiguration configuration = JSHintConfiguration.getInstance(getProject());
    configuration.setExtendedState(true, state);
    doTestDefault();
  }

  public void testSampleInDumbMode() {
    JSHintState state = new JSHintState.Builder()
      .setOptionsState(
        new JSHintOptionsState.Builder()
          .put(JSHintOption.STRICT, false)
          .put(JSHintOption.FORIN, true)
          .put(JSHintOption.UNDEF, true)
          .put(JSHintOption.PREDEF, "foo:true, bar")
          .build())
      .build();

    JSHintConfiguration configuration = JSHintConfiguration.getInstance(getProject());
    configuration.setExtendedState(true, state);

    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, getTestRootDisposable());
    DumbModeTestUtils.runInDumbModeSynchronously(getProject(), () -> {
      doTestDefault();
    });
  }

  public void testCheck_predef() {
    JSHintState state = new JSHintState.Builder()
      .setConfigFileUsed(true)
      .setOptionsState(new JSHintOptionsState.Builder().build())
      .build();

    JSHintConfiguration configuration = JSHintConfiguration.getInstance(getProject());
    configuration.setExtendedState(true, state);
    doTestDefault();
  }

  public void testIgnoreDistBuild() {
    doTest("dist", "dist/build.js");
  }

  public void testIgnoreDistBuildDefs() {
    doTest("dist", "dist/build.defs.js");
  }

  public void testIgnoreDistClient() {
    doTest("dist", "dist/client/client.js");
  }

  public void testIgnoreDistClientDefs() {
    doTest("dist", "dist/client/client.defs.js");
  }

  public void testIndent() {
    JSHintState state = new JSHintState.Builder()
      .setOptionsState(
        new JSHintOptionsState.Builder()
          .put(JSHintOption.UNDEF, true)
          .put(JSHintOption.UNUSED, true)
          .put(JSHintOption.ESVERSION, 6)
          .put(JSHintOption.INDENT, 2)
          .build())
      .build();
    JSHintConfiguration configuration = JSHintConfiguration.getInstance(getProject());
    configuration.setExtendedState(true, state);
    doTestDefault();
  }

  public void testJSHintWithNodeJS() {
    JSHintState state = new JSHintState.Builder()
      .setOptionsState(
        new JSHintOptionsState.Builder()
          .put(JSHintOption.UNDEF, true)
          .put(JSHintOption.NODE, true)
          .build())
      .build();

    JSHintConfiguration configuration = JSHintConfiguration.getInstance(getProject());
    configuration.setExtendedState(true, state);

    JSTestUtils.setMockNodeCoreModulesLib(getProject(), getTestRootDisposable());
    myFixture.testHighlighting(true, false, true, "JSHint.js");
  }

  private void doTestDefault() {
    myFixture.configureByFiles(getTestName(true) + ".js");
    myFixture.copyFileToProject(".jshintrc");
    myFixture.testHighlighting(true, false, true);
  }

  private void doTest(@NotNull String directoryToCopy, @NotNull String filePathToTest) {
    JSHintState state = new JSHintState.Builder()
      .setConfigFileUsed(true)
      .setOptionsState(new JSHintOptionsState.Builder().build())
      .build();

    JSHintConfiguration configuration = JSHintConfiguration.getInstance(getProject());
    configuration.setExtendedState(true, state);
    myFixture.copyDirectoryToProject(directoryToCopy, directoryToCopy);
    myFixture.copyFileToProject(".jshintrc");
    myFixture.copyFileToProject(".jshintignore");
    myFixture.configureByFile(filePathToTest);
    myFixture.testHighlighting(true, false, true);
  }
}
