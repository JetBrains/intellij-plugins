package com.intellij.dts

import com.intellij.application.options.CodeStyle
import com.intellij.dts.core.lang.DtsFileType
import com.intellij.dts.core.settings.DtsSettings
import com.intellij.dts.core.util.DtsUtil
import com.intellij.dts.core.zephyr.DtsZephyrProvider
import com.intellij.idea.AppMode
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightModuleFixture
import com.intellij.psi.PsiFile
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.testFramework.junit5.fixture.testNameFixture
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.fail
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
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

fun dtsFixture(projectFixture: TestFixture<Project>, extension: String = "dtsi"): TestFixture<DtsTestFixture> = testFixture("dtsFixture") {
  val codeInsight = codeInsightModuleFixture(projectFixture).init()
  val methodName = testNameFixture(lowerCaseFirstLetter = false).init()

  val settings = CodeStyle.getSettings(codeInsight.project).getIndentOptions(DtsFileType)
  settings.USE_TAB_CHARACTER = false

  val testName = methodName
    .replace(Regex("[#@_.,-]+"), " ")
    .toPascalCase()

  initialized(DtsTestFixture(codeInsight, testName, extension)) {}
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

  suspend fun addZephyr() {
    val zephyr = DTS_TEST_DATA_PATH.resolve("zephyr").absolutePathString()

    ModuleRootModificationUtil.addContentRoot(codeInsight.module, zephyr)

    VfsUtil.markDirtyAndRefresh(false, true, true, DtsUtil.findFile(zephyr)!!)

    DtsSettings.of(project).update {
      zephyrRoot = zephyr
      zephyrBoard = "$zephyr/boards/xtensa/esp32"
    }

    DtsZephyrProvider.of(project).awaitInit()
  }

  fun getFixture(path: String): String {
    return Files.readString(DTS_TEST_DATA_PATH.resolve(path))
  }

  fun getTestFixture(extension: String): String {
    return Files.readString(Path.of(codeInsight.testDataPath, "$testName.$extension"))
  }

  fun makeRelativeToWorkingDirectory(path: String): String {
    return path.replace(DTS_TEST_DATA_PATH.invariantSeparatorsPathString, "WORKING_DIRECTORY")
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
