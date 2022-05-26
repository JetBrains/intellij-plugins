package org.intellij.plugin.mdx.lang.psi

import com.intellij.lang.javascript.types.JEEmbeddedBlockElementType
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.lexer.Lexer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateDataModifications
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugin.mdx.lang.parse.JsxBlockProvider
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.lang.MarkdownElementType
import java.util.*
import kotlin.math.min

object MdxTemplateDataElementType : MdxTemplateDataElementTypeBase(),
        JEEmbeddedBlockElementType {

    override fun isModule(): Boolean = true

    override fun getTemplateDataInsertionTokens(): TokenSet {
        return TokenSet.forAllMatching(IElementType.TRUE)
    }
}


open class MdxTemplateDataElementTypeBase : TemplateDataElementType("MDX_TEMPLATE_JSX",
        MdxLanguage,
        MarkdownElementType.platformType(MdxTokenTypes.JSX_BLOCK_CONTENT),
        MdxTokenTypes.OUTER_ELEMENT_TYPE) {
    var lastImportExportBlock = 0
    override fun collectTemplateModifications(sourceCode: CharSequence, baseLexer: Lexer): TemplateDataModifications {
        val modifications = TemplateDataModifications()
        baseLexer.start(sourceCode)
         while (baseLexer.tokenType != null) {
            if (baseLexer.tokenType === MarkdownElementType.platformType(MdxTokenTypes.JSX_BLOCK_CONTENT)) {
                val trim = baseLexer.tokenText.trim()
                var hadEnter = false
                if (trim.startsWith("import") || trim.startsWith("export")) {
                    while (baseLexer.tokenType == MarkdownElementType.platformType(MdxTokenTypes.JSX_BLOCK_CONTENT)) {
                        if (baseLexer.tokenText.trim() == "" && hadEnter) {
                            break
                        }
                        lastImportExportBlock = baseLexer.tokenEnd
                        if (baseLexer.tokenText.endsWith('\n')){
                            hadEnter = true
                        }
                        baseLexer.advance()
                    }
                    lastImportExportBlock = baseLexer.tokenEnd
                    if (baseLexer.tokenType == null) {
                        return modifications
                    }
                    val sequence = baseLexer.bufferSequence
                    val trim1 = sequence.subSequence(0, lastImportExportBlock).trim()
                    if (!trim1.endsWith(';')) {
                        modifications.addRangeToRemove(min(trim1.length, sequence.length - 1), ";")
                    }
                }
            } else {
                val range = TextRange.create(baseLexer.tokenStart, baseLexer.tokenEnd)
                modifications.addOuterRange(range, true)
            }
            baseLexer.advance()
        }
        return modifications
    }
}
