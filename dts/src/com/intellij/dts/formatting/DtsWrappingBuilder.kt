package com.intellij.dts.formatting

import com.intellij.dts.lang.psi.*
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.dts.settings.DtsCodeStyleSettings
import com.intellij.formatting.*
import com.intellij.psi.util.elementType

abstract class DtsWrappingBuilder(protected val settings: DtsCodeStyleSettings) {
    companion object {
        fun childBuilder(settings: CodeStyleSettings): DtsWrappingBuilder =
            Null(settings.getCustomSettings(DtsCodeStyleSettings::class.java))

        fun childBuilder(parent: Block?, current: DtsWrappingBuilder): DtsWrappingBuilder {
            return when (ASTBlock.getElementType(parent)) {
                DtsTypes.NODE_CONTENT -> NodeContent(current.settings)
                DtsTypes.PROPERTY_CONTENT -> PropertyContent(current.settings)
                DtsTypes.CELL_ARRAY, DtsTypes.BYTE_ARRAY -> Array(current.settings)
                else -> current
            }
        }
    }

    fun getWrap(parent: Block?, child: ASTNode): Wrap? {
        val parentElement = ASTBlock.getPsiElement(parent)
        val childElement = child.psi ?: return null

        return getWrap(parentElement, childElement)
    }

    fun getAlignment(parent: Block?, child: ASTNode): Alignment? {
        val parentElement = ASTBlock.getPsiElement(parent)
        val childElement = child.psi ?: return null

        return getAlignment(parentElement, childElement)
    }

    protected abstract fun getWrap(parent: PsiElement?, child: PsiElement): Wrap?

    protected abstract fun getAlignment(parent: PsiElement?, child: PsiElement): Alignment?

    private class Null(settings: DtsCodeStyleSettings) : DtsWrappingBuilder(settings) {
        override fun getWrap(parent: PsiElement?, child: PsiElement): Wrap? = null
        override fun getAlignment(parent: PsiElement?, child: PsiElement): Alignment? = null
    }

    // handles wrapping for entries in side of nodes and the alignment for property assignments
    private class NodeContent(settings: DtsCodeStyleSettings) : DtsWrappingBuilder(settings) {
        private val wrap = Wrap.createWrap(WrapType.ALWAYS, true)
        private val alignment = Alignment.createAlignment(true, Alignment.Anchor.LEFT)

        override fun getWrap(parent: PsiElement?, child: PsiElement): Wrap? {
            if (parent is DtsNodeContent) {
                return wrap
            }

            return null
        }

        override fun getAlignment(parent: PsiElement?, child: PsiElement): Alignment? {
            if (settings.ALIGN_PROPERTY_ASSIGNMENT && parent is DtsProperty && child.elementType == DtsTypes.ASSIGN) {
                return alignment
            }

            return null
        }
    }

    // handles wrapping and alignment for property values
    private class PropertyContent(settings: DtsCodeStyleSettings) : DtsWrappingBuilder(settings) {
        private val wrap = Wrap.createWrap(WrapType.NORMAL, false)
        private val alignment = Alignment.createAlignment(true)

        override fun getWrap(parent: PsiElement?, child: PsiElement): Wrap? {
            if (parent is DtsPropertyContent && child is DtsValue) {
                return wrap
            }

            return null
        }

        override fun getAlignment(parent: PsiElement?, child: PsiElement): Alignment? {
            if (settings.ALIGN_PROPERTY_VALUES && parent is DtsPropertyContent && child is DtsValue)  {
                return alignment
            }

            return null
        }
    }

    // handles wrapping and alignment for array values
    private class Array(settings: DtsCodeStyleSettings) : DtsWrappingBuilder(settings) {
        private val wrap = Wrap.createWrap(WrapType.NORMAL, false)
        private val alignment = Alignment.createAlignment()

        override fun getWrap(parent: PsiElement?, child: PsiElement): Wrap? {
            if (parent is DtsArray && child is DtsValue) {
                return wrap
            }

            return null
        }

        override fun getAlignment(parent: PsiElement?, child: PsiElement): Alignment? {
            if (settings.ALIGN_PROPERTY_VALUES && parent is DtsArray && child is DtsValue) {
                return alignment
            }

            return null
        }
    }
}