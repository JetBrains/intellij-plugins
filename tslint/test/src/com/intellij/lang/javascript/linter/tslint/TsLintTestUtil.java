package com.intellij.lang.javascript.linter.tslint;

import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;

import java.io.File;

public class TsLintTestUtil {
    public static final String BASE_TEST_DATA_PATH = findTestDataPath();

    private static final String TS_LINT_DATA_PATH = "/tslint/test/data";

    private static String findTestDataPath() {
        String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
        return homePath + getRelativeTestDataPath(homePath);
    }

    private static String getRelativeTestDataPath(String homePath) {
        if (new File(homePath + "/contrib/.gitignore").isFile()) {
            return "/contrib" + TS_LINT_DATA_PATH;
        }
        return TS_LINT_DATA_PATH;
    }
}
