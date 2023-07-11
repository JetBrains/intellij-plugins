// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.model.Pointer
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.refactoring.suggested.createSmartPointer
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2Module
import org.angular2.entities.Angular2ModuleResolver
import org.angular2.entities.Angular2ResolvedSymbolsSet
import org.angular2.entities.metadata.stubs.Angular2MetadataModuleStub

class Angular2MetadataModule(element: Angular2MetadataModuleStub) : Angular2MetadataEntity<Angular2MetadataModuleStub>(
  element), Angular2Module {

  private val myModuleResolver = Angular2ModuleResolver({ this }, symbolCollector)

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
    get() = stub.memberName == null || !stub.memberName!!.startsWith("ɵ")

  override fun areExportsFullyResolved(): Boolean {
    return myModuleResolver.areExportsFullyResolved()
  }

  override fun areDeclarationsFullyResolved(): Boolean {
    return myModuleResolver.areDeclarationsFullyResolved()
  }

  override fun createPointer(): Pointer<out Angular2Module> {
    return this.createSmartPointer()
  }

  companion object {

    private val symbolCollector = object : Angular2ModuleResolver.SymbolCollector<Angular2MetadataModule> {
      override fun <U : Angular2Entity> collect(source: Angular2MetadataModule,
                                                propertyName: String,
                                                symbolClazz: Class<U>): Result<Angular2ResolvedSymbolsSet<U>> =
        collectSymbols(source, propertyName, symbolClazz)
    }

    private fun <T : Angular2Entity> collectSymbols(source: Angular2MetadataModule,
                                                    propertyName: String,
                                                    entityClass: Class<T>): Result<Angular2ResolvedSymbolsSet<T>> {
      val propertyStub = source.stub.getDecoratorFieldValueStub(propertyName)
                         ?: return Angular2ResolvedSymbolsSet.createResult(emptySet(), true, source)
      val allResolved = Ref(true)
      val result = HashSet<T>()
      val cacheDependencies = HashSet<PsiElement>()
      collectReferencedElements(propertyStub.psi, { element ->
        if (element != null && entityClass.isAssignableFrom(element.javaClass)) {
          result.add(entityClass.cast(element))
        }
        else {
          allResolved.set(false)
        }
      }, cacheDependencies)
      return Angular2ResolvedSymbolsSet.createResult(result, allResolved.get(), cacheDependencies)
    }
  }
}
