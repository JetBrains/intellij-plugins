package com.intellij.protobuf.python

import com.intellij.idea.TestFor
import com.intellij.protobuf.python.inspection.PbMessagePositionalArgumentsNotAllowedInspection
import com.intellij.protobuf.python.inspection.quickfix.ConvertToKeywordArgumentsQuickFix

@TestFor(issues = ["PY-92222"], classes = [PbMessagePositionalArgumentsNotAllowedInspection::class, ConvertToKeywordArgumentsQuickFix::class])
class PbPythonPositionalArgumentsInspectionTest : PbPythonTestBase() {

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(PbMessagePositionalArgumentsNotAllowedInspection::class.java)
  }

  fun testHighlighting() = runWithGeneratedPb("all.proto") { context ->
    configureUser("positional_arguments_inspection.py.test", context)
    myFixture.checkHighlighting()
  }

  fun testQuickFixConvertsAllPositionalArguments() = runWithGeneratedPb("all.proto") { context ->
    myFixture.configureByText("test.py", """
      from ${context.importName} import SomeMessage
      SomeMessage(<caret>1, 2, oneof_message=SomeMessage())
    """.trimIndent())

    myFixture.launchAction(QUICK_FIX_TEXT)

    myFixture.checkResult("""
      from ${context.importName} import SomeMessage
      SomeMessage(scalar_types_message=1, some_enum=2, oneof_message=SomeMessage())
    """.trimIndent())
  }

  fun testNoQuickFixWhenFieldIsAlreadyPassed() = runWithGeneratedPb("all.proto") { context ->
    myFixture.configureByText("test.py", """
      from ${context.importName} import SomeMessage
      SomeMessage(<caret>1, scalar_types_message=None)
    """.trimIndent())

    assertEmpty(myFixture.filterAvailableIntentions(QUICK_FIX_TEXT))
  }

  fun testNoQuickFixWhenArgumentsExceedFields() = runWithGeneratedPb("all.proto") { context ->
    myFixture.configureByText("test.py", """
      from ${context.importName} import SomeMessage
      SomeMessage.Nested_Message(<caret>"a", "b")
    """.trimIndent())

    assertEmpty(myFixture.filterAvailableIntentions(QUICK_FIX_TEXT))
  }

  companion object {
    private val QUICK_FIX_TEXT = PbPythonBundle.message("quickfix.convert.to.keyword.arguments")
  }
}
