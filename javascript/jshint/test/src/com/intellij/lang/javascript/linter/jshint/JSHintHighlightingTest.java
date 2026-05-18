package com.intellij.lang.javascript.linter.jshint;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.lang.javascript.linter.jshint.config.JSHintDescriptor;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class JSHintHighlightingTest extends LinterHighlightingTest {

  @Override
  protected @NotNull InspectionProfileEntry getInspection() {
    return new JSHintInspection();
  }

  @Override
  protected @NotNull Map<String, String> getGlobalPackageVersionsToInstall() {
    HashMap<String, String> map = new HashMap<>();
    map.put(JSHintDescriptor.PACKAGE_NAME, null);
    return map;
  }

  @Override
  protected @NotNull String getPackageName() {
    return JSHintDescriptor.PACKAGE_NAME;
  }

  @Override
  protected String getBasePath() {
    return "/contrib/javascript/jshint/test/data/highlighting/";
  }

  public void testSample() {
    doTestWithState(new JSHintState.Builder()
      .setOptionsState(
        new JSHintOptionsState.Builder()
          .put(JSHintOption.STRICT, false)
          .put(JSHintOption.FORIN, true)
          .put(JSHintOption.UNDEF, true)
          .put(JSHintOption.PREDEF, "foo:true, bar")
          .build())
      .build());
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

    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, getTestRootDisposable());
    DumbModeTestUtils.runInDumbModeSynchronously(getProject(), () -> {
      doTestWithState(state);
    });
  }

  public void testCheck_predef() {
    doTestWithState(new JSHintState.Builder()
      .setConfigFileUsed(true)
      .setOptionsState(new JSHintOptionsState.Builder().build())
      .build());
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
    doTestWithState(new JSHintState.Builder()
      .setOptionsState(
        new JSHintOptionsState.Builder()
          .put(JSHintOption.UNDEF, true)
          .put(JSHintOption.UNUSED, true)
          .put(JSHintOption.ESVERSION, 6)
          .put(JSHintOption.INDENT, 2)
          .build())
      .build());
  }

  public void testJSHintWithNodeJS() {
    JSHintState state = new JSHintState.Builder()
      .setOptionsState(
        new JSHintOptionsState.Builder()
          .put(JSHintOption.UNDEF, true)
          .put(JSHintOption.NODE, true)
          .build())
      .build();

    configureWithState(state);
    JSTestUtils.setMockNodeCoreModulesLib(getProject(), getTestRootDisposable());
    myFixture.testHighlighting(true, false, true, "JSHint.js");
  }

  private void doTestWithState(@NotNull JSHintState state) {
    configureWithState(state);
    myFixture.configureByFiles(getTestName(true) + ".js");
    myFixture.copyFileToProject(".jshintrc");
    myFixture.testHighlighting(true, false, true);
  }

  private void doTest(@NotNull String directoryToCopy, @NotNull String filePathToTest) {
    JSHintState state = new JSHintState.Builder()
      .setConfigFileUsed(true)
      .setOptionsState(new JSHintOptionsState.Builder().build())
      .build();

    configureWithState(state);
    myFixture.copyDirectoryToProject(directoryToCopy, directoryToCopy);
    myFixture.copyFileToProject(".jshintrc");
    myFixture.copyFileToProject(".jshintignore");
    myFixture.configureByFile(filePathToTest);
    myFixture.testHighlighting(true, false, true);
  }

  private void configureWithState(@NotNull JSHintState state) {
    JSHintConfiguration configuration = JSHintConfiguration.getInstance(getProject());
    configuration.setExtendedState(true, state.withLinterPackage(NodePackageRef.create(getNodePackage())));
  }
}
