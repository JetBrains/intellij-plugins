// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template

import com.intellij.psi.ResolveResult
import com.intellij.util.Processor
import java.util.*
import java.util.function.Consumer

abstract class Angular2TemplateScope
/**
 * A scope can be created with parent scope, which contents will be included in the resolution.
 * See [Angular2TemplateScope.resolveAllScopesInHierarchy]
 */
protected constructor(val parent: Angular2TemplateScope?) {
  private val children = ArrayList<Angular2TemplateScope>()

  init {
    @Suppress("LeakingThis")
    parent?.add(this)
  }

  fun getChildren(): List<Angular2TemplateScope> {
    return Collections.unmodifiableList(children)
  }

  private fun add(scope: Angular2TemplateScope) {
    this.children.add(scope)
  }

  /**
   * This method is called on every provided scope and allows for providing resolve results from enclosing scopes.
   */
  fun resolveAllScopesInHierarchy(processor: Processor<in ResolveResult>): Boolean {
    var scope: Angular2TemplateScope? = this
    var found = false
    val consumer = { resolveResult: ResolveResult ->
      if (!processor.process(resolveResult)) {
        found = true
      }
    }
    while (scope != null && !found) {
      scope.resolve(consumer)
      scope = scope.parent
    }
    return found
  }

  abstract fun resolve(consumer: Consumer<in ResolveResult>)
}
