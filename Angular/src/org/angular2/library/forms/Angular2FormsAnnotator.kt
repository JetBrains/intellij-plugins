package org.angular2.library.forms

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.html.Angular2HtmlFile

class Angular2FormsAnnotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    when (element) {
      is XmlAttribute -> annotateXmlAttribute(element, holder)
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