package org.jetbrains.qodana.cpp

import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.extended.CppTestContext
import com.intellij.ide.starter.extended.LanguageEngine
import com.intellij.ide.starter.extended.allure.Subsystems
import com.intellij.ide.starter.extended.getCLionContext
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginInstalledState
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.report.ErrorReporter
import com.intellij.ide.starter.runner.IDERunContext
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.io.toNioPathOrNull
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.testNameFixture
import com.jetbrains.qodana.sarif.model.SarifReport
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import utilities.qodana.runQodanaTest
import java.nio.file.Path
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

sealed interface Result {
  data class Succeeded(val problems: List<String>): Result
  data class Failed(val message: String): Result
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
    val sourceDir = PathManager.getHomeDir() / "contrib/qodana/cpp/test-data" / path
    check(sourceDir.exists()) { "Checkout directory $sourceDir does not exist" }

    val workDir = testWorkDir.toNioPath() / path
    sourceDir.copyToRecursively(workDir.createParentDirectories(), overwrite = true, followLinks = true)

    return workDir
  }

  fun analyzeProject(dir: Path): Result {
    System.err.println("Running Qodana C++ in ${dir}")

    val context = getCLionContext(
      testName,
      TestCase(IdeProductProvider.CL, LocalProjectInfo(dir)),
      context = CppTestContext.CLION,
      engine = LanguageEngine.NOVA
    )

    assertEquals(PluginInstalledState.INSTALLED, context.pluginConfigurator.getPluginInstalledState("org.jetbrains.qodana.cpp"))
    context.ide

    var report: SarifReport? = null
    try {
      runQodanaTest(context) { r, reportFromTest ->
        report = reportFromTest
      }
    }
    catch (e: Throwable) {
      val lastLine = e.message?.trim()?.lines()?.lastOrNull() ?: ""
      val message = lastLine.substringAfter("Qodana exited abnormally because: ", "")

      return Result.Failed(message)
    }

    assertNotNull(report)
    assertEquals(1, report!!.runs.size)
    return Result.Succeeded(report!!.runs.first().results.map { it.ruleId })
  }
}