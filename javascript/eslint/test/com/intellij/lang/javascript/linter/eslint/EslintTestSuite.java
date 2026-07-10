package com.intellij.lang.javascript.linter.eslint;

import com.intellij.lang.javascript.linter.eslint.unit.ESLintImportCodeStyleBasicTest;
import com.intellij.lang.javascript.linter.eslint.unit.EslintConfigCompletionTest;
import com.intellij.lang.javascript.linter.eslint.unit.EslintConfigHighlightingTest;
import com.intellij.lang.javascript.linter.eslint.next.EslintHighlightingLatestTest;
import com.intellij.lang.javascript.linter.eslint.stable.EslintFallbackToLegacyConfigV9Test;
import com.intellij.lang.javascript.linter.eslint.stable.EslintFixV10Test;
import com.intellij.lang.javascript.linter.eslint.stable.EslintHighlightingV10Test;
import com.intellij.lang.javascript.linter.eslint.stable.EslintHighlightingV8LegacyTest;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSConfigParsingTest;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSFixTest;
import com.intellij.lang.javascript.linter.eslint.standardjs.StandardJSHighlightingTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public final class EslintTestSuite {
  public static Test suite() {
    final TestSuite testSuite = new TestSuite("ESLint tests");
    testSuite.addTestSuite(EslintConfigHighlightingTest.class);
    testSuite.addTestSuite(EslintConfigCompletionTest.class);
    testSuite.addTestSuite(EslintYarnPnpTest.class);
    testSuite.addTestSuite(EslintHighlightingV10Test.class);
    testSuite.addTestSuite(EslintHighlightingV8LegacyTest.class);
    testSuite.addTestSuite(EslintFallbackToLegacyConfigV9Test.class);
    testSuite.addTestSuite(EslintFixV10Test.class);
    // next — kept in the suite; a failure here is triaged by the next.* package name (an upstream
    // ESLint release changed something), not treated as a regression in the change under test.
    testSuite.addTestSuite(EslintHighlightingLatestTest.class);
    testSuite.addTestSuite(ESLintImportCodeStyleBasicTest.class);
    testSuite.addTestSuite(ESLintImportCodeStyleIntegrationTest.class);

    testSuite.addTestSuite(StandardJSFixTest.class);
    testSuite.addTestSuite(StandardJSConfigParsingTest.class);
    testSuite.addTestSuite(StandardJSHighlightingTest.class);
    return testSuite;
  }
}
