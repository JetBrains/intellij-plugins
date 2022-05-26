package org.intellij.plugin.mdx

import com.intellij.psi.formatter.FormatterTestCase
import org.junit.Test

class MdxFormatterTest: FormatterTestCase() {
    override fun getTestDataPath(): String = "src/test/testData"
    override fun getBasePath(): String = "format"
    override fun getFileExtension(): String = "mdx"

    override fun getTestName(lowercaseFirstLetter: Boolean): String {
        return super.getTestName(false)
    }

    @Test
    fun testFormatting() = doTest()
}