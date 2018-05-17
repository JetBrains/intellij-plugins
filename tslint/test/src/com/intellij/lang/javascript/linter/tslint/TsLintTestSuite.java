package com.intellij.lang.javascript.linter.tslint;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@SuppressWarnings("NewClassNamingConvention")
@RunWith(Suite.class)
@Suite.SuiteClasses({
  TsLintHighlightingTest.class,
  TsLintConfigCompletionTest.class,
  TsLintConfigHighlightingTest.class,
  TsLintResolveTest.class,
  TsLintCodeStyleImportBasicTest.class,
  TsLintCodeStyleImportIntegrationTest.class
})
public class TsLintTestSuite {
}