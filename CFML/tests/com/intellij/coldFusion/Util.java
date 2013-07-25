package com.intellij.coldFusion;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Lera Nikolaenko
 * Date: 14.11.2008
 */
public class Util {
    @NonNls
    private static final String INPUT_DATA_FILE_EXT = "test.cfml";
    @NonNls
    private static final String EXPECTED_RESULT_FILE_EXT = "test.expected";

    public static String getInputDataFilePath(final String dataSubpath, final String testName) {
        return getInputDataFilePath(dataSubpath, testName, INPUT_DATA_FILE_EXT);
    }

    public static String getExpectedDataFilePath(final String dataSubpath, final String testName) {
        return getInputDataFilePath(dataSubpath, testName, EXPECTED_RESULT_FILE_EXT);
    }

    public static String getInputDataFileName(final String testName) {
        return testName + "." + INPUT_DATA_FILE_EXT;
    }

    public static String getExpectedDataFileName(final String testName) {
        return testName + "." + EXPECTED_RESULT_FILE_EXT;
    }

    public static String getInputData(final String dataSubpath, final String testName) {
        return getFileText(getInputDataFilePath(dataSubpath, testName, INPUT_DATA_FILE_EXT));
    }

    private static String getInputDataFilePath(final String dataSubpath, final String testName, final String fileExtension) {
        return PathManager.getHomePath() + "/" + dataSubpath + "/" + testName + "." + fileExtension;
    }

    private static String getFileText(final String filePath) {
        try {
            return FileUtil.loadFile(new File(filePath));
        } catch (IOException e) {
            System.out.println(filePath);
            throw new RuntimeException(e);
        }
    }
}
