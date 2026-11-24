package com.intellij.dts

import com.intellij.application.options.CodeStyle
import com.intellij.dts.core.lang.DtsFileType
import com.intellij.dts.core.settings.DtsSettings
import com.intellij.dts.core.zephyr.DtsZephyrProvider
import com.intellij.idea.AppMode
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.psi.PsiFile
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestObservation
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.pathInProjectFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.testFramework.junit5.fixture.testNameFixture
import com.intellij.util.asSafely
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.fail
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyToRecursively
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.pathString
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.minutes

private const val DTS_ROOT_DIR = "contrib/dts"

internal val dtsRootPath: Path = AppMode.getDevIdeaProjectDir()?.let { Path(it).resolve(DTS_ROOT_DIR) }
                                 ?: PathManager.getHomeDir().resolve(DTS_ROOT_DIR)

val DTS_TEST_DATA_PATH: Path = dtsRootPath.resolve("tests/testData")

private val defaultTestTimeout = 5.minutes

fun dtsTimeoutRunBlocking(action: suspend CoroutineScope.() -> Unit) {
  timeoutRunBlocking(defaultTestTimeout, action = action)
}

/**
 * Configures a file with [text] and then runs [action] on EDT, in one block.
 *
 * Dumb mode needs the write-intent lock to start. The block holds the lock from the end of the index wait in
 * `configureByText` to the end of [action], so dumb mode cannot start in between (CPP-53251).
 * Thus, [action] must not wait for indexes or for other EDT events.
 *
 * @param fileName the name of the file. When it is null, the name comes from [text], see [DtsTestFixture.configureByText].
 */
suspend fun <T> DtsTestFixture.dtsWithConfiguredFile(text: String, fileName: String? = null, action: () -> T): T =
  withContext(Dispatchers.EDT) {
    if (fileName == null) configureByText(text) else codeInsight.configureByText(fileName, text)
    action()
  }

fun dtsFixture(
  projectFixture: TestFixture<Project>,
  extension: String = "dtsi",
): TestFixture<DtsTestFixture> = testFixture("dtsFixture") {
  val projectRoot = projectFixture.pathInProjectFixture(Path(""))
  projectFixture.moduleFixture(projectRoot, addPathToSourceRoot = true).init()

  val codeInsight = codeInsightFixture(projectFixture, projectRoot).init()
  codeInsight.asSafely<CodeInsightTestFixtureImpl>()?.canChangeDocumentDuringHighlighting(true)

  val methodName = testNameFixture(lowerCaseFirstLetter = false).init()

  // Configure the settings before other threads can read them (CPP-53251).
  val settings = CodeStyle.createTestSettings()
  settings.getIndentOptions(DtsFileType).USE_TAB_CHARACTER = false
  CodeStyle.setTemporarySettings(codeInsight.project, settings)

  val testName = methodName
    .replace(Regex("[#@_.,-]+"), " ")
    .toPascalCase()

  TestObservation.awaitConfiguration(codeInsight.project)
  IndexingTestUtil.suspendUntilIndexesAreReady(codeInsight.project)

  initialized(DtsTestFixture(codeInsight, testName, extension)) {}
}

@OptIn(ExperimentalPathApi::class)
fun dtsProjectFixture(
  tempPathFixture: TestFixture<Path>,
  withZephyr: Boolean = false,
  openAfterCreation: Boolean = false,
): TestFixture<Project> {
  return testFixture("dtsProjectFixture") {
    val tempPath = tempPathFixture.init()
    val zephyrTarget = tempPath.resolve("zephyr")

    val zephyrPath = DTS_TEST_DATA_PATH.resolve("zephyr")
    if (withZephyr) {
      zephyrPath.copyToRecursively(zephyrTarget, followLinks = false)
    }

    val project = projectFixture(tempPathFixture, openAfterCreation = openAfterCreation).init()

    if (withZephyr) {
      val zephyrAbsPath = zephyrTarget.absolutePathString()
      DtsSettings.of(project).update {
        zephyrRoot = zephyrAbsPath
        zephyrBoard = "$zephyrAbsPath/boards/xtensa/esp32"
      }

      DtsZephyrProvider.of(project).awaitInit()
    }

    initialized(project) {}
  }
}

class DtsTestFixture internal constructor(
  val codeInsight: CodeInsightTestFixture,
  val testName: String,
  val extension: String,
) {
  val project: Project get() = codeInsight.project

  val testFile: String
    get() = "$testName.$extension"

  val testFilePath: String
    get() = "${codeInsight.testDataPath}/$testFile"

  fun configureByText(text: String): PsiFile {
    val fileName = "${text.hashCode()}.$extension"
    return codeInsight.configureByText(fileName, text)
  }

  fun addFile(path: String, text: String): PsiFile {
    return codeInsight.addFileToProject(path, text)
  }

  fun getFixture(path: String): String {
    return Files.readString(DTS_TEST_DATA_PATH.resolve(path))
  }

  fun getTestFixture(extension: String): String {
    return Files.readString(Path.of(codeInsight.testDataPath, "$testName.$extension"))
  }

  fun makeRelativeToWorkingDirectory(path: String): String {
    return path.replace(Path(codeInsight.tempDirPath).invariantSeparatorsPathString, "WORKING_DIRECTORY")
  }

  fun compareWithTestFixture(extension: String, actual: String) {
    val path = Path.of(codeInsight.testDataPath, "$testName.$extension")

    if (path.exists()) {
      PlatformTestUtil.assertSameLinesWithFile(path.absolutePathString(), makeRelativeToWorkingDirectory(actual))
    }
    else {
      path.writeText(makeRelativeToWorkingDirectory(actual))
      fail("File ${path.pathString} did not exist. Created new fixture.")
    }
  }
}

private fun String.toPascalCase(): String {
  val capitalize = { word: String -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

  return split(' ').joinToString("", transform = capitalize)
}
