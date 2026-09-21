package com.intellij.protobuf.python

import com.intellij.idea.TestFor
import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.impl.computeDocumentationBlocking
import com.intellij.platform.backend.documentation.impl.resolveLinkToTarget
import com.intellij.protobuf.python.documentation.PbPythonDocumentationLinkHandler
import com.intellij.protobuf.python.documentation.PbPythonDocumentationTarget
import com.intellij.protobuf.python.documentation.PbPythonDocumentationTargetProvider

@TestFor(
  issues = ["PY-92223"],
  classes = [PbPythonDocumentationTargetProvider::class, PbPythonDocumentationTarget::class, PbPythonDocumentationLinkHandler::class]
)
class PbPythonDocumentationTest : PbPythonTestBase() {

  fun testMessageCall() = runWithGeneratedPb("all.proto") { context ->
    val html = documentationAt(context, "SomeMessage(<caret>)")

    assertContainsAll(html, "message", "SomeMessage", "This text is an example of a multi-line comment.", "all.proto")
  }

  fun testKeywordArgument() = runWithGeneratedPb("all.proto") { context ->
    val html = documentationAt(context, "SomeMessage(some_<caret>enum=None)")

    assertContainsAll(html, "some_enum", ">SomeEnum</a>")
    assertFalse(html, "message" in html)
  }

  fun testFieldAccess() = runWithGeneratedPb("all.proto") { context ->
    val html = documentationAt(context, "SomeMessage().scalar_<caret>types_message")

    assertContainsAll(html, "scalar_types_message", ">ScalarTypesMessage</a>")
  }

  fun testFieldAssignmentShowsPythonAndProtoDocumentation() = runWithGeneratedPb("all.proto") { context ->
    val html = documentationAt(context, """
      msg = SomeMessage()
      msg.some_<caret>enum = 1
    """.trimIndent())

    assertContainsAll(html, "<hr>", "some_enum", ">SomeEnum</a>")
  }

  fun testNestedEnumComment() = runWithGeneratedPb("all.proto") { context ->
    val html = documentationAt(context, "SomeMessage.Nested_<caret>Enum")

    assertContainsAll(html, "enum", "NESTED_ENUM_VALUE_1", "This is an example of a single-line comment")
  }

  fun testOneofFieldsRenderedOnce() = runWithGeneratedPb("all.proto") { context ->
    val html = documentationAt(context, "SomeMessage(<caret>)")

    assertContainsAll(html, "test_oneof")
    assertEquals(html, 1, Regex("oneof_string").findAll(html).count())
  }

  fun testTypeLinkResolvesToReferencedSymbol() = runWithGeneratedPb("all.proto") { context ->
    val target = documentationTargetAt(context, "SomeMessage(some_<caret>enum=None)")
    val html = checkNotNull(computeDocumentationBlocking(target.createPointer())).html
    val link = checkNotNull(Regex("""href=['"](psi_element://[^'"]+)['"]>SomeEnum<""").find(html)) { html }.groupValues[1]

    val linkedTarget = runBlockingMaybeCancellable { resolveLinkToTarget(target.createPointer(), link) }
    val linkedHtml = checkNotNull(computeDocumentationBlocking(checkNotNull(linkedTarget) { link })).html

    assertContainsAll(linkedHtml, "enum", "ENUM_VALUE_ALIAS")
  }

  private fun documentationAt(context: GeneratedProtoContext, code: String): String {
    val target = documentationTargetAt(context, code)
    return checkNotNull(computeDocumentationBlocking(target.createPointer())) { "No documentation for $target" }.html
  }

  private fun documentationTargetAt(context: GeneratedProtoContext, code: String): DocumentationTarget {
    myFixture.configureByText("test.py", "from ${context.importName} import SomeMessage\n$code")
    val targets = runBlockingMaybeCancellable {
      readAction {
        IdeDocumentationTargetProvider.getInstance(myFixture.project).documentationTargets(myFixture.editor, myFixture.file, myFixture.caretOffset)
      }
    }
    return checkNotNull(targets.firstOrNull { it is PbPythonDocumentationTarget }) { "No Protobuf documentation target in $targets" }
  }

  private fun assertContainsAll(html: String, vararg expected: String) {
    for (text in expected) {
      assertTrue("Expected '$text' in:\n$html", text in html)
    }
  }
}
