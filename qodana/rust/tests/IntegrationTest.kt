package org.intellij.qodana.rust

import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.extended.allure.Subsystems
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.report.ErrorReporter
import com.intellij.ide.starter.runner.IDERunContext
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.rustrover.integration.testFramework.createRustRoverTestContext
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.testNameFixture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import utilities.qodana.qodanaReport
import utilities.qodana.runQodana
import java.nio.file.Path
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import com.jetbrains.qodana.sarif.model.Result as SarifResult


class Result(
  val ok: Boolean,
  val results: List<SarifResult>,
  val log: String = "",
) {
  companion object {
    fun Success(results: List<SarifResult>) = Result(true, results)
    fun Failure(log: String) = Result(false, emptyList(), log)

    private val ERROR_REGEX = "^Qodana exited abnormally because: (.*)$".toRegex(RegexOption.MULTILINE)
  }

  /** List of rule IDs for backward compatibility */
  val problems: List<String>
    get() = results.mapNotNull { it.ruleId }

  val failureMessage by lazy {
    ERROR_REGEX.find(log)?.groupValues[1]
  }

  /**
   * Parsed location from a location string in format: `filename[:line[:column]]`
   * - `filename` is relative to the project root
   * - `line` and `column` are optional (1-based)
   */
  private data class Location(val filename: String, val line: Int?, val column: Int?) {
    companion object {
      fun parse(location: String): Location {
        val parts = location.split(":")
        return Location(
          filename = parts[0],
          line = parts.getOrNull(1)?.toIntOrNull(),
          column = parts.getOrNull(2)?.toIntOrNull()
        )
      }
    }

    fun matches(physicalLocation: com.jetbrains.qodana.sarif.model.PhysicalLocation?): Boolean {
      if (physicalLocation == null) return false
      val uri = physicalLocation.artifactLocation?.uri ?: return false
      // Check if the URI ends with the filename (handles relative paths)
      if (!uri.endsWith(filename)) return false
      // If line is specified, check it
      if (line != null && physicalLocation.region?.startLine != line) return false
      // If column is specified, check it
      if (column != null && physicalLocation.region?.startColumn != column) return false
      return true
    }
  }

  /**
   * Find all issues with the given rule ID, optionally filtered by location.
   * @param location Optional location string in format: `filename[:line[:column]]`
   */
  fun findIssues(ruleId: String, location: String? = null): List<SarifResult> {
    val loc = location?.let { Location.parse(it) }
    return results.filter { result ->
      result.ruleId == ruleId &&
      (loc == null || result.locations?.any { loc.matches(it.physicalLocation) } == true)
    }
  }

  /**
   * Find the first issue with the given rule ID, optionally filtered by location.
   * @param location Optional location string in format: `filename[:line[:column]]`
   * @return The first matching issue, or null if none found
   */
  fun findIssue(ruleId: String, location: String? = null): SarifResult? =
    findIssues(ruleId, location).firstOrNull()
}

@Subsystems.Startup
@TestApplication
abstract class IntegrationTest {
  private val testName by testNameFixture()
  private val tempDirTestFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture()

  @BeforeEach
  fun setUp() {
    di = DI {
      extend(di)
      bindSingleton<ErrorReporter>(overrides = true) {
        object : ErrorReporter {
          override fun reportErrorsAsFailedTests(runContext: IDERunContext) {}
        }
      }
    }
    tempDirTestFixture.setUp()
  }

  @AfterEach
  fun tearDown() {
    tempDirTestFixture.tearDown()
  }

  /**
   * Given a path relative to the module's resources directory, copies it to a temporary directory and returns its path.
   */
  fun checkout(path: String) = checkout(path.toNioPathOrNull()!!)
  fun checkout(path: Path): Path {
    val testWorkDir = tempDirTestFixture.findOrCreateDir(testName)
    val sourceDir = PathManager.getHomeDir() / "contrib/qodana/rust/test-data" / path
    check(sourceDir.exists()) { "Checkout directory $sourceDir does not exist" }

    val workDir = testWorkDir.toNioPath() / path
    sourceDir.copyToRecursively(workDir.createParentDirectories(), overwrite = true, followLinks = true)

    return workDir
  }

  fun analyzeProject(dir: Path): Result {
    val context = createRustRoverTestContext(
      testName,
      TestCase(IdeProductProvider.RR, LocalProjectInfo(dir)),
    )

    val result = context.runQodana(testName, dir)
    val report = result.qodanaReport

    assertNotNull(report)
    assertEquals(1, report.runs.size)
    return Result.Success(report.runs.first().results)
  }
}
