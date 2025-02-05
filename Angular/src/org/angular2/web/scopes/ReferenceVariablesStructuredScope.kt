package org.angular2.web.scopes

import com.intellij.javascript.webSymbols.symbols.asWebSymbol
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.JS_SYMBOLS
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.query.WebSymbolsCompoundScope
import com.intellij.webSymbols.utils.WebSymbolsStructuredScope
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType.REFERENCE
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementVisitor
import org.angular2.lang.html.psi.Angular2HtmlReference

class ReferenceVariablesStructuredScope(location: PsiElement) : WebSymbolsStructuredScope<PsiElement, PsiFile>(location) {

  override val rootPsiElement: PsiFile?
    get() = location.containingFile

  override val scopesBuilderProvider: (PsiFile, WebSymbolsPsiScopesHolder) -> PsiElementVisitor?
    get() = { _, holder -> ReferenceVariablesStructuredScopeVisitor(holder) }

  override val providedSymbolKinds: Set<WebSymbolQualifiedKind> = PROVIDED_SYMBOL_KINDS

  fun flattenSymbols(resolveToMultipleSymbols: Boolean): List<WebSymbol> {
    val rootScope = getRootScope() ?: return emptyList()
    val result = mutableListOf<WebSymbol>()
    if (resolveToMultipleSymbols) {
      val stack = mutableListOf(rootScope)
      while (!stack.isEmpty()) {
        val scope = stack.removeLast()
        result.addAll(scope.localSymbols)
        stack.addAll(scope.children)
      }
    }
    else {
      val stack = mutableListOf(Pair(rootScope, mutableSetOf<String>()))
      while (!stack.isEmpty()) {
        val (scope, names) = stack.removeLast()
        scope.localSymbols.forEach {
          val symbolName = it.name
          if (names.add(symbolName)) {
            result.add(it)
          }
        }
        scope.children.forEach {
          stack.add(Pair(it, names.toMutableSet()))
        }
      }
    }
    return result
  }

  override fun createPointer(): Pointer<out WebSymbolsCompoundScope> {
    val locationPtr = location.createSmartPointer()
    return Pointer {
      val location = locationPtr.dereference() ?: return@Pointer null
      ReferenceVariablesStructuredScope(location)
    }
  }

  private class ReferenceVariablesStructuredScopeVisitor(private val holder: WebSymbolsPsiScopesHolder) : Angular2HtmlRecursiveElementVisitor() {

    override fun visitXmlTag(tag: XmlTag) {
      val isTemplateTag = tag.children.any { it is XmlAttribute && it.name.startsWith("*") }
                          || isTemplateTag(tag)
      if (isTemplateTag) {
        holder.pushScope(tag)
      }
      super.visitXmlTag(tag)
      if (isTemplateTag) {
        holder.popScope()
      }
    }

    override fun visitBlock(block: Angular2HtmlBlock) {
      if (block.isPrimary) {
        sequenceOf(block).plus(block.blockSiblingsForward()).forEach {
          holder.pushScope(it)
          it.acceptChildren(this)
          holder.popScope()
        }
      }
    }

    override fun visitXmlAttribute(attribute: XmlAttribute) {
      val parent = attribute.parent
                   ?: return
      val info = Angular2AttributeNameParser.parse(attribute.name, parent)
      when (info.type) {
        REFERENCE -> addReference(attribute, info, isTemplateTag(parent))
        else -> {}
      }
    }

    fun addReference(
      attribute: XmlAttribute,
      info: Angular2AttributeNameParser.AttributeInfo,
      isTemplateTag: Boolean,
    ) {
      val `var` = if (attribute is Angular2HtmlReference) {
        attribute.variable?.asWebSymbol() ?: return
      }
      else {
        JSImplicitElementImpl.Builder(info.name, attribute)
          .setType(JSImplicitElement.Type.Variable)
          .setProperties(JSImplicitElement.Property.Constant)
          .toImplicitElement()
          .asWebSymbol()
      }
      if (isTemplateTag) {
        // References on ng-template are visible within parent scope
        holder.previousScope {
          addSymbol(`var`)
        }
      }
      else {
        holder.currentScope {
          addSymbol(`var`)
        }
      }
    }
  }


  companion object {
    private val PROVIDED_SYMBOL_KINDS: Set<WebSymbolQualifiedKind> = setOf(JS_SYMBOLS)
  }

}