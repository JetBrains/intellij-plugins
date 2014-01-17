package com.intellij.aws.cloudformation.tests;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import org.junit.ComparisonFailure;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestUtil {
  public static String getTestDataPath(String relativePath) {
    return System.getProperty("user.dir") + ("/testData" + relativePath).replace('/', File.separatorChar);
  }

  public static void check(String testDataDir, String baseName, String content) throws IOException {
    final File actualFile = new File(testDataDir, baseName + ".actual");
    final File expectedFile = new File(testDataDir, baseName + ".expected");

    if (!expectedFile.isFile()) {
      Files.write(content, actualFile, Charsets.UTF_8);
      fail("No .expected file " + expectedFile);
    } else {
      final String goldContent = FileUtil.loadFile(expectedFile, CharsetToolkit.UTF8, true);
      try {
        assertEquals(goldContent, content);
        actualFile.delete();
      }
      catch (ComparisonFailure e) {
        Files.write(content, actualFile, Charsets.UTF_8);
        throw e;
      }
    }
  }
}
