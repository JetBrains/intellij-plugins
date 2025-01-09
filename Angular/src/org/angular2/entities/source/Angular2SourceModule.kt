// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result
import org.angular2.Angular2DecoratorUtil.getObjectLiteralInitializer
import org.angular2.Angular2DecoratorUtil.getReferencedObjectLiteralInitializer
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2Module
import org.angular2.entities.Angular2ModuleResolver
import org.angular2.entities.Angular2ResolvedSymbolsSet

class Angular2SourceModule(decorator: ES6Decorator, implicitElement: JSImplicitElement)
  : Angular2SourceEntity(decorator, implicitElement), Angular2Module {

  private val myModuleResolver = Angular2ModuleResolver({ decorator }, symbolCollector)

  override val declarations: Set<Angular2Declaration>
    get() = myModuleResolver.declarations

  override val imports: Set<Angular2Entity>
    get() = myModuleResolver.imports

  override val exports: Set<Angular2Entity>
    get() = myModuleResolver.exports

  override val allExportedDeclarations: Set<Angular2Declaration>
    get() = myModuleResolver.allExportedDeclarations

  override val isScopeFullyResolved: Boolean
    get() = myModuleResolver.isScopeFullyResolved

  override val isPublic: Boolean
    get() = !getName().startsWith("ɵ")

  override fun areExportsFullyResolved(): Boolean {
    return myModuleResolver.areExportsFullyResolved()
  }

  override fun areDeclarationsFullyResolved(): Boolean {
    return myModuleResolver.areDeclarationsFullyResolved()
  }

  override fun createPointer(): Pointer<out Angular2Module> {
    return createPointer { decorator, implicitElement ->
      Angular2SourceModule(decorator, implicitElement)
    }
  }

  private class SourceEntitiesCollector<T : Angular2Entity>(entityClass: Class<T>, decorator: ES6Decorator)
    : Angular2SourceSymbolCollectorBase<T, Angular2ResolvedSymbolsSet<T>>(entityClass, decorator) {

    private val myResult = HashSet<T>()

    override fun createResult(isFullyResolved: Boolean, dependencies: Set<PsiElement>): Result<Angular2ResolvedSymbolsSet<T>> =
      Angular2ResolvedSymbolsSet.createResult(myResult, isFullyResolved, dependencies)

    override fun processAcceptableEntity(entity: T) {
      myResult.add(entity)
    }

  }

  companion object {
    @JvmField
    val symbolCollector: Angular2ModuleResolver.SymbolCollector<ES6Decorator> = object : Angular2ModuleResolver.SymbolCollector<ES6Decorator> {
      override fun <U : Angular2Entity> collect(source: ES6Decorator,
                                                propertyName: String,
                                                symbolClazz: Class<U>): Result<Angular2ResolvedSymbolsSet<U>> {
        return collectSymbols(source, propertyName, symbolClazz)
      }
    }

    private fun <T : Angular2Entity> collectSymbols(decorator: ES6Decorator,
                                                    propertyName: String,
                                                    symbolClazz: Class<T>): Result<Angular2ResolvedSymbolsSet<T>> {
      val initializer = getObjectLiteralInitializer(decorator)
                        ?: getReferencedObjectLiteralInitializer(decorator)
      val property = initializer?.findProperty(propertyName)
      return SourceEntitiesCollector(symbolClazz, decorator).collect(property)
    }

  }
}
