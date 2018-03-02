package com.intellij.prettierjs;

import com.intellij.javascript.debugger.NodeJsAppRule;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.InstallNpmModules;
import com.intellij.lang.javascript.linter.NodeLinterPackageTestPaths;
import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.lang.javascript.service.JSLanguageServiceUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.util.Collections;

public class ReformatWithPrettierTest extends CodeInsightFixtureTestCase {

  private NodeLinterPackageTestPaths myTestPackagePaths;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(PrettierJSTestUtil.getTestDataPath() + "reformat");

    myTestPackagePaths = ensureNpmPackage("prettier");
    NodeJsLocalInterpreter interpreter = new NodeJsLocalInterpreter(myTestPackagePaths.getNodePath().toString());
    NodeJsInterpreterManager.getInstance(myFixture.getProject()).setInterpreterRef(interpreter.toRef());
    NodePackage nodePackage = new NodePackage(myTestPackagePaths.getPackagePath().toString());
    PrettierConfiguration.getInstance(getProject()).update(interpreter, nodePackage);
    boolean debug = JSLanguageServiceQueue.LOGGER.isDebugEnabled();
    JSLanguageServiceQueue.LOGGER.setLevel(Level.TRACE);
    Disposer.register(getTestRootDisposable(),
                      () -> JSLanguageServiceQueue.LOGGER.setLevel(debug ? Level.DEBUG : Level.ERROR));
  }

  @Override
  protected boolean shouldRunTest() {
    //skip tests requiring npm package under teamcity for now.
    return !IS_UNDER_TEAMCITY;
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      myTestPackagePaths.onTearDown();
    }
    finally {
      super.tearDown();
    }
  }

  public void testWithoutConfig() {
    doReformatFile("js");
  }

  public void testTypeScriptWithoutConfig() {
    //test that parser is autodetected
    doReformatFile("ts");
  }
  
  public void testTypeScriptWithEmptyConfig() {
    //test that parser is autodetected
    doReformatFile("ts");
  }

  public void testWithPackageJsonConfig() {
    doReformatFile("js");
  }

  public void testJsFileWithSelection() {
    doReformatFile("js");
  }

  public void testWithEditorConfig() {
    doReformatFile("js");
  }

  public void testJsonFileDetectedByExtension() {
    doReformatFile("json");
  }

  public void testJsonFileDetectedByName() {
    doReformatFile(".babelrc", "");
  }

  public void testInvalidConfigErrorReported() {
    assertError((s) -> s.contains("tabWidth"), () -> doReformatFile("js"));
  }

  private void doReformatFile(final String extension) {
    doReformatFile("toReformat", extension);
  }

  private void doReformatFile(final String fileNamePrefix, final String extension) {
    String dirName = getTestName(true);
    myFixture.copyDirectoryToProject(dirName, "");
    String extensionWithDot = StringUtil.isEmpty(extension) ? "" : "." + extension;
    myFixture.configureByFile(fileNamePrefix + extensionWithDot);
    myFixture.testAction(new ReformatWithPrettierAction((new ReformatWithPrettierAction.ErrorHandler() {
      @Override
      public void showError(@NotNull Project project, @Nullable Editor editor, 
                            @NotNull String text, @Nullable Runnable onLinkClick) {
        throw new RuntimeException(text);
      }

      @Override
      public void showErrorWithDetails(@NotNull Project project, @Nullable Editor editor,
                                       @NotNull String text, @NotNull String details) {
        throw new RuntimeException(text + " " + details);
      }
    })));
    myFixture.checkResultByFile(dirName + "/" + fileNamePrefix + "_after" + extensionWithDot);
  }

  private NodeLinterPackageTestPaths ensureNpmPackage(String packageName) {
    try {
      final NodeJsAppRule nodeJsAppRule = new NodeJsAppRule("6.10.2");
      nodeJsAppRule.executeBefore();
      final InstallNpmModules npmModules = new InstallNpmModules(nodeJsAppRule, ContainerUtil.list(packageName), Collections.emptyMap());
      npmModules.run();

      return new NodeLinterPackageTestPaths(getProject(), packageName, npmModules.getNpmPath(), npmModules.getNodePath(),
                                            npmModules.getLintersFolder());
    }
    catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
  }

  private static void assertError(Condition<String> checkException, Runnable runnable) {
    try {
      runnable.run();
      Assert.fail("Expected exception but was none");
    }
    catch (Exception e) {
      Assert.assertTrue("Expected condition to be valid for exception: " + e.getMessage(), checkException.value(e.getMessage()));
    }

  }
}
