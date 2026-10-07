package org.jetbrains.qodana.dart

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.LoggedErrorProcessor
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.InvalidPathException
import java.nio.file.Path

@TestApplication
class DartProblemTest {
  @Test
  fun `parses problems`() {
    val problems = parseDartProblems(resourceText("diagnostics.json"))

    assertEquals(3, problems.size)
    val warningProblem = problems[0]
    assertEquals(DartAnalyzeLocation("lib/main.dart", 7, 12, 1, 8, 1, 20), warningProblem.location)
    assertEquals("unused_import", warningProblem.code)
    assertEquals("WARNING", warningProblem.severity)
    assertEquals("Unused import: 'package:example/example.dart'.", warningProblem.message)
    assertEquals("Try removing the import directive.", warningProblem.correction)
  }

  @Test
  fun `parses empty problems`() {
    assertTrue(parseDartProblems(resourceText("no-diagnostics.json")).isEmpty())
  }

  @Test
  fun `throws on missing diagnostics array`() {
    assertThrows(Exception::class.java) { parseDartProblems("{}") }
  }

  @Test
  fun `throws on missing required problem fields`() {
    assertThrows(Exception::class.java) {
      parseDartProblems("""{"diagnostics":[{"severity":"ERROR"}]}""")
    }
  }

  @Test
  fun `parses a problem without a location`() {
    val problems = parseDartProblems(
      """
      {"diagnostics":[{"code":"todo","severity":"INFO","problemMessage":"TODO comment found."}]}
      """.trimIndent(),
    )
    assertEquals(1, problems.size)
    val problem = problems[0]

    assertNull(problem.location)
  }

  @Test
  fun `parses a problem without severity`() {
    val problems = parseDartProblems(
      """
      {"diagnostics":[{"code":"todo","problemMessage":"TODO comment found."}]}
      """.trimIndent(),
    )

    assertEquals("", problems.single().severity)
  }

  @Test
  fun `preserves empty and clamped ranges`() {
    assertEquals(TextRange(3, 3), DartAnalyzeLocation("lib/main.dart", 3, null, 1).rangeIn(10))
    assertEquals(TextRange(3, 3), DartAnalyzeLocation("lib/main.dart", 3, 0, 1).rangeIn(10))
    assertEquals(TextRange(3, 3), DartAnalyzeLocation("lib/main.dart", 3, -1, 1).rangeIn(10))
    assertEquals(TextRange(10, 10), DartAnalyzeLocation("lib/main.dart", 100, 1, 1).rangeIn(10))
  }

  @Test
  fun `uses analyzer lines and columns for LF and CRLF documents`() {
    val location = DartAnalyzeLocation("lib/main.dart", null, null, 2, 3, 2, 10)

    val lfDocument = EditorFactory.getInstance().createDocument("void main() {\n  missing;\n}\n")
    assertEquals("missing", lfDocument.getText(location.rangeIn(lfDocument)))
    val crlfDocument = EditorFactory.getInstance().createDocument(
      "void main() {\r\n  missing;\r\n}\r\n",
      true,
      true,
    )
    assertEquals("missing", crlfDocument.getText(location.rangeIn(crlfDocument)))
  }

  @Test
  fun `formats paths relative to the project root for logging`() {
    val projectPath = Path.of("/project")

    assertEquals("subproject/lib/main.dart", Path.of("/project/subproject/lib/main.dart").toQodanaLogPath(projectPath))
    assertEquals("/tmp/external.dart", Path.of("/tmp/external.dart").toQodanaLogPath(projectPath))
    assertEquals(".", Path.of("/project").toQodanaLogPath(projectPath))
    assertEquals("lib/main.dart", "lib/main.dart".toQodanaLogPath(projectPath))
  }

  @Test
  fun `logs invalid diagnostic paths and preserves the original text`() {
    val path = "lib/invalid\u0000.dart"
    var loggedException: Throwable? = null
    LoggedErrorProcessor.executeWith<Nothing>(object : LoggedErrorProcessor() {
      override fun processWarn(category: String, message: String, t: Throwable?): Boolean {
        loggedException = t
        return false
      }
    }) {
      assertEquals(path, path.toQodanaLogPath(Path.of("/project")))
    }

    val failure = assertInstanceOf(InvalidPathException::class.java, loggedException)
    assertEquals(path, failure.input)
    assertTrue(failure.stackTrace.isNotEmpty())
  }

  private fun resourceText(name: String): String = requireNotNull(javaClass.getResource("/dart-analyze/$name")).readText()
}
