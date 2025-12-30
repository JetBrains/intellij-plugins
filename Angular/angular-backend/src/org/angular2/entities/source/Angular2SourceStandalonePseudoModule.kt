package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.model.Pointer
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider.Result
import org.angular2.Angular2DecoratorUtil.EXPORTS_PROP
import org.angular2.entities.*
import org.angular2.lang.Angular2Bundle

/**
 * A special class to handle pseudo-modules for standalone components. E.g.:
 *
 * ```typescript
 * export declare const foo = readonly [typeof MyComponent, typeof MyDirective]
 * export declare const foo = [typeof MyComponent, typeof MyDirective]
 * export const foo = [MyComponent, MyDirective] as const
 * export const foo = [MyComponent, MyDirective]
 * ```
 */
class Angular2SourceStandalonePseudoModule(override val sourceElement: TypeScriptVariable) : Angular2Module {

  private val myModuleResolver = Angular2ModuleResolver({ sourceElement }, symbolCollector)

  override val declarations: Set<Angular2Declaration>
    get() = myModuleResolver.declarations

  override val exports: Set<Angular2Entity>
    get() = myModuleResolver.exports

  override val isPublic: Boolean
    get() = true

  override val isStandalonePseudoModule: Boolean
    get() = true

  override val allExportedDeclarations: Set<Angular2Declaration>
    get() = myModuleResolver.allExportedDeclarations

  override fun areDeclarationsFullyResolved(): Boolean =
    myModuleResolver.areDeclarationsFullyResolved()

  override fun areExportsFullyResolved(): Boolean =
    myModuleResolver.areExportsFullyResolved()

  override fun getName(): String =
    sourceElement.name ?: Angular2Bundle.message("angular.description.unnamed")

  override val entitySource: PsiElement
    get() = sourceElement

  override val entitySourceName: String
    get() = getName()

  override val entityJsType: JSType?
    get() = sourceElement.jsType

  override val isModifiable: Boolean
    get() = false

  override val imports: Set<Angular2Entity>
    get() = emptySet()

  override val isScopeFullyResolved: Boolean
    get() = myModuleResolver.isScopeFullyResolved

  override fun createPointer(): Pointer<Angular2SourceStandalonePseudoModule> {
    val varPtr = sourceElement.createSmartPointer()
    return Pointer {
      varPtr.dereference()?.let { Angular2SourceStandalonePseudoModule(it) }
    }
  }

  private class SourceEntitiesCollector<T : Angular2Entity>(entityClass: Class<T>, source: TypeScriptVariable)
    : Angular2SourceSymbolCollectorBase<T, Angular2ResolvedSymbolsSet<T>>(entityClass, source) {

    private val myResult = HashSet<T>()

    override fun createResult(isFullyResolved: Boolean, dependencies: Set<PsiElement>): Result<Angular2ResolvedSymbolsSet<T>> =
      Angular2ResolvedSymbolsSet.createResult(myResult, isFullyResolved, dependencies)

    override fun processAcceptableEntity(entity: T) {
      myResult.add(entity)
    }

  }

  companion object {
    private val symbolCollector = object : Angular2ModuleResolver.SymbolCollector<TypeScriptVariable> {
      override fun <U : Angular2Entity> collect(source: TypeScriptVariable,
                                                propertyName: String,
                                                symbolClazz: Class<U>): Result<Angular2ResolvedSymbolsSet<U>> {
        return collectSymbols(source, propertyName, symbolClazz)
      }
    }

    private fun <T : Angular2Entity> collectSymbols(source: TypeScriptVariable,
                                                    propertyName: String,
                                                    symbolClazz: Class<T>): Result<Angular2ResolvedSymbolsSet<T>> {
      if (propertyName != EXPORTS_PROP) {
        return Angular2ResolvedSymbolsSet.createResult(emptySet(), true, ModificationTracker.NEVER_CHANGED)
      }
      return SourceEntitiesCollector(symbolClazz, source).collect(source)
    }

  }

}