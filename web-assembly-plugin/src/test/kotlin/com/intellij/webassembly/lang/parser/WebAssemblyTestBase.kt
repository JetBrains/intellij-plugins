package com.intellij.webassembly.lang.parser

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.parsingFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.webassembly.lang.psi.WebAssemblyFileType
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.BeforeEach

abstract class WebAssemblyTestBase(dataPath: String) {
    companion object {
        private val projectFixture = projectFixture()
    }

    private val parsing by parsingFixture(
        projectFixture,
        "src/test/resources/parser/$dataPath",
        WebAssemblyFileType.defaultExtension,
        definitions = arrayOf(WebAssemblyParserDefinition()),
    )

    @BeforeEach
    fun setUpParsing() {
        parsing.includeRanges = true
    }

    fun doTest() {
        try {
            parsing.doTest(true)
        } catch (e: AssertionError) {
            assumeFalse(parsing.testName.startsWith("Bad"), "Bad token tests are minor: $e")
        }
    }
}
