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
import com.intellij.webSymbols.highlighting.newSilentAnnotationWithDebugInfo
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
          @Suppress("HardCodedStringLiteral", "DialogTitleCapitalization")
          holder
            .newAnnotation(HighlightSeverity.INFORMATION, "dot")
            .range(TextRange(offset + dotIndex, offset + dotIndex + 1))
            .textAttributes(TypeScriptHighlighter.TS_DOT)
            .create()
        }
        val end = if (dotIndex < 0) text.length else dotIndex
        highlightFormControlReference(holder, TextRange(offset + start, offset + end), text.substring(start, end))
        start = end + 1
      }
      while (start < text.length)
    }
  }

  private fun annotateXmlAttribute(attribute: XmlAttribute, holder: AnnotationHolder) {
    if (attribute.name in FORM_ANY_CONTROL_NAME_ATTRIBUTES && attribute.containingFile is Angular2HtmlFile) {
      attribute.valueElement
        ?.valueTextRange
        ?.takeIf { it.length > 0 }
        ?.let {
          highlightFormControlReference(holder, it, attribute.value ?: "")
        }
    }
  }

  private fun highlightFormControlReference(holder: AnnotationHolder, textRange: TextRange, text: String) {
    val isNumber = text.toIntOrNull() != null
    holder.newSilentAnnotationWithDebugInfo(HighlightInfoType.SYMBOL_TYPE_SEVERITY, if (isNumber) "form array control" else "form control")
      .range(textRange)
      .needsUpdateOnTyping()
      .textAttributes(if (isNumber) TypeScriptHighlighter.TS_NUMBER else TypeScriptHighlighter.TS_INSTANCE_MEMBER_VARIABLE)
      .create()
  }
}