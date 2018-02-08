package com.intellij.prettierjs;

import com.intellij.javascript.debugger.NodeJsAppRule;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.linter.InstallNpmModules;
import com.intellij.lang.javascript.linter.NodeLinterPackageTestPaths;
import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.intellij.util.containers.ContainerUtil;
import org.apache.log4j.Level;

import java.util.Collections;

public class ReformatWithPrettierTest extends CodeInsightFixtureTestCase {

  private NodeLinterPackageTestPaths myTestPackagePaths;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(PrettierJSTestUtil.getTestDataPath() + "reformat");

    myTestPackagePaths = ensureNpmPackage("prettier");
    String npmPath = myTestPackagePaths.getNpmPath().toString();
    NodeJsLocalInterpreter interpreter = new NodeJsLocalInterpreter(myTestPackagePaths.getNodePath().toString());
    NodeJsInterpreterManager.getInstance(myFixture.getProject()).setInterpreterRef(interpreter.toRef());
    PrettierConfiguration.getInstance(getProject()).update(interpreter, new NodePackage(myTestPackagePaths.getPackagePath().toString()));
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

  public void testWithPackageJsonConfig() {
    doReformatFile("js");
  }

  public void testJsFileWithSelection() {
    doReformatFile("js");
  }

  public void testWithEditorConfig() {
    doReformatFile("js");
  }

  private void doReformatFile(final String extension) {
    String dirName = getTestName(true);
    myFixture.copyDirectoryToProject(dirName, "");
    myFixture.configureByFile("toReformat." + extension);
    myFixture.testAction(new ReformatWithPrettierAction());
    myFixture.checkResultByFile(dirName + "/toReformat_after." + extension);
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
}
