package com.intellij.dts.ide

import com.intellij.dts.DtsIcons
import com.intellij.dts.DtsTestBase

class DtsOverrideLineMarkerProviderTest : DtsTestBase() {
    override fun getBasePath(): String = "ide/lineMark"

    fun `test no override`() = doTest(
        text = "&label {};",
        count = 0,
    )

    fun `test new property`() = doTest(
        text = "&label { prop; };",
        count = 0,
    )

    fun `test override`() = doTest(
        text = "&label { prop1; };",
        count = 1,
    )

    fun `test multiple overrides`() = doTest(
        text = "&label { prop1;\nprop2; };",
        count = 2,
    )

    private fun doTest(text: String, count: Int) {
        addFile("file.dtsi", getFixture("ide/lineMark/File.dtsi"))

        configureByText("""
            /include/ "file.dtsi"
            $text
        """)

        val gutter = myFixture.findAllGutters().filter { it.icon == DtsIcons.OverrideProperty }

        assertSize(count, gutter)
    }
}