package org.angular2.library.forms.scopes

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.model.Pointer
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.query.WebSymbolsQueryExecutor
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import com.intellij.webSymbols.utils.WebSymbolsStructuredScope
import org.angular2.Angular2Framework
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementVisitor
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.library.forms.*

class Angular2FormSymbolsScopeInAttributeValue(attributeValue: XmlAttribute) : WebSymbolsStructuredScope<XmlAttribute, PsiFile>(attributeValue) {

  companion object {
    private val providedSymbolKinds: Set<WebSymbolQualifiedKind> = setOf(NG_FORM_CONTROL_PROPS, NG_FORM_GROUP_PROPS)
    private const val PROP_SOURCE_SYMBOL = "source-symbol"
  }

  fun getNearestFormGroup(): Angular2FormGroup? =
    getRootScope()
      ?.let { findBestMatchingScope(it) }
      ?.properties[PROP_SOURCE_SYMBOL]
      ?.asSafely<Angular2FormGroup>()

  override val rootPsiElement: PsiFile
    get() = location.containingFile

  override val providedSymbolKinds: Set<WebSymbolQualifiedKind>
    get() = Companion.providedSymbolKinds

  override val scopesBuilderProvider: (PsiFile, WebSymbolsPsiScopesHolder) -> PsiElementVisitor?
    get() = provider@{ file, holder ->
      val formsComponent = Angular2FormsComponent.getFor(file)
                           ?: return@provider null
      val queryExecutor = WebSymbolsQueryExecutorFactory.createCustom {
        setFramework(Angular2Framework.ID)
      }
      return@provider Angular2FormSymbolsScopesBuilder(
        queryExecutor, formsComponent, holder
      )
    }

  override fun createPointer(): Pointer<Angular2FormSymbolsScopeInAttributeValue> {
    val locationPtr = location.createSmartPointer()
    return Pointer {
      locationPtr.element?.let { Angular2FormSymbolsScopeInAttributeValue(it) }
    }
  }

  override fun findBestMatchingScope(rootScope: WebSymbolsPsiScope): WebSymbolsPsiScope? =
    super.findBestMatchingScope(rootScope)?.let {
      if ((it.source as? XmlTag)?.attributes?.contains(location) == true) {
        it.parent
      }
      else
        it
    }

  private class Angular2FormSymbolsScopesBuilder(
    private val queryExecutor: WebSymbolsQueryExecutor,
    private val formsComponent: Angular2FormsComponent,
    private val holder: WebSymbolsPsiScopesHolder,
  ) : Angular2HtmlRecursiveElementVisitor() {

    override fun visitXmlTag(tag: XmlTag) {
      val formGroupBinding = findFormGroupBinding(tag)
      val formGroupName = findFormGroupNameFromAttribute(tag)
      val symbol = formGroupBinding
                     ?.asSafely<JSReferenceExpression>()
                     ?.let { formsComponent.getFormGroupFor(it) }
                   ?: formGroupName
                     ?.let { queryExecutor.runNameMatchQuery(NG_FORM_GROUP_PROPS.withName(it), additionalScope = listOf(holder.currentScope())) }
                     ?.firstNotNullOfOrNull { it as? Angular2FormGroup }

      if (formGroupBinding != null || formGroupName != null) {
        holder.pushScope(tag, symbol?.let { mapOf(PROP_SOURCE_SYMBOL to it) } ?: emptyMap(), providedSymbolKinds)
        symbol?.members?.let { holder.addSymbols(it) }
        try {
          super.visitXmlTag(tag)
        }
        finally {
          holder.popScope()
        }
      }
      else {
        super.visitXmlTag(tag)
      }
    }

    private fun findFormGroupBinding(tag: XmlTag): JSExpression? = tag
      .attributes
      .find {
        Angular2AttributeNameParser.parse(it.name, tag)
          .asSafely<Angular2AttributeNameParser.PropertyBindingInfo>()
          ?.takeIf { it.bindingType == PropertyBindingType.PROPERTY }
          ?.name == FORM_GROUP_BINDING
      }
      ?.let { Angular2Binding.get(it) }
      ?.expression

    private fun findFormGroupNameFromAttribute(tag: XmlTag): String? =
      tag.attributes
        .find { it.name == FORM_GROUP_NAME_ATTRIBUTE }
        ?.value

    override fun visitXmlAttribute(attribute: XmlAttribute) {
    }

    override fun visitXmlText(text: XmlText) {
    }
  }

}

