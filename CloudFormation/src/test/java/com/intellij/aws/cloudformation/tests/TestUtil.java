package com.intellij.aws.cloudformation.tests;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.util.Processor;

import java.io.File;

public class TestUtil {
  public static String getTestDataPath(String relativePath) {
    return "../../../testData/" + relativePath;
  }

  public static File getTestDataRoot() {
    return new File(System.getProperty("user.dir"), "testData");
  }

  public static void refreshVfs() {
    final LocalFileSystem lfs = LocalFileSystem.getInstance();

    FileUtil.visitFiles(getTestDataRoot(), new Processor<File>() {
      @Override
      public boolean process(File file) {
        lfs.refreshAndFindFileByIoFile(file);
        return true;
      }
    });
  }
}
