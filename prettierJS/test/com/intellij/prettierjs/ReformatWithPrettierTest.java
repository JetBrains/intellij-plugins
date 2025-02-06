// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.ActionsOnSaveTestUtil;
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

  public void testSubFolderIgnoredFileInRoot() {
    doReformatFile("package/toReformat", "js");
  }

  public void testSubFolderIgnoredFileInsidePackage() {
    doReformatFile("package/toReformat", "js");
  }

  public void testSubFolderIgnoredFileInsideSubDir() {
    doReformatFile("package/subdir/toReformat", "js");
  }

  public void testSubFolderIgnoredFileManual() {
    PrettierConfiguration.getInstance(getProject())
      .getState().configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL;
    doReformatFile("package/toReformat", "js");
  }

  public void testSubFolderIgnoredFileManualSubDir() {
    doReformatFile("package/toReformat", "js", () -> {
      PrettierConfiguration.getInstance(getProject())
        .getState().configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL;

      var ignoreFile = myFixture.findFileInTempDir(".prettierignore");
      PrettierConfiguration.getInstance(getProject())
        .getState().customIgnorePath = VfsUtilCore.virtualToIoFile(ignoreFile).getAbsolutePath();
    });
  }

  public void testSubFolderIgnoredFileManualSubDirFormat() {
    doReformatFile("package/toReformat", "js", () -> {
      PrettierConfiguration.getInstance(getProject())
        .getState().configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL;

      var ignoreFile = myFixture.findFileInTempDir(".prettierignore");
      PrettierConfiguration.getInstance(getProject())
        .getState().customIgnorePath = VfsUtilCore.virtualToIoFile(ignoreFile).getAbsolutePath();
    });
  }

  public void testInvalidConfigErrorReported() {
    assertError((s) -> s.contains("tabWidth"), () -> doReformatFile("js"));
  }

  public void testWithCrlf() throws IOException {
    enableDetailedLogs();
    doReformatFile("toReformat", "js", () -> JSTestUtils.ensureLineSeparators(myFixture.getFile(), LineSeparator.CRLF));
    FileDocumentManager.getInstance().saveAllDocuments();
    assertEquals(LineSeparator.CRLF, StringUtil.detectSeparators(VfsUtilCore.loadText(getFile().getVirtualFile())));
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
    configureRunOnSave(() -> doTestSaveAction(saveActionId, ""));
  }

  public void testRunPrettierOnCodeReformat() {
    configureRunOnReformat(() -> doTestEditorReformat(""));
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

  public void testMonorepoSubDirReformatAction() {
    // file in the root without prettier but it should be formatted via reformat action
    String dirName = getTestName(true);
    myFixture.copyDirectoryToProject(dirName, "");
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat.js"));
    runReformatAction();
    myFixture.checkResultByFile(dirName + "/toReformat_after.js");
  }

  public void testMonorepoSubDirEditorReformat() {
    configureRunOnReformat(() -> {
      //file in the root without prettier
      doTestEditorReformat("");

      //package with prettier in subfolder
      doTestEditorReformat("package-a/");

      //package without prettier in subfolder
      doTestEditorReformat("package-b/");
    });
  }

  public void testMonorepoSubDirOnSave() {
    configureRunOnSave(() -> {
      var actionId = "SaveDocument";

      //file in the root without prettier
      doTestSaveAction(actionId, "");

      //package with prettier in subfolder
      doTestSaveAction(actionId, "package-a/");

      //package without prettier in subfolder
      doTestSaveAction(actionId, "package-b/");
    });
  }

  public void testMonorepoOnSave() {
    configureRunOnSave(() -> {
      var actionId = "SaveDocument";

      //file in the root without prettier
      doTestSaveAction(actionId, "");

      //package with prettier in subfolder
      doTestSaveAction(actionId, "package-a/");

      //package without prettier in subfolder
      doTestSaveAction(actionId, "package-b/");
    });
  }

  public void testMonorepoSubDirOnSaveManualWithoutScope() {
    configureFormatFilesOutsideDependencyScope(true, () -> {
      var actionId = "SaveDocument";

      //file in the root without prettier
      doTestSaveAction(actionId, "");
      doTestEditorReformat("");

      //package with prettier in subfolder
      doTestSaveAction(actionId, "package-a/");
      doTestEditorReformat("package-a/");

      //package without prettier in subfolder
      doTestSaveAction(actionId, "package-b/");
      doTestEditorReformat("package-b/");
    });
  }

  public void testMonorepoSubDirOnSaveManualWithScopeOnSave() {
    configureFormatFilesOutsideDependencyScope(false, () -> {
      var actionId = "SaveDocument";

      //file in the root without prettier
      doTestSaveAction(actionId, "");

      //package with prettier in subfolder
      doTestSaveAction(actionId, "package-a/");

      //package without prettier in subfolder
      doTestSaveAction(actionId, "package-b/");
    });
  }

  public void testMonorepoSubDirOnSaveManualWithScopeEditorReformat() {
    configureFormatFilesOutsideDependencyScope(false, () -> {
      //file in the root without prettier
      doTestEditorReformat("");

      //package with prettier in subfolder
      doTestEditorReformat("package-a/");

      //package without prettier in subfolder
      doTestEditorReformat("package-b/");
    });
  }

  public void testChangeConfig() {
    var dirName = getTestName(true);
    myFixture.copyDirectoryToProject(dirName, "");
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir("toReformat_after.js"));

    // set singleQuote to true
    var config = myFixture.createFile("prettier.config.mjs", """
      const config = {
        singleQuote: true,
      }
      
      export default config
      """);
    runReformatAction();
    myFixture.checkResultByFile(dirName + "/toReformat_after.js");

    // change singleQuote to false
    myFixture.saveText(config, """
      const config = {
        singleQuote: false,
      }
      
      export default config
      """);
    runReformatAction();
    myFixture.checkResultByFile(dirName + "/toReformat_after_1.js");
  }

  public void testCommentAfterImports() {
    configureRunOnReformat(() -> doTestEditorReformat(""));
  }

  public void testRangeInVue() {
    // Prettier doesn't support range formatting in Vue (WEB-52196, WEB-52196, https://github.com/prettier/prettier/issues/13399),
    // and even removes line break at the end of the file. This test checks IDE's workaround of Prettier bug.
    myFixture.configureByText("foo.vue", "<template>\n<selection><div/></selection>\n</template>\n");
    runReformatAction();
    myFixture.checkResult("<template>\n<selection>  <div /></selection>\n</template>\n");
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
    myFixture.testAction(new ReformatWithPrettierAction((new PrettierUtil.ErrorHandler() {
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

  private void configureRunOnReformat(Runnable runnable) {
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(getProject());
    var origRunOnReformat = configuration.getState().runOnReformat;
    var configurationMode = configuration.getState().configurationMode;

    configuration.getState().runOnReformat = true;
    configuration.getState().configurationMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC;

    try {
      String dirName = getTestName(true);
      myFixture.copyDirectoryToProject(dirName, "");
      myFixture.getTempDirFixture().copyAll(getNodePackage().getSystemIndependentPath(), "node_modules/prettier");

      runnable.run();
    }
    finally {
      configuration.getState().runOnReformat = origRunOnReformat;
      configuration.getState().configurationMode = configurationMode;
    }
  }

  private void configureRunOnSave(Runnable runnable) {
    PrettierConfiguration configuration = PrettierConfiguration.getInstance(getProject());
    var runOnSave = configuration.getState().runOnSave;
    var runOnReformat = configuration.getState().runOnReformat;
    var configurationMode = configuration.getState().configurationMode;

    configuration.getState().runOnSave = true;
    configuration.getState().runOnReformat = false;
    configuration.getState().configurationMode = PrettierConfiguration.ConfigurationMode.AUTOMATIC;

    try {
      String dirName = getTestName(true);
      myFixture.copyDirectoryToProject(dirName, "");
      myFixture.getTempDirFixture().copyAll(getNodePackage().getSystemIndependentPath(), "node_modules/prettier");

      runnable.run();
    }
    finally {
      configuration.getState().runOnSave = runOnSave;
      configuration.getState().runOnReformat = runOnReformat;
      configuration.getState().configurationMode = configurationMode;
    }
  }

  private void configureFormatFilesOutsideDependencyScope(boolean enabled, Runnable runnable) {
    var configuration = PrettierConfiguration.getInstance(getProject());
    var runOnSave = configuration.getState().runOnSave;
    var runOnReformat = configuration.getState().runOnReformat;
    var configurationMode = configuration.getState().configurationMode;
    var formatFilesOutsideDependencyScope = configuration.getState().formatFilesOutsideDependencyScope;

    configuration.getState().runOnSave = true;
    configuration.getState().runOnReformat = true;
    configuration.getState().configurationMode = PrettierConfiguration.ConfigurationMode.MANUAL;
    configuration.getState().formatFilesOutsideDependencyScope = enabled;

    try {
      String dirName = getTestName(true);
      myFixture.copyDirectoryToProject(dirName, "");

      runnable.run();
    }
    finally {
      configuration.getState().runOnSave = runOnSave;
      configuration.getState().runOnReformat = runOnReformat;
      configuration.getState().configurationMode = configurationMode;
      configuration.getState().formatFilesOutsideDependencyScope = formatFilesOutsideDependencyScope;
    }
  }

  private void doTestEditorReformat(@NotNull String subDir) {
    String dirName = getTestName(true);
    myFixture.configureFromTempProjectFile(subDir + "toReformat.js");
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT);
    myFixture.checkResultByFile(dirName + "/" + subDir + "toReformat_after.js");
  }

  private void doTestSaveAction(@NotNull String actionId, @NotNull String subDir) {
    String dirName = getTestName(true);
    myFixture.configureFromTempProjectFile(subDir + "toReformat.js");
    myFixture.type(' ');
    myFixture.performEditorAction(actionId);
    ActionsOnSaveTestUtil.waitForActionsOnSaveToFinish(myFixture.getProject());
    myFixture.checkResultByFile(dirName + "/" + subDir + "toReformat_after.js");
  }
}
