// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.JSExternalToolIntegrationTest;
import com.intellij.lang.javascript.nodejs.library.yarn.AbstractYarnPnpIntegrationTest;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.util.LineSeparator;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.IOException;

public class ReformatWithPrettierTest extends JSExternalToolIntegrationTest {

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

  public void testIgnoredFile() {
    doReformatFile("toReformat", "js");
  }

  public void testInvalidConfigErrorReported() {
    assertError((s) -> s.contains("tabWidth"), () -> doReformatFile("js"));
  }

  public void testWithCrlf() throws IOException {
    enableDetailedLogs();
    doReformatFile("toReformat", "js", () -> JSTestUtils.ensureLineSeparators(myFixture.getFile(), LineSeparator.CRLF));
    FileDocumentManager.getInstance().saveAllDocuments();
    // Default Prettier behavior starting from v2.0.0 is 'lf'. See https://prettier.io/docs/en/options.html#end-of-line
    assertEquals(LineSeparator.LF, StringUtil.detectSeparators(VfsUtilCore.loadText(getFile().getVirtualFile())));
  }

  public void testWithUpdatingLfToCrlf() throws IOException {
    doReformatFile("toReformat", "js");
    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(getFile().getVirtualFile())));
  }

  public void testNotSupportedFile() {
    assertError(s -> s.contains("unsupported type"), () -> doReformatFile("test", "txt"));
  }

  public void testFileDetectedByShebangLine() {
    doReformatFile("test", "");
  }

  public void testRunPrettierOnSaveAll() {
    doTestRunPrettierOnSave("SaveAll");
  }

  public void testRunPrettierOnSaveDocument() {
    doTestRunPrettierOnSave("SaveDocument");
  }

  private void doTestRunPrettierOnSave(@NotNull String saveActionId) {
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(getProject());
    boolean origRunOnSave = configuration.getState().runOnSave;
    configuration.getState().runOnSave = true;
    try {
      myFixture.configureByText("foo.js", "var  a=''");
      myFixture.type(' ');
      myFixture.performEditorAction(saveActionId);
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
      myFixture.checkResult("var a = \"\";\n");
    }
    finally {
      configuration.getState().runOnSave = origRunOnSave;
    }
  }

  public void testRunPrettierOnCodeReformat() {
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(getProject());
    boolean origRunOnReformat = configuration.getState().runOnReformat;
    configuration.getState().runOnReformat = true;
    try {
      myFixture.configureByText("foo.js", "var  a=''");
      myFixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT);
      myFixture.checkResult("var a = \"\";\n");
    }
    finally {
      configuration.getState().runOnReformat = origRunOnReformat;
    }
  }

  public void testYarnPrettierBasicExample() throws Exception {
    doReformatFile("toReformat", "js", () -> {
      VirtualFile file = myFixture.findFileInTempDir("toReformat.js");
      VirtualFile root = file.getParent();
      NodePackage yarnPkg = AbstractYarnPnpIntegrationTest.installYarnGlobally(getNodeJsAppRule());
      VfsRootAccess.allowRootAccess(getTestRootDisposable(), yarnPkg.getSystemIndependentPath());
      AbstractYarnPnpIntegrationTest.configureYarnBerryAndRunYarnInstall(getProject(), yarnPkg, getNodeJsAppRule(), root);
      configureYarnPrettierPackage(root);
    });
  }

  public void testIncompleteBlock() {
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(getProject());
    boolean origRunOnReformat = configuration.getState().runOnReformat;
    configuration.getState().runOnReformat = true;
    try {
      String dirName = getTestName(true);
      myFixture.copyDirectoryToProject(dirName, "");
      myFixture.configureFromTempProjectFile("toReformat.js");
      // should be used exactly ACTION_EDITOR_REFORMAT instead of ReformatWithPrettierAction
      // to check a default formatter behavior combined with Prettier
      myFixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT);
      myFixture.checkResultByFile(dirName + "/" + "toReformat_after.js");
    }
    finally {
      configuration.getState().runOnReformat = origRunOnReformat;
    }
  }

  public void testNextVersion() {
    doReformatFile("toReformat", "js", () -> {
      performNpmInstallForPackageJson("package.json");
      NodePackage nextPrettier = new NodePackage(myFixture.getTempDirPath() + "/node_modules/prettier");
      PrettierConfiguration.getInstance(getProject()).withLinterPackage(NodePackageRef.create(nextPrettier));
    });
  }

  public void testResolveConfig() {
    doReformatFile("webc");
  }

  public void testAutoconfigured() {
    PrettierConfiguration.getInstance(getProject())
      .getState().configurationMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC;
    doReformatFile("subdir/formatted", "js", () -> {
      myFixture.getTempDirFixture().copyAll(getNodePackage().getSystemIndependentPath(), "subdir/node_modules/prettier");
    });

    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("ignored.js"));
    runReformatAction();
    myFixture.checkResultByFile(getTestName(true) + "/ignored_after.js");
  }

  private void configureYarnPrettierPackage(VirtualFile root) {
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(getProject());
    YarnPnpNodePackage yarnPrettierPkg = YarnPnpNodePackage.create(getProject(),
                                                                   PackageJsonUtil.findChildPackageJsonFile(root),
                                                                   PrettierUtil.PACKAGE_NAME, false, false);
    assertNotNull(yarnPrettierPkg);
    configuration.withLinterPackage(NodePackageRef.create(yarnPrettierPkg));
    NodePackage readYarnPrettierPkg = configuration.getPackage(null);
    assertInstanceOf(readYarnPrettierPkg, YarnPnpNodePackage.class);
    assertEquals("yarn:package.json:prettier", readYarnPrettierPkg.getSystemIndependentPath());
  }

  private void doReformatFile(final String extension) {
    doReformatFile("toReformat", extension);
  }

  private void doReformatFile(final String fileNamePrefix, final String extension) {
    doReformatFile(fileNamePrefix, extension, null);
  }

  private <T extends Throwable> void doReformatFile(final String fileNamePrefix,
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

  private void runReformatAction() {
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
