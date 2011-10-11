package com.google.jstestdriver.idea;

import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestCase;
import com.intellij.testFramework.TestDataFile;
import junit.framework.Assert;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class AbstractJsPsiTestCase extends PsiTestCase {

  protected void validateJsFile() throws Exception {
    validateJsFile(getTestName(true));
  }

  private void validateJsFile(final String fileNameWithoutExtension) throws Exception {
    validateFile(fileNameWithoutExtension + ".js");
  }

  @NotNull
  private JSFile createJsFile(@NotNull String fileText, @NotNull String fileName) throws Exception {
    myFile = createFile(myModule, fileName, fileText);
    JSFile jsFile = CastUtils.tryCast(myFile, JSFile.class);
    if (jsFile == null) {
      Assert.fail(JSFile.class + " was expected, but " + (myFile == null ? "null " : myFile.getClass()) + " found.");
    }
    return jsFile;
  }

  private void validateFile(@TestDataFile @NonNls String filePath) throws Exception {
    final String fullPath = getTestDataPath() + filePath;
    final String fullRefinedPath = fullPath.replace(File.separatorChar, '/');
    final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(fullRefinedPath);
    Assert.assertNotNull("file " + fullRefinedPath + " not found", vFile);
    String fileText = StringUtil.convertLineSeparators(VfsUtil.loadText(vFile));
    final String fileName = vFile.getName();

    JSFile jsFile = createJsFile(fileText, fileName);
    validateJsFile(jsFile, fileText);
  }

  protected abstract void validateJsFile(JSFile jsFile, String fileText) throws Exception;

}
