// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.html.webSymbols.HtmlDescriptorUtils.getHtmlNSDescriptor
import com.intellij.html.webSymbols.WebSymbolsHtmlQueryConfigurator
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType
import com.intellij.refactoring.rename.api.RenameValidationResult
import com.intellij.refactoring.rename.api.RenameValidator
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.declarations.WebSymbolDeclaration
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import com.intellij.xml.XmlElementDescriptor
import org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement
import org.angular2.codeInsight.documentation.Angular2ElementDocumentationTarget
import org.angular2.lang.Angular2Bundle
import org.angular2.web.Angular2Symbol
import org.angular2.web.NG_DIRECTIVE_ATTRIBUTE_SELECTORS
import org.angular2.web.NG_DIRECTIVE_ELEMENT_SELECTORS
import java.util.*
import java.util.regex.Pattern

class Angular2DirectiveSelectorSymbol(
  private val myParent: Angular2DirectiveSelectorImpl,
  override val textRangeInSourceElement: TextRange,
  override val name: @NlsSafe String,
  private val myElementSelector: String?,
  private val isElementSelector: Boolean,
) : Angular2Symbol, WebSymbolDeclaredInPsi {

  override val priority: WebSymbol.Priority
    // Match HTML elements and attributes priority
    get() = WebSymbol.Priority.LOW

  override val psiContext: PsiElement
    get() = myParent.psiParent

  override val sourceElement: PsiElement
    get() = myParent.psiParent

  override val project: Project
    get() = sourceElement.project

  override val qualifiedKind: WebSymbolQualifiedKind
    get() = if (isElementSelector) NG_DIRECTIVE_ELEMENT_SELECTORS else NG_DIRECTIVE_ATTRIBUTE_SELECTORS

  override val declaration: WebSymbolDeclaration?
    get() = if (referencedSymbol == null) super.declaration else null

  /**
   * Selectors should yield to HTML symbols and directive properties
   */
  val referencedSymbol: WebSymbol? by lazy(LazyThreadSafetyMode.PUBLICATION) {
    JSTypeEvaluationLocationProvider.assertLocationIsSet()
    getReferencedHtmlSymbol(name, sourceElement, isElementSelector, myElementSelector)
    ?: if (!isElementSelector) getReferencedDirectiveProperty(name, myParent) else null
  }

  override val presentation: TargetPresentation
    get() {
      val parent = myParent.psiParent
      val clazz = getClassForDecoratorElement(parent)
      return TargetPresentation.builder(name)
        .icon(icon)
        .locationText(parent.containingFile.name)
        .containerText(clazz?.name)
        .presentation()
    }

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
    Angular2ElementDocumentationTarget.create(
      name, location,
      Angular2EntitiesProvider.getEntity(sourceElement.contextOfType<TypeScriptClass>(true)))
    ?: super<Angular2Symbol>.getDocumentationTarget(location)

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(sourceElement) { referencedSymbol == symbol }
    || super<WebSymbolDeclaredInPsi>.isEquivalentTo(symbol)

  override fun createPointer(): Pointer<Angular2DirectiveSelectorSymbol> {
    val parent = myParent.createPointer()
    val range = textRangeInSourceElement
    val name = this.name
    val elementName = myElementSelector
    val isElement = isElementSelector
    return Pointer {
      val newParent = parent.dereference()
      if (newParent != null) Angular2DirectiveSelectorSymbol(newParent, range, name, elementName, isElement) else null
    }
  }

  override fun toString(): String {
    return (if (isElementSelector) "ElementDirectiveSelector" else "AttributeDirectiveSelector") + "<" + name + ">"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val symbol = other as Angular2DirectiveSelectorSymbol?
    return isElementSelector == symbol!!.isElementSelector &&
           myParent == symbol.myParent &&
           textRangeInSourceElement == symbol.textRangeInSourceElement &&
           name == symbol.name &&
           myElementSelector == symbol.myElementSelector
  }

  override fun hashCode(): Int {
    return Objects.hash(myParent, textRangeInSourceElement, name, myElementSelector, isElementSelector)
  }

  override fun validator(): RenameValidator {
    return MyRenameValidator(isElementSelector)
  }

  private fun getReferencedHtmlSymbol(name: String, sourceElement: PsiElement, isElementSelector: Boolean, elementSelector: String?):
    WebSymbolsHtmlQueryConfigurator.StandardHtmlSymbol? {

    val psiElement = sourceElement
    val nsDescriptor = getHtmlNSDescriptor(psiElement.project)
    if (nsDescriptor != null) {
      if (isElementSelector) {
        val elementDescriptor = nsDescriptor.getElementDescriptorByName(name)
        if (elementDescriptor != null) {
          return WebSymbolsHtmlQueryConfigurator.HtmlElementDescriptorBasedSymbol(elementDescriptor, null)
        }
      }
      else {
        var elementDescriptor: XmlElementDescriptor? = null
        var tagName = elementSelector
        if (elementSelector != null) {
          elementDescriptor = nsDescriptor.getElementDescriptorByName(elementSelector)
        }
        if (elementDescriptor == null) {
          elementDescriptor = nsDescriptor.getElementDescriptorByName("div")
          tagName = "div"
        }
        if (elementDescriptor != null) {
          val attributeDescriptor = elementDescriptor.getAttributeDescriptor(name, null)
          if (attributeDescriptor != null) {
            return WebSymbolsHtmlQueryConfigurator.HtmlAttributeDescriptorBasedSymbol(attributeDescriptor, tagName!!)
          }
        }
      }
    }
    return null
  }

  private fun getReferencedDirectiveProperty(name: @NlsSafe String, parent: Angular2DirectiveSelectorImpl): WebSymbol? {
    val directive = getClassForDecoratorElement(parent.psiParent)
                      ?.let { Angular2EntitiesProvider.getDirective(it) }
                    ?: return null
    return directive.inputs.find { it.name == name }
  }

  private class MyRenameValidator(private val myIsElement: Boolean) : RenameValidator {

    override fun validate(newName: String): RenameValidationResult {
      if (myIsElement) {
        return if (TAG_NAME_PATTERN.matcher(newName).matches())
          RenameValidationResult.ok()
        else
          RenameValidationResult.invalid(
            Angular2Bundle.message("angular.refactor.selector.invalid.html.element.name", newName))
      }
      return if (ATTRIBUTE_NAME_PATTERN.matcher(newName).matches())
        RenameValidationResult.ok()
      else
        RenameValidationResult.invalid(
          Angular2Bundle.message("angular.refactor.selector.invalid.html.attribute.name", newName))
    }
  }

  companion object {
    private val TAG_NAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9-]*")
    private val ATTRIBUTE_NAME_PATTERN = Pattern.compile("[^\\s\"'>/=\\p{Cntrl}]+")
  }
}
