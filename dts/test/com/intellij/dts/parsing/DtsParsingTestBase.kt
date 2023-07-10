package com.intellij.dts.parsing

import com.intellij.dts.completion.DtsBraceMatcher
import com.intellij.dts.lang.parser.DtsParserDefinition
import com.intellij.lang.LanguageBraceMatching
import com.intellij.testFramework.ParsingTestCase

abstract class DtsParsingTestBase(dataPath: String, fileExt: String) : ParsingTestCase(dataPath, fileExt, DtsParserDefinition()) {
    override fun getTestDataPath(): String = "testData/parser"

    override fun setUp() {
        super.setUp()

        // fixes issue when parser tests run before typing tests
        addExplicitExtension(LanguageBraceMatching.INSTANCE, myLanguage, DtsBraceMatcher())
    }
}