// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageHandler
import com.intellij.html.webSymbols.WebSymbolsHtmlQueryConfigurator
import com.intellij.html.webSymbols.WebSymbolsHtmlQueryConfigurator.Companion.getHtmlNSDescriptor
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.model.Pointer
import com.intellij.navigation.NavigationItem
import com.intellij.navigation.NavigationRequest
import com.intellij.navigation.NavigationTarget
import com.intellij.navigation.TargetPresentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.refactoring.rename.api.RenameValidationResult
import com.intellij.refactoring.rename.api.RenameValidator
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolOrigin
import com.intellij.webSymbols.utils.createPsiRangeNavigationItem
import com.intellij.xml.XmlElementDescriptor
import org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement
import org.angular2.entities.impl.TypeScriptElementDocumentationTarget
import org.angular2.web.Angular2Symbol
import org.angular2.web.Angular2SymbolOrigin
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_ELEMENT_SELECTORS
import org.angularjs.AngularJSBundle
import java.util.*
import java.util.regex.Pattern

class Angular2DirectiveSelectorSymbol(private val myParent: Angular2DirectiveSelectorImpl,
                                      val textRangeInSource: TextRange,
                                      override val name: @NlsSafe String,
                                      private val myElementSelector: String?,
                                      val isElementSelector: Boolean) : Angular2Symbol, SearchTarget, RenameTarget {

  override val priority: WebSymbol.Priority
    get() = WebSymbol.Priority.LOWEST

  override val psiContext: PsiElement
    get() = myParent.psiParent

  val source: PsiElement
    get() = myParent.psiParent

  override val project: Project
    get() = source.project

  override val namespace: String
    get() = WebSymbol.NAMESPACE_JS

  override val kind: String
    get() = if (isElementSelector) KIND_NG_DIRECTIVE_ELEMENT_SELECTORS else KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS

  override val origin: WebSymbolOrigin
    get() = Angular2SymbolOrigin(this)

  val isDeclaration: Boolean
    get() = referencedSymbols.all { it is Angular2Symbol }

  val referencedSymbols: List<WebSymbol>
    get() {
      val psiElement = source
      val nsDescriptor = getHtmlNSDescriptor(psiElement.project)
      if (nsDescriptor != null) {
        if (isElementSelector) {
          val elementDescriptor = nsDescriptor.getElementDescriptorByName(name)
          if (elementDescriptor != null) {
            return listOf<WebSymbol>(WebSymbolsHtmlQueryConfigurator.HtmlElementDescriptorBasedSymbol(elementDescriptor, null))
          }
        }
        else {
          var elementDescriptor: XmlElementDescriptor? = null
          var tagName = myElementSelector
          if (myElementSelector != null) {
            elementDescriptor = nsDescriptor.getElementDescriptorByName(myElementSelector)
          }
          if (elementDescriptor == null) {
            elementDescriptor = nsDescriptor.getElementDescriptorByName("div")
            tagName = "div"
          }
          if (elementDescriptor != null) {
            val attributeDescriptor = elementDescriptor.getAttributeDescriptor(name, null)
            if (attributeDescriptor != null) {
              return listOf<WebSymbol>(WebSymbolsHtmlQueryConfigurator.HtmlAttributeDescriptorBasedSymbol(attributeDescriptor, tagName!!))
            }
          }
        }
      }
      return listOf<WebSymbol>(this)
    }

  val textOffset: Int
    get() = myParent.psiParent.textOffset + textRangeInSource.startOffset

  val isAttributeSelector: Boolean
    get() = !isElementSelector

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

  override val usageHandler: UsageHandler
    get() = UsageHandler.createEmptyUsageHandler(name)

  override val targetName: String
    get() = name

  override val maximalSearchScope: SearchScope?
    get() = super<SearchTarget>.maximalSearchScope

  override fun presentation(): TargetPresentation {
    return presentation
  }

  override fun getDocumentationTarget(): DocumentationTarget {
    val clazz = PsiTreeUtil.getContextOfType(source, TypeScriptClass::class.java)
                ?: return super.getDocumentationTarget()
    return TypeScriptElementDocumentationTarget(name, clazz)
  }

  override fun createPointer(): Pointer<Angular2DirectiveSelectorSymbol> {
    val parent = myParent.createPointer()
    val range = textRangeInSource
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

  override fun getNavigationTargets(project: Project): Collection<DirectiveSelectorSymbolNavigationTarget> {
    return listOf(DirectiveSelectorSymbolNavigationTarget(this))
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val symbol = other as Angular2DirectiveSelectorSymbol?
    return isElementSelector == symbol!!.isElementSelector &&
           myParent == symbol.myParent &&
           textRangeInSource == symbol.textRangeInSource &&
           name == symbol.name &&
           myElementSelector == symbol.myElementSelector
  }

  override fun hashCode(): Int {
    return Objects.hash(myParent, textRangeInSource, name, myElementSelector, isElementSelector)
  }

  override fun validator(): RenameValidator {
    return MyRenameValidator(isElementSelector)
  }

  class DirectiveSelectorSymbolNavigationTarget(private val mySymbol: Angular2DirectiveSelectorSymbol) : NavigationTarget {

    override fun createPointer(): Pointer<out NavigationTarget> {
      return Pointer.delegatingPointer(
        mySymbol.createPointer(),
        DirectiveSelectorSymbolNavigationTarget::class.java
      ) { DirectiveSelectorSymbolNavigationTarget(it) }
    }

    override fun presentation(): TargetPresentation {
      return mySymbol.presentation
    }

    fun getNavigationItem(): NavigationItem? {
      return createPsiRangeNavigationItem(mySymbol.source, mySymbol.textRangeInSource.startOffset) as? NavigationItem
    }

    override fun navigationRequest(): NavigationRequest? {
      return getNavigationItem()?.navigationRequest()
    }
  }

  private class MyRenameValidator(private val myIsElement: Boolean) : RenameValidator {

    override fun validate(newName: String): RenameValidationResult {
      if (myIsElement) {
        return if (TAG_NAME_PATTERN.matcher(newName).matches())
          RenameValidationResult.ok()
        else
          RenameValidationResult.invalid(
            AngularJSBundle.message("angularjs.refactoring.selector.invalid.html.element.name", newName))
      }
      return if (ATTRIBUTE_NAME_PATTERN.matcher(newName).matches())
        RenameValidationResult.ok()
      else
        RenameValidationResult.invalid(
          AngularJSBundle.message("angularjs.refactoring.selector.invalid.html.attribute.name", newName))
    }
  }

  companion object {

    private val TAG_NAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9-]*")
    private val ATTRIBUTE_NAME_PATTERN = Pattern.compile("[^\\s\"'>/=\\p{Cntrl}]+")
  }
}
