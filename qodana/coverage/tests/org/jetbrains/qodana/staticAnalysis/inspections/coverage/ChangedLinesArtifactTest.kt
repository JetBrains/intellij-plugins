package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.rt.coverage.data.ClassData
import com.intellij.rt.coverage.data.LineData
import com.intellij.rt.coverage.data.ProjectData
import org.jetbrains.qodana.coverage.ChangedLinesArtifactPayload
import org.jetbrains.qodana.coverage.buildChangedLinesPayload
import org.jetbrains.qodana.coverage.readChangedLinesPayload
import org.jetbrains.qodana.coverage.writeChangedLinesArtifact
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Files
import java.nio.file.Path

@RunWith(JUnit4::class)
class ChangedLinesArtifactTest {

  @get:Rule
  val tempFolder: TemporaryFolder = TemporaryFolder()

  @Test
  fun `buildChangedLinesPayload relativizes file URLs against the project path`() {
    val projectPath = tempFolder.newFolder("project").toPath()
    val sub = Files.createDirectories(projectPath.resolve("src"))
    val a = Files.createFile(sub.resolve("A.go"))
    val b = Files.createFile(sub.resolve("B.go"))

    val urlToLines = mapOf(
      a.toUri().toString() to setOf(10, 11),
      b.toUri().toString() to setOf(7),
    )
    val payload = buildChangedLinesPayload(urlToLines, projectPath)

    assertEquals(setOf("src/A.go", "src/B.go"), payload.files.keys)
    assertEquals(setOf(10, 11), payload.files["src/A.go"])
    assertEquals(setOf(7), payload.files["src/B.go"])
  }

  @Test
  fun `buildChangedLinesPayload skips entries outside the project path`() {
    val projectPath = tempFolder.newFolder("project").toPath()
    val outside = tempFolder.newFolder("outside").toPath().resolve("X.go")
    Files.createFile(outside)

    val payload = buildChangedLinesPayload(
      mapOf(outside.toUri().toString() to setOf(1)),
      projectPath,
    )
    assertTrue("paths outside the project must be dropped", payload.files.isEmpty())
  }

  @Test
  fun `buildChangedLinesPayload drops files with empty line sets`() {
    val projectPath = tempFolder.newFolder("project").toPath()
    val a = Files.createFile(projectPath.resolve("A.go"))
    val payload = buildChangedLinesPayload(
      mapOf(a.toUri().toString() to emptySet()),
      projectPath,
    )
    assertTrue(payload.files.isEmpty())
  }

  @Test
  fun `writeChangedLinesArtifact skips empty payloads`() {
    val coveragePath = tempFolder.newFolder("coverage").toPath()
    writeChangedLinesArtifact(coveragePath, ChangedLinesArtifactPayload(files = emptyMap()))
    assertFalse(Files.exists(coveragePath.resolve("changedLines")))
  }

  @Test
  fun `writeChangedLinesArtifact + readChangedLinesPayload round-trip via filesystem`() {
    val coveragePath = tempFolder.newFolder("coverage").toPath()
    val payload = ChangedLinesArtifactPayload(
      files = mapOf("src/foo.kt" to setOf(2, 4, 6))
    )
    writeChangedLinesArtifact(coveragePath, payload)
    val written = coveragePath.resolve("changedLines")
    assertTrue(Files.isRegularFile(written))

    val readBack = readChangedLinesPayload(written)
    assertNotNull(readBack)
    assertEquals(payload, readBack)
  }

  @Test
  fun `readChangedLinesPayload returns null when the file is missing`() {
    val missing = tempFolder.root.toPath().resolve("nope")
    assertNull(readChangedLinesPayload(missing))
  }

  @Test
  fun `filterClassLinesByAllowed keeps only allowed line numbers`() {
    val data = ProjectData()
    val cls = data.getOrCreateClassData("/abs/proj/src/Foo.go")
    cls.setLines(linesArray(mapOf(1 to "()V", 3 to "()V", 5 to "()V", 7 to "()V")))

    val filtered = filterClassLinesByAllowed(data, mapOf("/abs/proj/src/Foo.go" to setOf(3, 5)))
    val filteredCls: ClassData = filtered.getClassData("/abs/proj/src/Foo.go")
    assertNotNull(filteredCls)

    @Suppress("UNCHECKED_CAST")
    val lines = filteredCls.lines as Array<LineData?>
    assertNull("line 1 was not in the allowed set", lines.getOrNull(1))
    assertEquals(3, lines[3]!!.lineNumber)
    assertEquals(5, lines[5]!!.lineNumber)
    assertNull("line 7 was not in the allowed set", lines.getOrNull(7))
  }

  @Test
  fun `filterClassLinesByAllowed drops classes whose path is not in the allowed map`() {
    val data = ProjectData()
    data.getOrCreateClassData("/abs/proj/src/Kept.go").setLines(linesArray(mapOf(1 to "()V")))
    data.getOrCreateClassData("/abs/proj/src/Dropped.go").setLines(linesArray(mapOf(1 to "()V")))

    val filtered = filterClassLinesByAllowed(data, mapOf("/abs/proj/src/Kept.go" to setOf(1)))
    assertNotNull(filtered.getClassData("/abs/proj/src/Kept.go"))
    assertNull(filtered.getClassData("/abs/proj/src/Dropped.go"))
  }

  @Test
  fun `filterClassLinesByAllowed returns an empty result when no classes match`() {
    val data = ProjectData()
    data.getOrCreateClassData("/abs/proj/src/Foo.go").setLines(linesArray(mapOf(1 to "()V")))

    val filtered = filterClassLinesByAllowed(data, mapOf("/abs/proj/src/Bar.go" to setOf(1)))
    assertTrue(filtered.classes.isEmpty())
  }

  private fun linesArray(lineToMethodSig: Map<Int, String>): Array<LineData?> {
    val max = lineToMethodSig.keys.max()
    val arr = arrayOfNulls<LineData>(max + 1)
    for ((n, sig) in lineToMethodSig) arr[n] = LineData(n, sig)
    return arr
  }

  @Suppress("unused")
  private fun Path.touch(): Path = also { Files.createFile(it) }
}
