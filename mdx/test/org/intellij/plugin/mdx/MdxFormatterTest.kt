package org.intellij.plugin.mdx

import com.intellij.psi.formatter.FormatterTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MdxFormatterTest: FormatterTestCase() {
    override fun getTestDataPath(): String = MdxTestUtil.testDataPath
    override fun getBasePath(): String = "format"
    override fun getFileExtension(): String = "mdx"

    override fun getTestName(lowercaseFirstLetter: Boolean): String {
        return super.getTestName(false)
    }

    @Test
    fun testFormatting() = doTest()
}