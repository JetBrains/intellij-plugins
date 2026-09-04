package org.jetbrains.qodana.dart

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

@TestApplication
class DartAnalyzeDiagnosticTest {
  @Test
  fun `parses diagnostics and converts them to external tool issues`() {
    val diagnostics = parseDartAnalyzeDiagnostics(resourceText("diagnostics.json"))

    assertEquals(3, diagnostics.size)
    val warningDiagnostic = diagnostics[0]
    assertEquals(DartAnalyzeLocation("lib/main.dart", 7, 12, 1, 8, 1, 20), warningDiagnostic.location)
    val warning = warningDiagnostic.toExternalToolIssue(Path.of("/project"))
    assertEquals(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, warning.inspectionId)
    assertEquals(HighlightDisplayLevel.WARNING, warning.severity)
    assertEquals("[unused_import] Unused import: 'package:example/example.dart'.\nTry removing the import directive.", warning.description)
    assertEquals("file:///project/lib/main.dart", warning.file)
    assertEquals(1, warning.line)
    assertEquals(7, warning.offset)
    assertEquals(12, warning.length)
    assertEquals(HighlightDisplayLevel.ERROR, diagnostics[1].toExternalToolIssue(Path.of("/project")).severity)
    assertEquals(HighlightDisplayLevel.WEAK_WARNING, diagnostics[2].toExternalToolIssue(Path.of("/project")).severity)
  }

  @Test
  fun `parses empty diagnostics`() {
    assertTrue(parseDartAnalyzeDiagnostics(resourceText("no-diagnostics.json")).isEmpty())
  }

  @Test
  fun `throws on missing diagnostics array`() {
    assertThrows(Exception::class.java) { parseDartAnalyzeDiagnostics("{}") }
  }

  @Test
  fun `throws on missing required diagnostic fields`() {
    assertThrows(Exception::class.java) {
      parseDartAnalyzeDiagnostics("""{"diagnostics":[{"severity":"ERROR"}]}""")
    }
  }

  @Test
  fun `converts a diagnostic without a location`() {
    val diagnostics = parseDartAnalyzeDiagnostics(
      """
      {"diagnostics":[{"code":"todo","severity":"INFO","problemMessage":"TODO comment found."}]}
      """.trimIndent(),
    )
    assertEquals(1, diagnostics.size)
    val diagnostic = diagnostics[0]

    assertNull(diagnostic.location)
    val issue = diagnostic.toExternalToolIssue(Path.of("/project"))
    assertNull(issue.file)
    assertNull(issue.line)
    assertNull(issue.offset)
    assertNull(issue.length)
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
  fun `converts absolute and relative paths to file URLs`() {
    val projectPath = Path.of("/project")

    assertEquals("file:///project/lib/main.dart", "/project/lib/main.dart".toFileUrl(projectPath))
    assertEquals("file:///project/lib/main.dart", "lib/main.dart".toFileUrl(projectPath))
    assertNull("\u0000invalid".toFileUrl(projectPath))
  }

  private fun resourceText(name: String): String = requireNotNull(javaClass.getResource("/dart-analyze/$name")).readText()
}
