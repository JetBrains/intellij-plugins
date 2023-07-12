package com.intellij.dts

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.util.*

abstract class DtsTestBase : BasePlatformTestCase() {
    protected val testName: String
        get() = getTestName(false).toPascalCase()

    protected val testFile: String
        get() = "$testName.${getTestFileExtension()}"

    protected val testFilePath: String
        get() = "$testDataPath/$testFile"

    open fun getTestFileExtension(): String = "dtsi"

    override fun getTestDataPath(): String = "testData/$basePath"

    override fun getTestName(lowercaseFirstLetter: Boolean): String = super.getTestName(false)
}

private fun String.toPascalCase(): String {
    val capitalize = { word: String -> word.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

    return split(' ').joinToString("", transform = capitalize)
}