package com.intellij.lang.javascript.frameworks.nextjs

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  NextJsMoveTest::class,
  NextJsResolveTest::class,
  NextJsHighlightTest::class,
  NextJsCompletionTest::class
)
class NextJsTestSuite