package org.jetbrains.qodana.dart

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Path

class DartAnalyzeRunnerTest {
  @TempDir
  lateinit var tempDirectory: Path

  @Test
  fun `reports malformed JSON output`() = runBlocking {
    val result = DartExecutableRunner(
      processExecutor = FakeExecutor(DartAnalyzeProcessResult(0, "", ""), DartAnalyzeProcessResult(0, "not JSON", "")),
      executableResolver = availableExecutableResolver,
    ).run(tempDirectory)

    assertTrue(result is DartAnalyzeRunResult.Failure)
  }

  @Test
  fun `keeps diagnostics from nonzero Dart exit and analyzes the module from its directory`() = runBlocking {
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(0, "", ""),
      DartAnalyzeProcessResult(2, resourceText("diagnostics.json"), "failure"),
    )

    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)
    assertEquals(DartPreparationResult.Success, runner.prepare(tempDirectory))
    val result = runner.analyze(tempDirectory)

    assertEquals(
      listOf(
        listOf("dart", "pub", "get"),
        listOf("dart", "analyze", "--format=json", "."),
      ),
      executor.commands,
    )
    assertEquals(listOf(tempDirectory, tempDirectory), executor.workingDirectories)
    assertEquals(3, (result as DartAnalyzeRunResult.Success).diagnostics.size)
  }

  @Test
  fun `uses Flutter executable commands for Flutter modules`() = runBlocking {
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(0, "", ""),
      DartAnalyzeProcessResult(0, resourceText("no-diagnostics.json"), ""),
    )

    FlutterExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).run(tempDirectory)

    assertEquals(
      listOf(
        listOf("flutter", "pub", "get"),
        listOf("dart", "analyze", "--format=json", "."),
      ),
      executor.commands,
    )
    assertEquals(listOf(tempDirectory, tempDirectory), executor.workingDirectories)
  }

  @Test
  fun `reports a pub get failure without analyzing`() = runBlocking {
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(1, "", "The lower bound of sdk must be 2.12.0 or higher to enable null safety."),
    )

    val result = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).run(tempDirectory)

    val failure = result as DartAnalyzeRunResult.Failure
    assertEquals(DART_PROJECT_SETUP_SHORT_NAME, failure.inspectionId)
    assertTrue(failure.message.contains("must be 2.12.0 or higher to enable null safety"))
    assertTrue(failure.message.contains("exited with code 1"))
    assertEquals(listOf(listOf("dart", "pub", "get")), executor.commands)
  }

  @Test
  fun `reports an analyze failure without diagnostics`() = runBlocking {
    val executor = FakeExecutor(
      DartAnalyzeProcessResult(0, "", ""),
      DartAnalyzeProcessResult(3, "{\"diagnostics\":[]}", "Analyzer failed"),
    )

    val result = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).run(tempDirectory)

    val failure = result as DartAnalyzeRunResult.Failure
    assertTrue(failure.message.contains("dart analyze exited with code 3: Analyzer failed"))
    assertEquals(
      listOf(
        listOf("dart", "pub", "get"),
        listOf("dart", "analyze", "--format=json", "."),
      ),
      executor.commands,
    )
  }

  @Test
  fun `reports process startup failures`() = runBlocking {
    val result = DartExecutableRunner(
      processExecutor = FailingExecutor(),
      executableResolver = availableExecutableResolver,
    ).run(tempDirectory)

    assertTrue(result is DartAnalyzeRunResult.Failure)
  }

  @Test
  fun `rethrows CancellationException during pub get`() {
    val executor = CanceledExecutor(cancelOnCall = 1)
    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)

    assertThrows(CancellationException::class.java) {
      runBlocking { runner.prepare(tempDirectory) }
    }

    assertEquals(1, executor.callCount)
  }

  @Test
  fun `rethrows CancellationException during analyze`() {
    val executor = CanceledExecutor(cancelOnCall = 2)
    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)

    assertEquals(DartPreparationResult.Success, runBlocking { runner.prepare(tempDirectory) })
    assertThrows(CancellationException::class.java) {
      runBlocking { runner.analyze(tempDirectory) }
    }

    assertEquals(2, executor.callCount)
  }

  private fun resourceText(name: String): String = requireNotNull(javaClass.getResource("/dart-analyze/$name")).readText()

  private val availableExecutableResolver = DartExecutableResolver { executableName, _ ->
    DartExecutable.Available(Path.of(executableName))
  }

  private class FakeExecutor(vararg results: DartAnalyzeProcessResult) : DartAnalyzeProcessExecutor {
    private val results = ArrayDeque(results.asList())
    val commands = ArrayList<List<String>>()
    val workingDirectories = ArrayList<Path>()

    override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
      commands.add(command)
      workingDirectories.add(workingDirectory)
      return results.removeFirst()
    }
  }

  private class FailingExecutor : DartAnalyzeProcessExecutor {
    override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
      throw IOException("Unable to start Dart")
    }
  }

  private class CanceledExecutor(private val cancelOnCall: Int) : DartAnalyzeProcessExecutor {
    var callCount = 0

    override suspend fun execute(command: List<String>, workingDirectory: Path): DartAnalyzeProcessResult {
      callCount++
      if (callCount >= cancelOnCall) throw CancellationException()
      return DartAnalyzeProcessResult(0, "", "")
    }
  }
}
