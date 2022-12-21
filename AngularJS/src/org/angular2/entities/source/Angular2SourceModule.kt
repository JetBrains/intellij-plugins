// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.util.AstLoadingFilter
import com.intellij.util.containers.Stack
import org.angular2.Angular2DecoratorUtil.getObjectLiteralInitializer
import org.angular2.Angular2DecoratorUtil.getReferencedObjectLiteralInitializer
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2Module
import org.angular2.entities.Angular2ModuleResolver
import org.angular2.entities.Angular2ModuleResolver.ResolvedEntitiesList

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

  private class SourceSymbolCollector<T : Angular2Entity>(entityClass: Class<T>, private val myDecorator: ES6Decorator)
    : Angular2SourceEntityListProcessor<T>(entityClass) {

    private var myIsFullyResolved = true
    private val myResult = HashSet<T>()
    private val myDependencies = HashSet<PsiElement>()
    private val myResolveQueue = Stack<PsiElement>()

    fun collect(value: JSExpression?): Result<ResolvedEntitiesList<T>> {
      if (value == null) {
        return ResolvedEntitiesList.createResult(myResult, false, myDecorator)
      }
      val visited = HashSet<PsiElement>()
      processCacheDependency(myDecorator)
      myResolveQueue.push(value)
      while (!myResolveQueue.empty()) {
        ProgressManager.checkCanceled()
        val element = myResolveQueue.pop()
        if (!visited.add(element)) {
          // Protect against cyclic references or visiting same thing several times
          continue
        }
        processCacheDependency(element)
        val children = resolve(element)
        if (children.isEmpty()) {
          element.accept(resultsVisitor)
        }
        else {
          myResolveQueue.addAll(children)
        }
      }
      return ResolvedEntitiesList.createResult(myResult, myIsFullyResolved, myDependencies)
    }

    override fun processCacheDependency(element: PsiElement) {
      myDependencies.add(element)
    }

    override fun processNonAcceptableEntityClass(aClass: JSClass) {
      myIsFullyResolved = false
    }

    override fun processAcceptableEntity(entity: T) {
      myResult.add(entity)
    }

    override fun processAnyType() {
      myIsFullyResolved = false
    }

    override fun processAnyElement(node: JSElement) {
      myIsFullyResolved = false
    }
  }

  companion object {
    @JvmField
    val symbolCollector = object : Angular2ModuleResolver.SymbolCollector<ES6Decorator> {
      override fun <U : Angular2Entity> collect(source: ES6Decorator,
                                                propertyName: String,
                                                symbolClazz: Class<U>): Result<ResolvedEntitiesList<U>> {
        return collectSymbols(source, propertyName, symbolClazz)
      }
    }

    private fun <T : Angular2Entity> collectSymbols(decorator: ES6Decorator,
                                                    propertyName: String,
                                                    symbolClazz: Class<T>): Result<ResolvedEntitiesList<T>> {
      val initializer = getObjectLiteralInitializer(decorator)
                        ?: getReferencedObjectLiteralInitializer(decorator)
      val property = initializer?.findProperty(propertyName)
      return if (property == null) {
        ResolvedEntitiesList.createResult(emptySet(), true, decorator)
      }
      else AstLoadingFilter.forceAllowTreeLoading<Result<ResolvedEntitiesList<T>>, RuntimeException>(property.containingFile) {
        SourceSymbolCollector(symbolClazz, decorator).collect(property.value)
      }
    }

  }
}
