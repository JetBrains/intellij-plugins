// Copyright 2000-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.jshint;

import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;

import java.io.File;

public final class JSHintTestUtil {
    public static final String BASE_TEST_DATA_PATH = findTestDataPath();

    private static final String JS_HINT_DATA_PATH = "/javascript/jshint/test/data";

    private static String findTestDataPath() {
        String homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy();
        return homePath + getRelativeTestDataPath(homePath);
    }

    private static String getRelativeTestDataPath(String homePath) {
        if (new File(homePath + "/contrib/.gitignore").isFile()) {
            return "/contrib" + JS_HINT_DATA_PATH;
        }
        return JS_HINT_DATA_PATH;
    }
}