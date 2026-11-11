package com.intellij.lang.javascript.linter.jshint;

import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.linter.JSLinterDescriptor;
import com.intellij.lang.javascript.linter.LinterHighlightingFixture;
import com.intellij.lang.javascript.linter.jshint.config.JSHintDescriptor;
import com.intellij.lang.javascript.modules.JSTempDirFixture;
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions;
import com.intellij.testFramework.DumbModeTestUtils;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@TestApplication
public class JSHintHighlightingTest {
  private final TestFixture<LinterHighlightingFixture> jshint = JSTempDirFixture.jsTempDirFixture(
    "jsHintFixture",
    JSTempDirFixtureOptions.forBasePath("/contrib/javascript/jshint/test/data/highlighting/")
      .withoutDefaultInspections()
      .withMainPackage(JSHintDescriptor.PACKAGE_NAME)
      .withGlobalPackages(globalPackageVersions()),
    (fixture, testClass, testMethod, options) -> new LinterHighlightingFixture(fixture, testClass, testMethod, options,
                                                                               () -> new JSHintInspection()) {
      @Override
      protected @NotNull JSLinterDescriptor findLinterDescriptor() {
        JSHintDescriptor descriptor = ContainerUtil.findInstance(JSLinterDescriptor.EP_NAME.getExtensionList(), JSHintDescriptor.class);
        Assertions.assertNotNull(descriptor);
        return descriptor;
      }
    });

  private static @NotNull Map<String, String> globalPackageVersions() {
    HashMap<String, String> map = new HashMap<>();
    map.put(JSHintDescriptor.PACKAGE_NAME, null);
    return map;
  }

  @Test
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

  @Test
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

    CodeInsightTestFixtureImpl.mustWaitForSmartMode(false, jshint.get().getTestRootDisposable());
    DumbModeTestUtils.runInDumbModeSynchronously(jshint.get().getProject(), () -> {
      doTestWithState(state);
    });
  }

  @Test
  public void testCheck_predef() {
    doTestWithState(new JSHintState.Builder()
      .setConfigFileUsed(true)
      .setOptionsState(new JSHintOptionsState.Builder().build())
      .build());
  }

  @Test
  public void testIgnoreDistBuild() {
    doTest("dist", "dist/build.js");
  }

  @Test
  public void testIgnoreDistBuildDefs() {
    doTest("dist", "dist/build.defs.js");
  }

  @Test
  public void testIgnoreDistClient() {
    doTest("dist", "dist/client/client.js");
  }

  @Test
  public void testIgnoreDistClientDefs() {
    doTest("dist", "dist/client/client.defs.js");
  }

  @Test
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

  @Test
  public void testJSHintWithNodeJS() {
    JSHintState state = new JSHintState.Builder()
      .setOptionsState(
        new JSHintOptionsState.Builder()
          .put(JSHintOption.UNDEF, true)
          .put(JSHintOption.NODE, true)
          .build())
      .build();

    configureWithState(state);
    JSTestUtils.setMockNodeCoreModulesLib(jshint.get().getProject(), jshint.get().getTestRootDisposable());
    jshint.get().getFixture().testHighlighting(true, false, true, "JSHint.js");
  }

  private void doTestWithState(@NotNull JSHintState state) {
    configureWithState(state);
    CodeInsightTestFixture myFixture = jshint.get().getFixture();
    myFixture.configureByFiles(jshint.get().getTestName(true) + ".js");
    myFixture.copyFileToProject(".jshintrc");
    myFixture.testHighlighting(true, false, true);
  }

  private void doTest(@NotNull String directoryToCopy, @NotNull String filePathToTest) {
    JSHintState state = new JSHintState.Builder()
      .setConfigFileUsed(true)
      .setOptionsState(new JSHintOptionsState.Builder().build())
      .build();

    configureWithState(state);
    CodeInsightTestFixture myFixture = jshint.get().getFixture();
    myFixture.copyDirectoryToProject(directoryToCopy, directoryToCopy);
    myFixture.copyFileToProject(".jshintrc");
    myFixture.copyFileToProject(".jshintignore");
    myFixture.configureByFile(filePathToTest);
    myFixture.testHighlighting(true, false, true);
  }

  private void configureWithState(@NotNull JSHintState state) {
    JSHintConfiguration configuration = JSHintConfiguration.getInstance(jshint.get().getProject());
    configuration.setExtendedState(true, state.withLinterPackage(NodePackageRef.create(jshint.get().getNodePackage())));
  }
}
