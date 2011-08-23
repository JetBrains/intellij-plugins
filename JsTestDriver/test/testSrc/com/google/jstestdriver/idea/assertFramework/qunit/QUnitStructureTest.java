package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.jstestdriver.idea.JsTestDriverTestUtils;
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
import org.junit.Test;

import java.io.File;

public class QUnitStructureTest extends PsiTestCase {

  @Test
  public void testBasicUsage() throws Exception {
    validateJsFile();
  }

  @Test
  public void testEmptyModule() throws Exception {
    validateJsFile();
  }

  @Test
  public void testMiscModules() throws Exception {
    validateJsFile();
  }

  @Test
  public void testModuleWithLifecycle() throws Exception {
    validateJsFile();
  }

  @Test
  public void testSingleTestOnDefaultModule() throws Exception {
    validateJsFile();
  }

  private void validateJsFile() throws Exception {
    validateJsFile(getTestName(true));
  }

  private void validateJsFile(final String fileNameWithoutExtension) throws Exception {
    validateFile(fileNameWithoutExtension + ".js");
  }

  @Override
  protected String getTestDataPath() {
    return JsTestDriverTestUtils.getTestDataDir().getAbsolutePath() + "/assertFramework/qunit/structure/";
  }

  private void validateFile(@TestDataFile @NonNls String filePath) throws Exception {
    final String fullPath = getTestDataPath() + filePath;
    final String fullRefinedPath = fullPath.replace(File.separatorChar, '/');
    final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(fullRefinedPath);
    Assert.assertNotNull("file " + fullRefinedPath + " not found", vFile);
    String fileText = StringUtil.convertLineSeparators(VfsUtil.loadText(vFile));
    final String fileName = vFile.getName();

    JSFile jsFile = createJsFile(fileText, fileName);
    QUnitFileStructure qUnitFileStructure = buildQUnitFileStructureByJsFile(jsFile);
    MarkedQUnitFileStructure markedQUnitFileStructure = MarkedQUnitStructureUtils.buildMarkedQUnitFileStructureByFileText(
      fileText, jsFile
    );
    validateQUnitFileStructure(markedQUnitFileStructure, qUnitFileStructure);
  }

  private static void validateQUnitFileStructure(MarkedQUnitFileStructure markedQUnitFileStructure, QUnitFileStructure qUnitFileStructure) {
    for (MarkedQUnitModuleStructure markedQUnitModuleStructure : markedQUnitFileStructure.getModules()) {
      QUnitModuleStructure qUnitModuleStructure = qUnitFileStructure.getQUnitModuleByName(markedQUnitModuleStructure.getName());
      if (qUnitModuleStructure != null) {
        validateQUnitModule(markedQUnitModuleStructure, qUnitModuleStructure);
      } else {
        Assert.fail("Can't find automatically collected module with name '" + markedQUnitModuleStructure.getName() + "'");
      }
    }
    if (qUnitFileStructure.getModuleCount() != markedQUnitFileStructure.getModules().size()) {
      Assert.fail("Found marked " + markedQUnitFileStructure.getModules().size() + " modules, but automatically found "
                  + qUnitFileStructure.getModuleCount() + " modules");
    }
  }

  private static void validateQUnitModule(MarkedQUnitModuleStructure markedQUnitModuleStructure, QUnitModuleStructure qUnitModuleStructure) {
    Assert.assertEquals(markedQUnitModuleStructure.getName(), qUnitModuleStructure.getName());
    Assert.assertEquals(markedQUnitModuleStructure.getPsiElement(), qUnitModuleStructure.getJsCallExpression());
    for (MarkedQUnitTestMethodStructure markedQUnitTestStructure : markedQUnitModuleStructure.getTestStructures()) {
      QUnitTestMethodStructure qUnitTestMethodStructure = qUnitModuleStructure.getTestMethodStructureByName(markedQUnitTestStructure.getName());
      if (qUnitTestMethodStructure == null) {
        Assert.fail("Can't find automatically collected test with name '" + markedQUnitTestStructure.getName() + "' inside module '"
                    + qUnitModuleStructure.getName() + "'");
      }
      validateQUnitTestStructure(markedQUnitTestStructure, qUnitTestMethodStructure);
    }
  }

  private static void validateQUnitTestStructure(MarkedQUnitTestMethodStructure markedQUnitTestStructure,
                                                 QUnitTestMethodStructure qUnitTestMethodStructure) {
    Assert.assertEquals(markedQUnitTestStructure.getName(), qUnitTestMethodStructure.getName());
    Assert.assertEquals(markedQUnitTestStructure.getCallExpression(), qUnitTestMethodStructure.getCallExpression());
  }

  private static QUnitFileStructure buildQUnitFileStructureByJsFile(JSFile jsFile) {
    QUnitFileStructureBuilder builder = QUnitFileStructureBuilder.getInstance();
    return builder.buildTestFileStructure(jsFile);
  }

  private JSFile createJsFile(String fileText, String fileName) throws Exception {
    myFile = createFile(myModule, fileName, fileText);
    JSFile jsFile = CastUtils.tryCast(myFile, JSFile.class);
    if (jsFile == null) {
      Assert.fail(JSFile.class + " was expected, but " + (myFile == null ? "null " : myFile.getClass()) + " found.");
    }
    return jsFile;
  }

}
