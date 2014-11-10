package com.jetbrains.lang.dart.ide.fileStructure;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.FileStructureTestBase;
import com.intellij.testFramework.IdeaTestCase;
import com.intellij.testFramework.PlatformTestUtil;
import org.junit.Assert;

import java.io.File;

import static com.jetbrains.lang.dart.util.DartTestUtils.BASE_TEST_DATA_PATH;

public abstract class DartFileStructureTestCase extends FileStructureTestBase {

  protected DartFileStructureTestCase() {
    IdeaTestCase.initPlatformPrefix();
  }

  protected abstract String getTestDataFolderName();

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
    return "/fileStructure/" + getTestDataFolderName();
  }
}
