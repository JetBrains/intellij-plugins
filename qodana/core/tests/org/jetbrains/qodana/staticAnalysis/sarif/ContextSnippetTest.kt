package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.builders.ModuleFixtureBuilder
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ContextSnippetTest : CodeInsightFixtureTestCase<ModuleFixtureBuilder<*>>() {
  @Test
  fun `full text snippet`() {
    doTest("""
      class C {
      <selection>  private String str;
        private void fun() {
          int <caret>a = 0;
        }
      }</selection>
    """.trimIndent())
  }

  @Test
  fun `text snippet with 4 lines`() {
    doTest("""
      <selection>class C {
        <caret>private String str;
        private void fun() {
          int a = 0;</selection>
        }
      }
    """.trimIndent())
  }

  @Test
  fun `text snippet at start`() {
    doTest("""
      <selection><caret>class C {
        private String str;
        private void fun() {</selection>
          int a = 0;
        }
      }
    """.trimIndent())
  }

  @Test
  fun `text snippet at end`() {
    doTest("""
      class C {
        private String str;
        private void fun() {
      <selection>    int a = 0;
        }
      <caret>}</selection>
    """.trimIndent())
  }

  @Test
  fun `text snippet at last symbol`() {
    doTest("""
      class C {
        private String str;
        private void fun() {
      <selection>    int a = 0;
        }
      }<caret></selection>
    """.trimIndent())
  }

  @Test
  fun `long line`() {
    doTest("""
      <selection>abcd<caret>${"e".repeat(MAX_CONTEXT_CHARS_LENGTH - 4)}</selection>fgh
    """.trimIndent())
  }

  @Test
  fun `long line 2`() {
    doTest("""
      <selection>B
      abcd<caret>${"e".repeat(MAX_CONTEXT_CHARS_LENGTH - 6)}</selection>fgh
      }
    """.trimIndent())
  }

  @Test
  fun `import statement`() {
    doTest("""
      <selection>package hello;

      <caret>import java.util.stream.Stream;
      
      </selection>
      public class Hello {
      
          public static void main(String[] args) {
              if (true) {
      
              }
          }
      }

    """.trimIndent(), "import java.util.stream.Stream;".length)
  }

  private fun doTest(text: String, len: Int = 0) {
    val file = myFixture.configureByText("A.java", text)
    val logicalPosition = myFixture.editor.caretModel.logicalPosition
    val problem = CommonDescriptor("", logicalPosition.line + 1, logicalPosition.column, len, null, null)
    val region = getContextRegion(problem, VfsUtil.loadText(file.virtualFile))
    val selectionModel = myFixture.editor.selectionModel
    assertEquals(selectionModel.selectedText, region?.snippet?.text)
    assertEquals(selectionModel.selectionStart, region?.charOffset)
    val selectionStart = editor.offsetToLogicalPosition(selectionModel.selectionStart)
    assertEquals(selectionStart.column + 1, region?.startColumn)
    assertEquals(selectionStart.line + 1, region?.startLine)
  }
}
