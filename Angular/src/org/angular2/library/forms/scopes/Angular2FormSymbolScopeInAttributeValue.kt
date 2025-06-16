package org.angular2.library.forms.scopes

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.query.PolySymbolQueryExecutor
import com.intellij.polySymbols.query.PolySymbolQueryExecutorFactory
import com.intellij.polySymbols.utils.PolySymbolStructuredScope
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import com.intellij.util.asSafely
import org.angular2.Angular2Framework
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementVisitor
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.library.forms.*
import org.angular2.library.forms.impl.Angular2FormArrayControl
import org.angular2.library.forms.impl.Angular2UnknownFormArray
import org.angular2.library.forms.impl.Angular2UnknownFormControl
import org.angular2.library.forms.impl.Angular2UnknownFormGroup

class Angular2FormSymbolScopeInAttributeValue(attributeValue: XmlAttribute) : PolySymbolStructuredScope<XmlAttribute, PsiFile>(attributeValue) {

  companion object {
    private const val PROP_SOURCE_SYMBOL = "source-symbol"
  }

  fun getNearestFormGroup(): Angular2FormGroup? =
    getCurrentScope()
      ?.properties[PROP_SOURCE_SYMBOL]
      ?.asSafely<Angular2FormGroup>()

  override val rootPsiElement: PsiFile
    get() = location.containingFile

  override val providedSymbolKinds: Set<PolySymbolQualifiedKind>
    get() = NG_FORM_ANY_CONTROL_PROPS

  override val scopesBuilderProvider: (PsiFile, PolySymbolPsiScopesHolder) -> PsiElementVisitor?
    get() = provider@{ file, holder ->
      val formsComponent = Angular2FormsComponent.getFor(file)
                           ?: return@provider null
      val queryExecutor = PolySymbolQueryExecutorFactory.createCustom {
        setFramework(Angular2Framework.ID)
      }
      return@provider Angular2FormSymbolsScopesBuilder(
        queryExecutor, formsComponent, holder
      )
    }

  override fun createPointer(): Pointer<Angular2FormSymbolScopeInAttributeValue> {
    val locationPtr = location.createSmartPointer()
    return Pointer {
      locationPtr.element?.let { Angular2FormSymbolScopeInAttributeValue(it) }
    }
  }

  override fun findBestMatchingScope(rootScope: PolySymbolPsiScope): PolySymbolPsiScope? =
    super.findBestMatchingScope(rootScope)?.let {
      if ((it.source as? XmlTag)?.attributes?.contains(location) == true)
        it.parent
      else
        it
    }

  private class Angular2FormSymbolsScopesBuilder(
    private val queryExecutor: PolySymbolQueryExecutor,
    private val formsComponent: Angular2FormsComponent,
    private val holder: PolySymbolPsiScopesHolder,
  ) : Angular2HtmlRecursiveElementVisitor() {

    override fun visitXmlTag(tag: XmlTag) {
      val formGroupBinding = findFormGroupBinding(tag)
      val formGroupName = findFormGroupNameFromAttribute(tag)
      val formArrayName = findFormArrayNameFromAttribute(tag)
      val symbol =
        formGroupBinding
          ?.asSafely<JSReferenceExpression>()
          ?.let { formsComponent.getFormGroupFor(it) }
        ?: formGroupName
          ?.let {
            queryExecutor.nameMatchQuery(NG_FORM_GROUP_PROPS, it)
              .additionalScope(holder.currentScope())
              .run()
          }
          ?.firstNotNullOfOrNull { it as? Angular2FormGroup }
        ?: formArrayName
          ?.let {
            queryExecutor.nameMatchQuery(NG_FORM_ARRAY_PROPS, it)
              .additionalScope(holder.currentScope())
              .run()
          }
          ?.firstNotNullOfOrNull { it as? Angular2FormArray }

      if (formGroupBinding != null || formGroupName != null || formArrayName != null) {
        holder.pushScope(tag, symbol?.let { mapOf(PROP_SOURCE_SYMBOL to it) } ?: emptyMap(), NG_FORM_ANY_CONTROL_PROPS) {
          if (symbol is Angular2FormGroup) {
            addSymbols(symbol.members)
          }
          else if (symbol is Angular2FormArray) {
            addSymbol(Angular2FormArrayControl)
          }
          else {
            addSymbol(Angular2UnknownFormGroup)
            addSymbol(Angular2UnknownFormControl)
            addSymbol(Angular2UnknownFormArray)
          }
        }

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

    private fun findFormArrayNameFromAttribute(tag: XmlTag): String? =
      tag.attributes
        .find { it.name == FORM_ARRAY_NAME_ATTRIBUTE }
        ?.value

    override fun visitXmlAttribute(attribute: XmlAttribute) {
    }

    override fun visitXmlText(text: XmlText) {
    }
  }
}

