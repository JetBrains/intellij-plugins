package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.random.Random

class FileTextTest {
  @Test
  fun `answers as StringUtil for every line ending`() {
    val random = Random(42)
    val alphabet = charArrayOf('a', 'b', ' ', '\n', '\r')
    repeat(500) {
      val text = String(CharArray(random.nextInt(0, 60)) { alphabet[random.nextInt(alphabet.size)] })
      val fileText = FileText(text)
      for (offset in -1..text.length + 1) {
        assertEquals(StringUtil.offsetToLineColumn(text, offset), fileText.offsetToLineColumn(offset), "offset $offset of '${StringUtil.escapeLineBreak(text)}'")
      }
      for (line in 0..text.length + 1) {
        for (col in 0..3) {
          assertEquals(StringUtil.lineColToOffset(text, line, col), fileText.lineColToOffset(line, col), "line $line, col $col of '${StringUtil.escapeLineBreak(text)}'")
        }
      }
    }
  }
}

@TestApplication
class QodanaFileTextTest {
  @Test
  fun `keeps the line separators of the file when its document is loaded`(@TempDir dir: Path) {
    val path = dir.resolve("a.txt")
    Files.writeString(path, "first\r\nsecond\r\n")
    val file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)!!
    val document = runReadAction { FileDocumentManager.getInstance().getDocument(file) }
    assertEquals("first\nsecond\n", document?.text)

    assertEquals("first\r\nsecond\r\n", qodanaFileText(file)?.text)
  }

  @Test
  fun `keeps the line separators of the file without a document`(@TempDir dir: Path) {
    val path = dir.resolve("a.txt")
    Files.writeString(path, "first\r\nsecond\r\n")
    val file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path)!!
    assertEquals(null, FileDocumentManager.getInstance().getCachedDocument(file))

    assertEquals("first\r\nsecond\r\n", qodanaFileText(file)?.text)
  }

  @Test
  fun `reloads the text after the file changes`() {
    val file = LightVirtualFile("a.txt", "old")
    assertEquals("old", qodanaFileText(file)?.text)

    file.setContent(null, "new", true)
    assertEquals("new", qodanaFileText(file)?.text)
  }
}
