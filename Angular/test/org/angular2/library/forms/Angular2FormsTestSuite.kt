package org.angular2.library.forms

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
  Angular2FormsCodeCompletionTest::class,
  Angular2FormsHighlightingTest::class,
  Angular2FormsRenameRefactoringTest::class,
  Angular2FormsQuickFixesTest::class,
)
class Angular2FormsTestSuite