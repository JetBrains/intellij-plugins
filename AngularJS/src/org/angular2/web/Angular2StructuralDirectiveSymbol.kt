package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.navigation.NavigationTarget
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.refactoring.rename.symbol.RenameableSymbol
import com.intellij.webSymbols.*
import org.angular2.entities.Angular2Directive

open class Angular2StructuralDirectiveSymbol private constructor(private val directive: Angular2Directive,
                                                                 sourceSymbol: Angular2Symbol,
                                                                 private val hasInputsToBind: Boolean) :
  Angular2SymbolDelegate<Angular2Symbol>(sourceSymbol) {

  companion object {
    @JvmStatic
    fun create(directive: Angular2Directive,
               sourceSymbol: Angular2Symbol,
               hasInputsToBind: Boolean): Angular2StructuralDirectiveSymbol =
      when (sourceSymbol) {
        is PsiSourcedWebSymbol ->
          object : Angular2StructuralDirectiveSymbol(directive, sourceSymbol, hasInputsToBind), PsiSourcedWebSymbol {

            override val source: PsiElement?
              get() = (delegate as PsiSourcedWebSymbol).source

            override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
              super<Angular2StructuralDirectiveSymbol>.getNavigationTargets(project)

            override val psiContext: PsiElement?
              get() = super<Angular2StructuralDirectiveSymbol>.psiContext
          }
        is RenameableSymbol, is RenameTarget ->
          object : Angular2StructuralDirectiveSymbol(directive, sourceSymbol, hasInputsToBind), RenameableSymbol {
            override val renameTarget: RenameTarget
              get() = renameTargetFromDelegate()
          }
        else -> Angular2StructuralDirectiveSymbol(directive, sourceSymbol, hasInputsToBind)
      }
  }

  override val attributeValue: WebSymbolHtmlAttributeValue?
    get() = if (!hasInputsToBind)
      WebSymbolHtmlAttributeValue.create(required = false)
    else super.attributeValue

  override val priority: WebSymbol.Priority?
    get() = WebSymbol.Priority.HIGH

  override val namespace: SymbolNamespace
    get() = WebSymbol.NAMESPACE_JS

  override val kind: SymbolKind
    get() = Angular2WebSymbolsRegistryExtension.KIND_NG_STRUCTURAL_DIRECTIVES

  override val properties: Map<String, Any>
    get() = super.properties + Pair(Angular2WebSymbolsRegistryExtension.PROP_SYMBOL_DIRECTIVE, directive)

  override fun createPointer(): Pointer<Angular2StructuralDirectiveSymbol> {
    val directivePtr = directive.createPointer()
    val selectorPtr = delegate.createPointer()
    val hasInputsToBind = this.hasInputsToBind
    return Pointer {
      val directive = directivePtr.dereference() ?: return@Pointer null
      val selector = selectorPtr.dereference() ?: return@Pointer null
      create(directive, selector, hasInputsToBind)
    }
  }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is Angular2StructuralDirectiveSymbol
    && other.delegate == delegate

  override fun hashCode(): Int =
    delegate.hashCode()

}