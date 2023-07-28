package com.intellij.dts.parsing

import com.intellij.dts.completion.DtsBraceMatcher
import com.intellij.dts.lang.DtsLanguage
import com.intellij.dts.lang.parser.DtsParserDefinition
import com.intellij.lang.LanguageBraceMatching
import com.intellij.psi.PsiFile
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.ParsingTestCase
import java.nio.charset.StandardCharsets

abstract class DtsParsingTestBase(dataPath: String, fileExt: String) : ParsingTestCase(dataPath, fileExt, DtsParserDefinition()) {
    override fun getTestDataPath(): String = "testData/parser"

    override fun setUp() {
        super.setUp()

        // fixes issue when parser tests run before typing tests
        addExplicitExtension(LanguageBraceMatching.INSTANCE, myLanguage, DtsBraceMatcher())
    }

    override fun createFile(name: String, text: String): PsiFile {
        val virtualFile = LightVirtualFile(name, DtsLanguage, text)
        virtualFile.charset = StandardCharsets.UTF_8

        SingleRootFileViewProvider.doNotCheckFileSizeLimit(virtualFile)

        return createFile(virtualFile)
    }
}