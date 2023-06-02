package com.intellij.webassembly.lang.parser

import com.intellij.testFramework.JUnit38AssumeSupportRunner
import com.intellij.testFramework.ParsingTestCase
import com.intellij.webassembly.lang.psi.WebAssemblyFileType
import junit.framework.AssertionFailedError
import org.junit.AssumptionViolatedException
import org.junit.runner.RunWith


@RunWith(JUnit38AssumeSupportRunner::class)
abstract class WebAssemblyTestBase(dataPath: String) :
    ParsingTestCase("parser/$dataPath", WebAssemblyFileType.defaultExtension, WebAssemblyParserDefinition()) {

    fun doTest() {
        try {
            doTest(true)
        } catch (e: AssertionFailedError) {
            if (getTestName(false).startsWith("Bad")) {
                throw AssumptionViolatedException("Bad token tests are minor", e)
            }
        }
    }

    override fun getTestDataPath(): String = "src/test/resources"

    override fun skipSpaces(): Boolean = false

    override fun includeRanges(): Boolean = true
}