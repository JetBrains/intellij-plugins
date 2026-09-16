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
      processExecutor = FakeExecutor(DartAnalyzeProcessResult(0, "not JSON", "")),
      executableResolver = availableExecutableResolver,
    ).analyze(tempDirectory)

    assertTrue(result is DartAnalyzeRunResult.Failure)
  }

  @Test
  fun `keeps diagnostics from nonzero Dart exit and analyzes the module from its directory`() = runBlocking {
    val executor = FakeExecutor(DartAnalyzeProcessResult(2, diagnosticsResourceText(), "failure"))

    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)
    val result = runner.analyze(tempDirectory)

    assertEquals(listOf(listOf("dart", "analyze", "--format=json", ".")), executor.commands)
    assertEquals(listOf(tempDirectory), executor.workingDirectories)
    assertEquals(3, (result as DartAnalyzeRunResult.Success).diagnostics.size)
  }

  @Test
  fun `reports an analyze failure without diagnostics`() = runBlocking {
    val executor = FakeExecutor(DartAnalyzeProcessResult(3, "{\"diagnostics\":[]}", "Analyzer failed"))

    val result = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver).analyze(tempDirectory)

    val failure = result as DartAnalyzeRunResult.Failure
    assertTrue(failure.message.contains("dart analyze exited with code 3: Analyzer failed"))
    assertEquals(listOf(listOf("dart", "analyze", "--format=json", ".")), executor.commands)
  }

  @Test
  fun `reports process startup failures`() = runBlocking {
    val result = DartExecutableRunner(
      processExecutor = FailingExecutor(),
      executableResolver = availableExecutableResolver,
    ).analyze(tempDirectory)

    assertTrue(result is DartAnalyzeRunResult.Failure)
  }

  @Test
  fun `rethrows CancellationException during analyze`() {
    val executor = CanceledExecutor(cancelOnCall = 1)
    val runner = DartExecutableRunner(processExecutor = executor, executableResolver = availableExecutableResolver)

    assertThrows(CancellationException::class.java) {
      runBlocking { runner.analyze(tempDirectory) }
    }

    assertEquals(1, executor.callCount)
  }

  private fun diagnosticsResourceText(): String = requireNotNull(javaClass.getResource("/dart-analyze/diagnostics.json")).readText()

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
