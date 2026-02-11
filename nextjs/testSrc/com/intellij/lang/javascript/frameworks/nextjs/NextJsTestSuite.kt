package com.intellij.lang.javascript.frameworks.nextjs

import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.Suite

@Ignore("Manual only")
@RunWith(Suite::class)
@Suite.SuiteClasses(
  NextJsMoveTest::class,
  NextJsResolveTest::class,
  NextJsHighlightTest::class,
  NextJsCompletionTest::class
)
class NextJsTestSuite