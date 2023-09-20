package com.intellij.dts.documentation

import com.intellij.dts.DtsTestBase
import com.intellij.lang.documentation.ide.IdeDocumentationTargetProvider
import com.intellij.platform.backend.documentation.impl.computeDocumentationBlocking

class DtsZephyrDocumentationTest : DtsTestBase() {
    override fun getBasePath(): String = "documentation"

    override fun setUp() {
        super.setUp()
        addZephyr()
    }

    fun `test property binding`() = doTest()

    fun `test property binding with ref`() = doTest()

    fun `test property default binding`() = doTest()

    fun `test property inherited binding`() = doTest()

    fun `test property default`() = doTest()

    fun `test node binding`() = doTest()

    fun `test node inherited binding`() = doTest()

    fun `test node reference`() = doTest()

    fun `test node default`() = doTest()

    fun `test dts code`() = doTest()

    private fun doTest() {
        val content = getTestFixture("overlay")
        val fixture = getTestFixture("html")

        myFixture.configureByText("esp32.overlay", content)

        val documentations = IdeDocumentationTargetProvider
            .getInstance(project)
            .documentationTargets(myFixture.editor, myFixture.file, myFixture.caretOffset - 1)
            .mapNotNull { computeDocumentationBlocking(it.createPointer())?.html }

        assertOneElement(documentations)
        assertEquals(formatHtml(fixture), formatHtml(documentations.first()))
    }

    private fun formatHtml(text: String): String {
        return text
            .replace(Regex("\\s*\\n\\s*"), "")
            .replace(Regex("(<[^>]+>(?=\\s*<))"), "$0\n")
    }
}