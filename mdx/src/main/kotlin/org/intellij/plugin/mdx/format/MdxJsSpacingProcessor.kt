package org.intellij.plugin.mdx.format

import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.JSNodeVisitor
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.formatter.JSSpacingProcessor
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.intellij.plugin.mdx.lang.psi.MdxTemplateDataElementType
import java.util.function.BiConsumer

class MdxJsSpacingProcessor(parent: ASTNode?,
                       child1: ASTNode,
                       child2: ASTNode,
                       topSettings: CodeStyleSettings,
                       dialect: Language?,
                       jsCodeStyleSettings: JSCodeStyleSettings) : JSSpacingProcessor(parent, child1, child2, topSettings, dialect, jsCodeStyleSettings) {
    fun visitMdxElement(node: ASTNode) {
        val type = node.elementType
        if (type is MdxTemplateDataElementType) {
            BiConsumer<JSNodeVisitor, ASTNode> { obj: JSNodeVisitor, node: ASTNode? -> obj.visitEmbeddedContent(node) }.accept(this, node)
            return
        } else {
            super.visit(node)
        }
    }

    override fun calcSpacing(): Spacing? {
        if (myParent.elementType is MdxTemplateDataElementType){
            visitMdxElement(myParent)
            return myResult
        }
        return super.calcSpacing()
    }
}