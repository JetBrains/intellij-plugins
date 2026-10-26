package org.jetbrains.qodana.protocol

import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.io.write
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.relativeTo

@TestApplication
class OpenInIdeFileRegionLocatorTest {
  @Test
  fun `test find region`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 2,
      offsetInLine = 4,
      regionLength = 18,
      regionValidator = regionValidator("System.out.println"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isTrue
  }

  @Test
  fun `test find multiline region`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 2,
      offsetInLine = 22,
      regionLength = 21,
      regionValidator = regionValidator("(\"Hello world!\");\n  }"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isTrue
  }

  @Test
  fun `test first file line region`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 0,
      offsetInLine = 0,
      regionLength = 15,
      regionValidator = regionValidator("final class A {"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isTrue
  }

  @Test
  fun `test last file line region`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 4,
      offsetInLine = 0,
      regionLength = 1,
      regionValidator = regionValidator("}"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isTrue
  }

  @Test
  fun `test no region wrong relative path`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = Path("not_existing").resolve(file.relativeTo(root)).toString(),
      regionStartLine = 2,
      offsetInLine = 4,
      regionLength = 18,
      regionValidator = regionValidator("System.out.println"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isFalse
  }

  @Test
  fun `test no region wrong line`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 3,
      offsetInLine = 4,
      regionLength = 18,
      regionValidator = regionValidator("System.out.println"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isFalse
  }

  @Test
  fun `test no region wrong offsetInLine`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 2,
      offsetInLine = 3,
      regionLength = 18,
      regionValidator = regionValidator("System.out.println"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isFalse
  }

  @Test
  fun `test no region wrong regionLength`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 2,
      offsetInLine = 4,
      regionLength = 17,
      regionValidator = regionValidator("System.out.println"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isFalse
  }

  @Test
  fun `test no region wrong region`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 2,
      offsetInLine = 4,
      regionLength = 18,
      regionValidator = regionValidator("wrong region"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isFalse
  }

  @Test
  fun `test no region too big start line`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 100,
      offsetInLine = 4,
      regionLength = 18,
      regionValidator = regionValidator("System.out.println"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isFalse
  }

  @Test
  fun `test no region too big offset in line`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 2,
      offsetInLine = 400,
      regionLength = 18,
      regionValidator = regionValidator("System.out.println"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isFalse
  }

  @Test
  fun `test no region too big regionLength`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 2,
      offsetInLine = 4,
      regionLength = 200,
      regionValidator = regionValidator("System.out.println"),
    )
    assertThat(locator.regionExistsInDirectory(root)).isFalse
  }

  @Test
  fun `test always true region validator`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 2,
      offsetInLine = 4,
      regionLength = 10,
      regionValidator = { true },
    )
    assertThat(locator.regionExistsInDirectory(root)).isTrue
  }

  @Test
  fun `test zero region`(): Unit = timeoutRunBlocking {
    val file = createJavaTempFile("A.java")
    val root = file.parent.parent
    val locator = OpenInIdeFileRegionLocator(
      fileRelativePath = file.relativeTo(root).toString(),
      regionStartLine = 0,
      offsetInLine = 0,
      regionLength = 0,
      regionValidator = { true },
    )
    assertThat(locator.regionExistsInDirectory(root)).isTrue
  }
}

private fun createJavaTempFile(name: String): Path {
  val file = createTempFile(name)
  @Language("JAVA")
  val content = """
final class A {
  public static void main(String[] args) {
    System.out.println("Hello world!");
  }
}
    """.trimIndent()
  file.write(content)
  return file
}

private fun regionValidator(expectedRegion: String): (String) -> Boolean {
  return { region: String -> region == expectedRegion }
}