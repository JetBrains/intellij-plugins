package com.intellij.lang.javascript.linter.tslint;

import com.intellij.idea.IJIgnore;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@IJIgnore(issue = "Manual only")
@Suite
@SelectClasses({
  TsLintConfigCompletionTest.class,
  TsLintConfigHighlightingTest.class,
  TsLintResolveTest.class,
  TsLintCodeStyleImportBasicTest.class
})
public class TsLintTestSuite {
}