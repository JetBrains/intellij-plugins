package org.intellij.plugin.mdx

import com.intellij.lang.LanguageASTFactory
import com.intellij.lang.javascript.JavascriptASTFactory
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.dialects.ECMA6ParserDefinition
import com.intellij.lang.xml.XMLLanguage
import com.intellij.lang.xml.XmlASTFactory
import com.intellij.psi.LanguageFileViewProviders
import com.intellij.testFramework.ParsingTestCase
import org.intellij.plugin.mdx.js.MdxJSParserDefinition
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugin.mdx.lang.parse.MdxParserDefinition
import org.intellij.plugin.mdx.lang.psi.MdxFileViewProviderFactory
import org.intellij.plugins.markdown.lang.MarkdownFileViewProviderFactory
import org.intellij.plugins.markdown.lang.MarkdownLanguage
import org.intellij.plugins.markdown.lang.parser.MarkdownParserDefinition
import org.intellij.plugins.markdown.lang.psi.MarkdownAstFactory
import org.junit.Test

class MdxParsingTest : ParsingTestCase(
    "",
    "mdx",
    MdxParserDefinition(),
    MarkdownParserDefinition(),
    MdxJSParserDefinition(),
    ECMA6ParserDefinition()
) {

    override fun getTestDataPath(): String {
        return "src/test/testData/parsing"
    }

    override fun checkAllPsiRoots(): Boolean {
        return false
    }

    override fun setUp() {
        super.setUp()
        addExplicitExtension(LanguageFileViewProviders.INSTANCE, MdxLanguage, MdxFileViewProviderFactory())
        addExplicitExtension(LanguageFileViewProviders.INSTANCE, MdxLanguage, MarkdownFileViewProviderFactory())
        addExplicitExtension(LanguageASTFactory.INSTANCE, MarkdownLanguage.INSTANCE, MarkdownAstFactory())
        addExplicitExtension(LanguageASTFactory.INSTANCE, JavascriptLanguage.INSTANCE, JavascriptASTFactory())
        addExplicitExtension(LanguageASTFactory.INSTANCE, XMLLanguage.INSTANCE, XmlASTFactory())
    }

    override fun skipSpaces(): Boolean {
        return false
    }

    override fun includeRanges(): Boolean {
        return true
    }

    @Test
    fun testParsingTestData() {
        doTest(true)
    }

    @Test
    fun testParsingList() {
        doTest(true)
    }

    @Test
    fun testParsingWithNewLines() {
        doTest(true)
    }

    @Test
    fun testParsingAlert() {
        doTest(true)
    }

    @Test
    fun testParsingEmbedded() {
        doTest(true)
    }

    @Test
    fun testParsingPrisma() {
        doTest(true)
    }
}