package com.intellij.dts.documentation

class DtsBindingDocumentationTest : DtsDocumentationTest() {
    override fun getBasePath(): String = "documentation/binding"

    override fun setUp() {
        super.setUp()
        addZephyr()
    }

    fun `test compatible ethernet`() = doTest()

    fun `test compatible espressif,esp32-pinctrl`() = doTest()
}