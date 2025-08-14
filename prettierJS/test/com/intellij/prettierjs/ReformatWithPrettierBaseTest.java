// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.JSExternalToolIntegrationTest;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

public abstract class ReformatWithPrettierBaseTest extends JSExternalToolIntegrationTest {

  @Override
  protected String getMainPackageName() {
    return PrettierUtil.PACKAGE_NAME;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(PrettierJSTestUtil.getTestDataPath() + "reformat");
    PrettierConfiguration.getInstance(getProject())
      .withLinterPackage(NodePackageRef.create(getNodePackage()))
      .getState().configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL;
  }

  protected void doReformatFile(final String extension) {
    doReformatFile("toReformat", extension);
  }

  protected void doReformatFile(final String fileNamePrefix, final String extension) {
    doReformatFile(fileNamePrefix, extension, null);
  }

  protected <T extends Throwable> void doReformatFile(final String fileNamePrefix,
                                                      final String extension,
                                                      @Nullable ThrowableRunnable<T> configureFixture) throws T {
    String dirName = getTestName(true);
    myFixture.copyDirectoryToProject(dirName, "");
    String extensionWithDot = StringUtil.isEmpty(extension) ? "" : "." + extension;
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir(fileNamePrefix + extensionWithDot));
    if (configureFixture != null) {
      configureFixture.run();
    }
    runReformatAction();
    myFixture.checkResultByFile(dirName + "/" + fileNamePrefix + "_after" + extensionWithDot);
  }

  protected void runReformatAction() {
    myFixture.testAction(new ReformatWithPrettierAction());
  }

  protected void assertError(Condition<String> checkException, Runnable runnable) {
    runnable.run();

    var project = myFixture.getProject();
    var manager = PrettierLanguageServiceManager.getInstance(project);
    var service = manager.getJsLinterServices().values().stream().findFirst().orElse(null);
    var error = service.service.getError();
    Assert.assertTrue("Expected condition to be valid for exception: " + error.getMessage(), checkException.value(error.getMessage()));
  }
}