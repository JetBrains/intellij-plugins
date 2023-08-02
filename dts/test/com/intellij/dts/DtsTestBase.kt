package com.intellij.dts

import com.intellij.dts.settings.DtsSettings
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.absolute
import kotlin.io.path.pathString

abstract class DtsTestBase : BasePlatformTestCase() {
    protected val testName: String
        get() = getTestName(false)
            .replace(Regex("[_.,-]+"), " ")
            .toPascalCase()

    protected val testFile: String
        get() = "$testName.${getTestFileExtension()}"

    protected val testFilePath: String
        get() = "$testDataPath/$testName.${getTestFileExtension()}"

    protected open fun getTestFileExtension(): String = "dtsi"

    override fun getTestDataPath(): String = "testData/$basePath"

    protected fun configureByText(text: String): String {
        val fileName = "${text.hashCode()}.${getTestFileExtension()}"
        myFixture.configureByText(fileName, text.replaceCaret())

        return fileName
    }

    protected fun addFile(path: String, text: String): PsiFile {
        return myFixture.addFileToProject(path, text)
    }

    protected fun addZephyr() {
        val zephyr = Path.of("testData/zephyr").absolute()

        val manager = ModuleManager.getInstance(project)
        ModuleRootModificationUtil.addContentRoot(manager.modules.first(), zephyr.pathString)

        val settings = DtsSettings.of(project)
        settings.update {
            zephyrBoard = "esp32"
            zephyrArch = "xtensa"
        }
    }

    protected fun getTestFixture(extension: String): String {
        return Files.readString(Path.of(testDataPath, "$testName.$extension")).replaceCaret()
    }
}

private fun String.toPascalCase(): String {
    val capitalize = { word: String -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

    return split(' ').joinToString("", transform = capitalize)
}

private fun String.replaceCaret(): String {
    return replace("/*caret*/", "<caret>")
}