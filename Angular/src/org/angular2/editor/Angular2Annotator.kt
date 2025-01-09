package org.angular2.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.angular2.Angular2DecoratorUtil.isHostBinding
import org.angular2.Angular2DecoratorUtil.isHostListenerDecoratorEventLiteral
import org.angular2.Angular2DecoratorUtil.isHostBindingDecoratorLiteral
import org.angular2.lang.html.highlighting.Angular2HtmlHighlighterColors
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType

class Angular2Annotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    when (element) {
      is JSProperty -> {
        if (!isHostBinding(element)) return
        val identifier = element.nameIdentifier ?: return
        val range: TextRange
        val name: String
        when (identifier.elementType) {
          JSTokenTypes.STRING_LITERAL -> {
            name = JSStringUtil.unquoteStringLiteralValue(identifier.text)
            range = identifier.textRange.let { TextRange(it.startOffset + 1, it.endOffset - 1) }
          }
          JSTokenTypes.IDENTIFIER -> {
            name = identifier.text
            range = identifier.textRange
          }
          else -> {
            return
          }
        }
        val attributeType = Angular2AttributeNameParser.parse(name).type
        val color = when (attributeType) {
          Angular2AttributeType.PROPERTY_BINDING -> Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
          Angular2AttributeType.EVENT -> Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
          Angular2AttributeType.REGULAR -> XmlHighlighterColors.HTML_ATTRIBUTE_NAME
          else -> return
        }
        if (range.length > 0)
          holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(color)
            .create()
      }
      is JSLiteralExpression -> {
        val color = when {
          isHostBindingDecoratorLiteral(element) -> Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
          isHostListenerDecoratorEventLiteral(element) -> Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
          else -> return
        }
        val range = element.textRange.let { TextRange(it.startOffset + 1, it.endOffset - 1) }
        if (range.length > 0)
          holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(color)
            .create()
      }
    }
  }
}