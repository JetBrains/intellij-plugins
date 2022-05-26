package org.intellij.plugin.mdx.format

import com.intellij.formatting.*
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock
import com.intellij.formatting.templateLanguages.TemplateLanguageBlockFactory
import com.intellij.formatting.templateLanguages.TemplateLanguageFormattingModelBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.DocumentBasedFormattingModel
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.formatter.xml.SyntheticBlock
import com.intellij.psi.templateLanguages.SimpleTemplateLanguageFormattingModelBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTag
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.lang.MarkdownElementType

class MdxFormattingModelBuilder : TemplateLanguageFormattingModelBuilder() {
    override fun createTemplateLanguageBlock(node: ASTNode,
                                             wrap: Wrap?,
                                             alignment: Alignment?,
                                             foreignChildren: MutableList<DataLanguageBlockWrapper>?,
                                             codeStyleSettings: CodeStyleSettings): TemplateLanguageBlock {
        val documentModel = FormattingDocumentModelImpl.createOn(node.psi.containingFile)
        return MdxBlock(this, codeStyleSettings, node, foreignChildren, HtmlPolicy(codeStyleSettings, documentModel))
    }

    override fun createModel(element: PsiElement, settings: CodeStyleSettings): FormattingModel {
        val file = element.containingFile
        val rootBlock: Block
        val node = element.node
        rootBlock = if (node.elementType === MdxTokenTypes.OUTER_ELEMENT_TYPE) {
            return SimpleTemplateLanguageFormattingModelBuilder().createModel(element, settings)
        } else {
            getRootBlock(file, file.viewProvider, settings)
        }
        return DocumentBasedFormattingModel(rootBlock, element.project, settings, file.fileType, file)
    }

    override fun dontFormatMyModel(): Boolean {
        return false
    }


    private class MdxBlock internal constructor(blockFactory: TemplateLanguageBlockFactory, settings: CodeStyleSettings,
                                                node: ASTNode, foreignChildren: List<DataLanguageBlockWrapper>?, private val myHtmlPolicy: HtmlPolicy) : TemplateLanguageBlock(blockFactory, settings, node, foreignChildren) {

        override fun getTemplateTextElementType(): IElementType {
            return MarkdownElementType.platformType(MdxTokenTypes.JSX_BLOCK_CONTENT)
        }
        override fun getIndent(): Indent? {
            if (myNode.text.trim { it <= ' ' }.isEmpty()) {
                return Indent.getNoneIndent()
            }

            val foreignParent = getForeignBlockParent(true)
            return if (foreignParent != null) {
                if (foreignParent.node is XmlTag
                        && !myHtmlPolicy.indentChildrenOf(foreignParent.node as XmlTag?)) {
                    Indent.getNoneIndent()
                } else Indent.getNormalIndent()
            } else Indent.getNoneIndent()
        }

        override fun isRequiredRange(range: TextRange): Boolean {
            return false
        }

        override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
            return if (myNode.elementType === MdxTokenTypes.JSX_BLOCK_CONTENT) {
                ChildAttributes(Indent.getNormalIndent(), null)
            } else {
                ChildAttributes(Indent.getNoneIndent(), null)
            }
        }

        private fun getForeignBlockParent(immediate: Boolean): DataLanguageBlockWrapper? {
            var foreignBlockParent: DataLanguageBlockWrapper? = null
            var parent = parent
            while (parent != null) {
                if (parent is DataLanguageBlockWrapper && parent.original !is SyntheticBlock) {
                    foreignBlockParent = parent
                    break
                } else if (immediate && parent is MdxBlock) {
                    break
                }
                parent = parent.parent
            }
            return foreignBlockParent
        }

    }
}
