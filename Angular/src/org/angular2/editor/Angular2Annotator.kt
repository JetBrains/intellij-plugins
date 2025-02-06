package org.angular2.editor

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.javascript.JSStringUtil
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.css.impl.util.CssHighlighter
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.HOST_BINDING_DEC
import org.angular2.Angular2DecoratorUtil.SELECTOR_PROP
import org.angular2.Angular2DecoratorUtil.VIEW_CHILDREN_DEC
import org.angular2.Angular2DecoratorUtil.VIEW_CHILD_DEC
import org.angular2.Angular2DecoratorUtil.getDecoratorForLiteralParameter
import org.angular2.Angular2DecoratorUtil.isHostBinding
import org.angular2.Angular2DecoratorUtil.isHostBindingClassValueLiteral
import org.angular2.Angular2DecoratorUtil.isHostListenerDecoratorEventLiteral
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2EntityUtils.getPropertyDeclarationOrReferenceKindAndDirective
import org.angular2.lang.expr.highlighting.Angular2HighlighterColors
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.expr.psi.Angular2TemplateBindingKey
import org.angular2.lang.html.highlighting.Angular2HtmlHighlighterColors
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.signals.Angular2SignalUtils.getPossibleSignalFunNameForLiteralParameter
import org.angular2.signals.Angular2SignalUtils.isViewChildSignalCall
import org.angular2.signals.Angular2SignalUtils.isViewChildrenSignalCall
import org.angular2.web.NG_DIRECTIVE_ATTRIBUTE_SELECTORS
import org.angular2.web.NG_DIRECTIVE_ELEMENT_SELECTORS
import org.angular2.web.isNgClassLiteralContext

class Angular2Annotator : Annotator {
  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    when (element) {
      is JSProperty -> visitJSProperty(element, holder)
      is JSLiteralExpression -> visitJSLiteralExpression(element, holder)
      is XmlAttribute -> visitXmlAttribute(element, holder)
      is Angular2HtmlNgContentSelector -> visitAngular2DirectiveSelector(element.selector, holder)
      is Angular2TemplateBindingKey -> visitTemplateBindingKey(element, holder)
    }
  }

  private fun visitJSProperty(element: JSProperty, holder: AnnotationHolder) {
    val isHostBinding = isHostBinding(element)
    val isNgClassAttribute = isNgClassLiteralContext(element.parent)
    if (!isHostBinding && !isNgClassAttribute) return
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
    val parsedName = Angular2AttributeNameParser.parse(name)
    val color = when {
      isHostBinding -> when (parsedName.type) {
        Angular2AttributeType.PROPERTY_BINDING -> Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
        Angular2AttributeType.EVENT -> Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
        Angular2AttributeType.REGULAR -> XmlHighlighterColors.HTML_ATTRIBUTE_NAME
        else -> return
      }
      else -> CssHighlighter.CSS_CLASS_NAME
    }
    if (range.length > 0) {
      holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
        .range(range)
        .textAttributes(color)
        .create()
      if (isHostBinding && parsedName is Angular2AttributeNameParser.PropertyBindingInfo) {
        holder.highlightPropertyBinding(parsedName, range.startOffset)
      }
    }
  }

  private fun visitJSLiteralExpression(
    element: JSLiteralExpression,
    holder: AnnotationHolder,
  ) {
    if (!element.isQuotedLiteral) return
    if (element.parent.asSafely<JSProperty>()?.name == SELECTOR_PROP) {
      Angular2EntitiesProvider.getDirective(element.parentOfType<ES6Decorator>())
        ?.selector
        ?.takeIf { it.psiParent == element }
        ?.let {
          visitAngular2DirectiveSelector(it, holder)
        }
      return
    }
    val decorator = getDecoratorForLiteralParameter(element)
    val signalFunctionName = getPossibleSignalFunNameForLiteralParameter(element)
    val isHostBindingDecoratorLiteral = decorator?.decoratorName == HOST_BINDING_DEC
    val isViewChildrenLiteral = decorator?.decoratorName.let { it == VIEW_CHILDREN_DEC || it == VIEW_CHILD_DEC }
                                || isViewChildSignalCall(signalFunctionName)
                                || isViewChildrenSignalCall(signalFunctionName)
    val isHostListenerDecoratorEventLiteral = isHostListenerDecoratorEventLiteral(element)
    val info = getPropertyDeclarationOrReferenceKindAndDirective(element, true)
               ?: getPropertyDeclarationOrReferenceKindAndDirective(element, false)
    val color = when {
      isHostBindingDecoratorLiteral || info?.kind == Angular2DecoratorUtil.INPUTS_PROP ->
        Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
      isHostListenerDecoratorEventLiteral || info?.kind == Angular2DecoratorUtil.OUTPUTS_PROP ->
        Angular2HtmlHighlighterColors.NG_EVENT_BINDING_ATTR_NAME
      isViewChildrenLiteral ->
        Angular2HighlighterColors.NG_VARIABLE
      isHostBindingClassValueLiteral(element) || isNgClassLiteralContext(element) -> CssHighlighter.CSS_CLASS_NAME
      else -> return
    }
    val range = element.textRange.let { TextRange(it.startOffset + 1, it.endOffset - 1) }
    if (range.length > 0) {
      holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
        .range(range)
        .textAttributes(color)
        .create()
      if (isHostBindingDecoratorLiteral) {
        Angular2AttributeNameParser.parse(element.stringValue?.let { "[$it]" } ?: return)
          .asSafely<Angular2AttributeNameParser.PropertyBindingInfo>()
          ?.let { info ->
            holder.highlightPropertyBinding(info, range.startOffset - 1)
          }
      }
    }
  }

  private fun visitXmlAttribute(element: XmlAttribute, holder: AnnotationHolder) {
    if (element.name == "class") {
      // TODO - move to a CSS annotator
      element.valueElement
        ?.valueTextRange
        ?.takeIf { it.length > 0 }
        ?.let {
          holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
            .range(it)
            .textAttributes(CssHighlighter.CSS_CLASS_NAME)
            .create()
        }
    }
    else {
      val elementNameOffset = element.nameElement.textRange.startOffset
      Angular2AttributeNameParser.parse(element.name)
        .asSafely<Angular2AttributeNameParser.PropertyBindingInfo>()
        ?.let {
          holder.highlightPropertyBinding(it, elementNameOffset)
        }
    }
  }

  private fun visitTemplateBindingKey(key: Angular2TemplateBindingKey, holder: AnnotationHolder) {
    val color = when ((key.parent as? Angular2TemplateBinding ?: return).keyKind) {
      Angular2TemplateBinding.KeyKind.LET -> TypeScriptHighlighter.TS_INSTANCE_MEMBER_VARIABLE
      Angular2TemplateBinding.KeyKind.BINDING ->Angular2HtmlHighlighterColors.NG_PROPERTY_BINDING_ATTR_NAME
      else -> return
    }
    holder.newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
      .range(key.textRange)
      .textAttributes(color)
      .create()
  }

  private fun visitAngular2DirectiveSelector(selector: Angular2DirectiveSelector, holder: AnnotationHolder) {
    val offset = selector.psiParent.textRange.startOffset
    for (it in selector.simpleSelectorsWithPsi) {
      for (symbol in it.allSymbols) {
        holder.highlightSymbol(symbol.textRangeInSourceElement.shiftRight(offset), symbol.qualifiedKind)
      }
    }
  }

  private fun AnnotationHolder.highlightPropertyBinding(info: Angular2AttributeNameParser.PropertyBindingInfo, offset: Int) {
    val color = when (info.bindingType) {
      PropertyBindingType.ATTRIBUTE -> XmlHighlighterColors.HTML_ATTRIBUTE_NAME
      PropertyBindingType.CLASS -> CssHighlighter.CSS_CLASS_NAME
      PropertyBindingType.STYLE -> CssHighlighter.CSS_PROPERTY_NAME
      else -> return
    }
    newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
      .range(TextRange(offset + info.nameOffset, offset + info.nameOffset + info.name.length))
      .textAttributes(color)
      .create()
  }

  private fun AnnotationHolder.highlightSymbol(textRange: TextRange, qualifiedKind: WebSymbolQualifiedKind): Boolean {
    val color = colorForSymbolKind(qualifiedKind)
    if (color != null) {
      newSilentAnnotation(HighlightInfoType.SYMBOL_TYPE_SEVERITY)
        .range(textRange)
        .textAttributes(color)
        .create()
      return true
    }
    return false
  }

  private fun colorForSymbolKind(qualifiedKind: WebSymbolQualifiedKind): TextAttributesKey? =
    when (qualifiedKind) {
      WebSymbol.CSS_CLASSES -> CssHighlighter.CSS_CLASS_NAME
      WebSymbol.CSS_PROPERTIES -> CssHighlighter.CSS_PROPERTY_NAME
      WebSymbol.HTML_ATTRIBUTES, NG_DIRECTIVE_ATTRIBUTE_SELECTORS ->
        XmlHighlighterColors.HTML_ATTRIBUTE_NAME
      NG_DIRECTIVE_ELEMENT_SELECTORS -> XmlHighlighterColors.HTML_TAG_NAME
      else -> null
    }
}