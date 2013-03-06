package com.jetbrains.typoscript;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.IOException;

/**
 * @author lene
 *         Date: 05.03.13
 */
public class TypoScriptTestUtil {
  @NonNls
  private static final String INPUT_DATA_FILE_EXT = "test.ts";
  @NonNls
  private static final String EXPECTED_RESULT_FILE_EXT = "test.expected";

  private TypoScriptTestUtil() {
  }

  @NonNls
  public static String getDataSubPath(@NonNls String theme) {
    return "/contrib/typoScript/testData/typoscript/" + theme;
  }

  public static String getInputDataFilePath(final String dataSubPath, final String testName, final String fileExtension) {
    return PathManager.getHomePath() + "/" + dataSubPath + "/" + testName + "." + fileExtension;
  }

  public static String getExpectedDataFilePath(final String dataSubPath, final String testName) {
    return TypoScriptTestUtil.getInputDataFilePath(dataSubPath, testName, EXPECTED_RESULT_FILE_EXT);
  }

  public static String getInputData(final String dataSubPath, final String testName) {
    final String filePath = TypoScriptTestUtil.getInputDataFilePath(dataSubPath, testName, INPUT_DATA_FILE_EXT);
    try {
      return FileUtil.loadFile(new File(filePath));
    }
    catch (IOException e) {
      System.out.println(filePath);
      throw new RuntimeException(e);
    }
  }
}
