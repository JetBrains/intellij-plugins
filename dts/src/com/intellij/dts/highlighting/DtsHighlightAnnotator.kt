package com.intellij.dts.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ExternallyAnnotated
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsUtil

private val stringEscapeRx = Regex("\\\\((x[0-9a-fA-F]{1,2})|([0-7][0-8]{0,2})|[^x0-7])")

class DtsHighlightAnnotator : Annotator {
    private fun highlight(holder: AnnotationHolder, range: TextRange, attr: DtsTextAttributes) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(attr.attribute)
            .create()
    }

    private fun annotateStringEscape(element: PsiElement, holder: AnnotationHolder) {
        for (match in stringEscapeRx.findAll(element.text)) {
            val range = TextRange(
                element.textOffset + match.range.first,
                element.textOffset + match.range.last + 1
            )

            highlight(holder, range, DtsTextAttributes.STRING_ESCAPE)
        }
    }

    private fun annotateName(element: PsiElement, holder: AnnotationHolder) {
        if (element.parent is DtsProperty) {
            highlight(holder, element.textRange, DtsTextAttributes.PROPERTY_NAME)
        }
        else if (element.parent is DtsNode) {
            val (name, addr) = DtsUtil.splitName(element.text)

            if (name.isNotEmpty()) {
                val nameRange = TextRange.from(element.startOffset, name.length)
                highlight(holder, nameRange, DtsTextAttributes.NODE_NAME)
            }

            if (addr != null) {
                val addrStart = element.startOffset + name.length + 1
                val addrRange = TextRange(addrStart, element.endOffset)
                highlight(holder, addrRange, DtsTextAttributes.NODE_UNIT_ADDR)
            }
        }
    }

    private fun annotateLabel(element: PsiElement, holder: AnnotationHolder) {
        val range = TextRange.from(element.textOffset, element.textLength - 1)
        highlight(holder, range, DtsTextAttributes.LABEL)
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is ExternallyAnnotated) return

        when (element.elementType) {
            DtsTypes.LABEL -> annotateLabel(element, holder)
            DtsTypes.NAME -> annotateName(element, holder)
            DtsTypes.STRING_VALUE, DtsTypes.CHAR_VALUE -> annotateStringEscape(element, holder)
        }
    }
}