package com.intellij.lang.javascript.linter.eslint;

import com.intellij.lang.javascript.linter.eslint.unit.ESLintImportCodeStyleBasicTest;
import com.intellij.lang.javascript.linter.eslint.unit.EslintConfigCompletionTest;
import com.intellij.lang.javascript.linter.eslint.unit.EslintConfigHighlightingTest;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSConfigParsingTest;
import com.intellij.testFramework.TestFrameworkUtil;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Ignore;

@Ignore("Manual only")
public final class EslintTestSuite {
  public static Test suite() {
    final TestSuite testSuite = new TestSuite("ESLint tests");
    testSuite.addTestSuite(EslintConfigHighlightingTest.class);
    testSuite.addTestSuite(EslintConfigCompletionTest.class);
    testSuite.addTestSuite(EslintOnSaveActivationTest.class);
    testSuite.addTestSuite(ESLintImportCodeStyleBasicTest.class);

    testSuite.addTestSuite(StandardJSConfigParsingTest.class);
    return TestFrameworkUtil.flattenSuite(testSuite);
  }
}
