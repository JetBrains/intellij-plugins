package com.intellij.prettierjs;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.linter.JSExternalToolIntegrationTest;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.LineSeparator;
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
    PrettierConfiguration.getInstance(getProject()).update(getNodeInterpreter(), getNodePackage());
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

  private void doReformatFile(final String extension) {
    doReformatFile("toReformat", extension);
  }

  private void doReformatFile(final String fileNamePrefix, final String extension) {
    doReformatFile(fileNamePrefix, extension, null);
  }

  private void doReformatFile(final String fileNamePrefix,
                              final String extension,
                              @Nullable Runnable configureFixture) {
    String dirName = getTestName(true);
    myFixture.copyDirectoryToProject(dirName, "");
    String extensionWithDot = StringUtil.isEmpty(extension) ? "" : "." + extension;
    myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir(fileNamePrefix + extensionWithDot));
    if (configureFixture != null) {
      configureFixture.run();
    }
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
