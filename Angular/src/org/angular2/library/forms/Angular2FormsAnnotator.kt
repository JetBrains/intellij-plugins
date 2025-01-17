package org.angular2.library.forms

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.html.Angular2HtmlFile

class Angular2FormsAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    when (element) {
      is XmlAttribute -> annotateXmlAttribute(element, holder)
      is JSLiteralExpression -> if (element.isQuotedLiteral)
        annotateJSStringLiteral(element, holder)
    }
  }

  private fun annotateJSStringLiteral(expression: JSLiteralExpression, holder: AnnotationHolder) {
    if (findFormGroupForGetCallParameter(expression) != null
        || findFormGroupForGetCallParameterArray(expression) != null) {
      var offset = expression.textRange.startOffset + 1
      var start = 0
      val text = expression.stringValue ?: return
      do {
        val dotIndex = text.indexOf('.', start)
        if (dotIndex >= 0) {
          holder
            .newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(TextRange(offset + dotIndex, offset + dotIndex + 1))
            .textAttributes(TypeScriptHighlighter.TS_DOT)
            .create()
        }
        val end = if (dotIndex < 0) text.length else dotIndex
        holder
          .newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
          .range(TextRange(offset + start, offset + end))
          .textAttributes(TypeScriptHighlighter.TS_INSTANCE_MEMBER_VARIABLE)
          .create()
        start = end + 1
      } while (start < text.length)
    }
  }

  private fun annotateXmlAttribute(attribute: XmlAttribute, holder: AnnotationHolder) {
    if (attribute.name.let { it == FORM_GROUP_NAME_ATTRIBUTE || it == FORM_CONTROL_NAME_ATTRIBUTE }
        && attribute.containingFile is Angular2HtmlFile) {
      attribute.valueElement
        ?.valueTextRange
        ?.takeIf { it.length > 0 }
        ?.let {
          holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
            .range(it)
            .textAttributes(TypeScriptHighlighter.TS_INSTANCE_MEMBER_VARIABLE)
            .create()
        }
    }
  }
}