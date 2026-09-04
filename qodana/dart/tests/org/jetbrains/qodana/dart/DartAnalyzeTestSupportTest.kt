package org.jetbrains.qodana.dart

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class DartAnalyzeTestSupportTest {
  @TempDir
  lateinit var tempDirectory: Path

  @Test
  fun `selects recorded responses by module path`() = runBlocking {
    val firstModule = Files.createDirectory(tempDirectory.resolve("first-module"))
    val secondModule = Files.createDirectory(tempDirectory.resolve("second-module"))
    val executor = RecordedDartAnalyzeProcessExecutor(
      tempDirectory,
      mapOf(
        "first-module" to readDartAnalyzeTestData("no-diagnostics.json"),
        "second-module" to readDartAnalyzeTestData("diagnostics.json"),
      ),
    )

    assertEquals(
      readDartAnalyzeTestData("diagnostics.json").content,
      executor.execute(listOf("dart", "analyze"), secondModule).standardOutput,
    )
    assertEquals(
      readDartAnalyzeTestData("no-diagnostics.json").content,
      executor.execute(listOf("dart", "analyze"), firstModule).standardOutput,
    )
  }
}
