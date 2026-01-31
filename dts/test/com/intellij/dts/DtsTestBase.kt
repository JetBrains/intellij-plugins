package com.intellij.dts

import com.intellij.application.options.CodeStyle
import com.intellij.dts.lang.DtsFileType
import com.intellij.dts.settings.DtsSettings
import com.intellij.dts.util.DtsUtil
import com.intellij.dts.zephyr.DtsZephyrProvider
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.psi.PsiFile
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.pathString
import kotlin.io.path.writeText

val DTS_TEST_ROOT_PATH: String = PathManager.getHomePath() + "/contrib/dts"
val DTS_TEST_DATA_PATH: String = PathManager.getHomePath() + "/contrib/dts/testData"

abstract class DtsTestBase : BasePlatformTestCase() {
  override fun setUp() {
    super.setUp()

    val settings = CodeStyle.getSettings(project).getIndentOptions(DtsFileType)
    settings.USE_TAB_CHARACTER = false
  }

  protected val testName: String
    get() = getTestName(false)
      .replace(Regex("[#@_.,-]+"), " ")
      .toPascalCase()

  protected val testFile: String
    get() = "$testName.${getTestFileExtension()}"

  protected val testFilePath: String
    get() = "$testDataPath/$testName.${getTestFileExtension()}"

  protected open fun getTestFileExtension(): String = "dtsi"

  override fun getTestDataPath(): String = "$DTS_TEST_DATA_PATH/$basePath"

  protected fun configureByText(text: String): PsiFile {
    val fileName = "${text.hashCode()}.${getTestFileExtension()}"
    return myFixture.configureByText(fileName, text)
  }

  protected fun addFile(path: String, text: String): PsiFile {
    return myFixture.addFileToProject(path, text)
  }

  protected fun addZephyr() {
    val zephyr = Path.of(DTS_TEST_DATA_PATH, "zephyr").absolute().pathString

    val manager = ModuleManager.getInstance(project)
    ModuleRootModificationUtil.addContentRoot(manager.modules.first(), zephyr)

    HeavyPlatformTestCase.refreshRecursively(DtsUtil.findFile(zephyr)!!)

    DtsSettings.of(project).update {
      zephyrRoot = zephyr
      zephyrBoard = "$zephyr/boards/xtensa/esp32"
    }

    runBlocking {
      DtsZephyrProvider.of(project).awaitInit()
    }
  }

  protected fun getFixture(path: String): String {
    return Files.readString(Path.of(DTS_TEST_DATA_PATH, path))
  }

  protected fun getTestFixture(extension: String): String {
    return Files.readString(Path.of(testDataPath, "$testName.$extension"))
  }

  protected fun makeRelativeToWorkingDirectory(path: String): String {
    return path.replace(DTS_TEST_DATA_PATH, "WORKING_DIRECTORY")
  }

  protected fun compareWithTestFixture(extension: String, actual: String) {
    val path = Path.of(testDataPath, "$testName.$extension")

    if (path.exists()) {
      assertSameLinesWithFile(path.absolutePathString(), makeRelativeToWorkingDirectory(actual))
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
