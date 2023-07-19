package com.intellij.dts

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.util.*

abstract class DtsTestBase : BasePlatformTestCase() {
    protected val testName: String
        get() = getTestName(false).toPascalCase()

    protected val testFile: String
        get() = "$testName.${getTestFileExtension()}"

    protected val testFilePath: String
        get() = "$testDataPath/$testFile"

    protected open fun getTestFileExtension(): String = "dtsi"

    override fun getTestDataPath(): String = "testData/$basePath"

    override fun getTestName(lowercaseFirstLetter: Boolean): String = super.getTestName(false)

    protected fun configureByText(text: String): String {
        val fileName = "${text.hashCode()}.dtsi"
        myFixture.configureByText(fileName, text)

        return fileName
    }

    protected fun addFile(path: String, text: String): PsiFile {
        return myFixture.addFileToProject(path, text)
    }
}

private fun String.toPascalCase(): String {
    val capitalize = { word: String -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

    return split(' ').joinToString("", transform = capitalize)
}