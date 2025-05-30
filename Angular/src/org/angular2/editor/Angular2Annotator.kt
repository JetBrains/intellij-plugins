package org.angular2.editor

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.psi.css.impl.util.CssHighlighter
import com.intellij.psi.xml.XmlAttribute
import com.intellij.polySymbols.highlighting.newSilentAnnotationWithDebugInfo

class Angular2Annotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    when (element) {
      is XmlAttribute -> visitXmlAttribute(element, holder)
    }
  }

  private fun visitXmlAttribute(element: XmlAttribute, holder: AnnotationHolder) {
    if (element.name == "class") {
      // TODO - move to a CSS annotator
      element.valueElement
        ?.valueTextRange
        ?.takeIf { it.length > 0 }
        ?.let {
          holder.newSilentAnnotationWithDebugInfo(HighlightInfoType.SYMBOL_TYPE_SEVERITY, CssHighlighter.CSS_CLASS_NAME.externalName)
            .range(it)
            .textAttributes(CssHighlighter.CSS_CLASS_NAME)
            .create()
        }
    }
  }
}