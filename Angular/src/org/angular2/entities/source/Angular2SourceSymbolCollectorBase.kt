// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.util.AstLoadingFilter
import com.intellij.util.containers.Stack
import org.angular2.entities.Angular2Entity

internal abstract class Angular2SourceSymbolCollectorBase<T : Angular2Entity, R>(
  entityClass: Class<T>, private val myDecorator: ES6Decorator
) : Angular2SourceEntityListProcessor<T>(entityClass) {

  private var myIsFullyResolved = true
  private val myDependencies = HashSet<PsiElement>()
  private val myResolveQueue = Stack<PsiElement>()

  fun collect(property: JSProperty?): Result<R> =
    if (property == null)
      createResult(true, setOf(myDecorator))
    else
      AstLoadingFilter.forceAllowTreeLoading<Result<R>, RuntimeException>(property.containingFile) {
        collect(property.value)
      }

  private fun collect(value: JSExpression?): Result<R> {
    if (value == null) {
      return createResult(false, setOf(myDecorator))
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
    return createResult(myIsFullyResolved, myDependencies)
  }

  protected abstract fun createResult(isFullyResolved: Boolean, dependencies: Set<PsiElement>): Result<R>

  final override fun processCacheDependency(element: PsiElement) {
    myDependencies.add(element)
  }

  override fun processNonAcceptableEntityClass(aClass: JSClass) {
    myIsFullyResolved = false
  }

  override fun processAnyType() {
    myIsFullyResolved = false
  }

  override fun processAnyElement(node: JSElement) {
    myIsFullyResolved = false
  }
}