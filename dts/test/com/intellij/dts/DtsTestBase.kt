package com.intellij.dts

import com.intellij.application.options.CodeStyle
import com.intellij.dts.lang.DtsFileType
import com.intellij.dts.settings.DtsSettings
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

val DTS_TEST_ROOT_PATH: String = PathManager.getHomePath() + "/contrib/dts"
val DTS_TEST_DATA_PATH: String = PathManager.getHomePath() + "/contrib/dts/testData"

abstract class DtsTestBase : BasePlatformTestCase() {
  override fun setUp() {
    super.setUp()

    val settings = CodeStyle.getSettings(project).getIndentOptions(DtsFileType)
    settings.USE_TAB_CHARACTER = false
  }

  private val testName: String
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

    val settings = DtsSettings.of(project)
    settings.state.zephyrBoard = Path.of(zephyr, "boards/xtensa/esp32").absolute().pathString
  }

  protected fun getFixture(path: String): String {
    return Files.readString(Path.of(DTS_TEST_DATA_PATH, path))
  }

  protected fun getTestFixture(extension: String): String {
    return Files.readString(Path.of(testDataPath, "$testName.$extension"))
  }

  protected fun compareWithTestFixture(extension: String, actual: String) {
    val path = Path.of(testDataPath, "$testName.$extension")

    if (path.exists()) {
      assertSameLinesWithFile(path.absolutePathString(), actual.replacePath())
    }
    else {
      path.writeText(actual.replacePath())
      fail("File ${path.pathString} did not exist. Created new fixture.")
    }
  }
}

private fun String.toPascalCase(): String {
  val capitalize = { word: String -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

  return split(' ').joinToString("", transform = capitalize)
}

private fun String.replacePath(): String {
  return replace(DTS_TEST_DATA_PATH, "WORKING_DIRECTORY")
}
