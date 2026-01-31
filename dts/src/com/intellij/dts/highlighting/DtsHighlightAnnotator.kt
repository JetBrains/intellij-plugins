package com.intellij.dts.highlighting

import com.intellij.dts.lang.psi.DtsCompilerDirective
import com.intellij.dts.lang.psi.DtsPHandle
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsSubNode
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsUtil
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ExternallyAnnotated
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset

private val stringEscapeRx = Regex("\\\\((x[0-9a-fA-F]{1,2})|([0-7][0-8]{0,2})|[^x0-7])")

class DtsHighlightAnnotator : Annotator, DumbAware {
  fun interface Holder {
    fun newAnnotation(range: TextRange, attr: DtsTextAttributes)
  }

  private fun annotateStringEscape(element: PsiElement, holder: Holder) {
    for (match in stringEscapeRx.findAll(element.text)) {
      val range = TextRange(
        element.textOffset + match.range.first,
        element.textOffset + match.range.last + 1
      )

      holder.newAnnotation(range, DtsTextAttributes.STRING_ESCAPE)
    }
  }

  private fun annotateNodeName(nodeName: String, startOffset: Int, holder: Holder) {
    val (name, addr) = DtsUtil.splitName(nodeName)

    if (name.isNotEmpty()) {
      val nameRange = TextRange.from(startOffset, name.length)
      holder.newAnnotation(nameRange, DtsTextAttributes.NODE_NAME)
    }

    if (!addr.isNullOrEmpty()) {
      val addrRange = TextRange.from(startOffset + name.length + 1, addr.length)
      holder.newAnnotation(addrRange, DtsTextAttributes.NODE_UNIT_ADDR)
    }
  }

  private fun annotateName(element: PsiElement, holder: Holder) {
    when (val parent = element.parent) {
      is DtsProperty -> holder.newAnnotation(element.textRange, DtsTextAttributes.PROPERTY_NAME)
      is DtsSubNode -> annotateNodeName(element.text, element.startOffset, holder)
      is DtsCompilerDirective -> when (parent.dtsDirectiveType) {
        DtsTypes.DELETE_PROP -> holder.newAnnotation(element.textRange, DtsTextAttributes.PROPERTY_NAME)
        DtsTypes.DELETE_NODE -> annotateNodeName(element.text, element.startOffset, holder)
      }
    }
  }

  private fun annotateLabel(element: PsiElement, holder: Holder) {
    val range = TextRange.from(element.textOffset, element.textLength - 1)
    holder.newAnnotation(range, DtsTextAttributes.LABEL)
  }

  private fun annotatePHandle(element: DtsPHandle, holder: Holder) {
    element.dtsPHandleLabel?.let {
      holder.newAnnotation(it.textRange, DtsTextAttributes.LABEL)
    }
    element.dtsPHandlePath?.let {
      var offset = it.startOffset + 1
      for (segment in it.text.removePrefix("/").split("/")) {
        annotateNodeName(segment, offset, holder)
        offset += segment.length + 1
      }
    }
  }

  fun annotate(element: PsiElement, holder: Holder) {
    if (element is ExternallyAnnotated) return

    if (element is DtsPHandle) {
      annotatePHandle(element, holder)
      return
    }

    when (element.elementType) {
      DtsTypes.LABEL -> annotateLabel(element, holder)
      DtsTypes.NAME -> annotateName(element, holder)
      DtsTypes.STRING_LITERAL, DtsTypes.CHAR_LITERAL -> annotateStringEscape(element, holder)
    }
  }

  override fun annotate(element: PsiElement, holder: AnnotationHolder) = annotate(element) { range, attr ->
    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
      .range(range)
      .textAttributes(attr.attribute)
      .create()
  }
}