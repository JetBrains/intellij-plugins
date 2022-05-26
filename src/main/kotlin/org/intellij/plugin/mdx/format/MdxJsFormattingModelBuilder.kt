package org.intellij.plugin.mdx.format

import com.intellij.formatting.FormattingDocumentModel
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.WrapType
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.JSLanguageUtil
import com.intellij.lang.javascript.formatter.JavascriptFormattingModelBuilder
import com.intellij.lang.javascript.psi.JSFunctionExitPoint
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.formatter.xml.XmlPolicy
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText

class MdxJsFormattingModelBuilder : JavascriptFormattingModelBuilder() {
    override fun createModel(element: PsiElement, settings: CodeStyleSettings, mode: FormattingMode): FormattingModel {
        val dialect = JSLanguageUtil.getLanguageDialect(element)
        val alignment = element.node.getUserData(BLOCK_ALIGNMENT)
        val jsBlockContext = MdxJsBlockContext(settings, dialect, null, mode)
        val rootBlock = jsBlockContext.createBlock(element.node, null, alignment, null, null, null)
        val formattingModel = createJSFormattingModel(element.containingFile, settings, rootBlock)
        val xmlFormattingPolicy = getPolicy(element, settings, formattingModel.documentModel)
        jsBlockContext.setXmlFormattingPolicy(xmlFormattingPolicy)
        return formattingModel
    }
    fun getPolicy(element: PsiElement, settings: CodeStyleSettings, model: FormattingDocumentModel): XmlFormattingPolicy {
        return if (DialectDetector.isJSX(element)) object : HtmlPolicy(settings, model) {
            override fun getWrappingTypeForTagBegin(tag: XmlTag): WrapType {
                val parent = tag.parent
                if (isRestrictedProduction(parent)) return WrapType.NONE
                if (parent !is XmlTag) return WrapType.NORMAL
                return if (newlineProhibitedBefore(tag)) WrapType.NONE else super.getWrappingTypeForTagBegin(tag)
            }

            override fun indentChildrenOf(parentTag: XmlTag): Boolean {
                return if (parentTag != null && parentTag.name.isEmpty()) true else super.indentChildrenOf(parentTag)
            }

            override fun allowWrapBeforeText(): Boolean {
                return false
            }

            override fun insertLineBreakBeforeTag(xmlTag: XmlTag): Boolean {
                return false
            }

            override fun checkName(tag: XmlTag, option: String): Boolean {
                return checkName(tag, option, false)
            }

            override fun isInlineTag(tag: XmlTag): Boolean {
                return StringUtil.isCapitalized(tag.name) || super.isInlineTag(tag)
            }
        } else object : XmlPolicy(settings, model) {
            override fun getWrappingTypeForTagBegin(tag: XmlTag): WrapType {
                return if (tag.parent !is XmlTag) WrapType.NORMAL else super.getWrappingTypeForTagBegin(tag)
            }
        }
    }

    private fun newlineProhibitedBefore(tag: PsiElement): Boolean {
        val prevSibling = tag.prevSibling
        return (prevSibling is PsiWhiteSpace
                && prevSibling.getPrevSibling() is XmlText)
    }

    private fun isRestrictedProduction(parent: PsiElement?): Boolean {
        return parent is JSFunctionExitPoint
    }

}