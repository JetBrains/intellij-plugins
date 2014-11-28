package com.jetbrains.lang.dart.ide.fileStructure;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.FileStructureTestBase;
import com.intellij.testFramework.PlatformTestUtil;
import org.junit.Assert;

import java.io.File;

import static com.jetbrains.lang.dart.util.DartTestUtils.BASE_TEST_DATA_PATH;

public class DartFileStructureTest extends FileStructureTestBase {

  @Override
  protected void checkTree() throws Exception {
    final String expected = FileUtil.loadFile(new File(BASE_TEST_DATA_PATH + getBasePath() + "/" + getTreeFileName()), true);
    Assert.assertEquals(expected.trim(), PlatformTestUtil.print(getTree(), true).trim());
  }

  @Override
  protected String getFileExtension() {
    myFixture.setTestDataPath(BASE_TEST_DATA_PATH + getBasePath());
    return "dart";
  }

  @Override
  protected String getBasePath() {
    return "/fileStructure/";
  }

  public void testConstructor() throws Exception {
    checkTree();
  }

  public void testEnums() throws Exception {
    checkTree();
  }

  public void testInsideClass() throws Exception {
    checkTree();
  }

  public void testTopLevel() throws Exception {
    checkTree();
  }
}
