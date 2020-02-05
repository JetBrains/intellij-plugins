// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.template

import com.intellij.psi.ResolveResult
import com.intellij.util.Processor
import java.util.*
import java.util.function.Consumer

abstract class VueTemplateScope
/**
 * A scope can be created with parent scope, which contents will be included in the resolution.
 * See [VueTemplateScope.processAllScopesInHierarchy]
 */
protected constructor(val parent: VueTemplateScope?) {
  protected val children = ArrayList<VueTemplateScope>()

  init {
    @Suppress("LeakingThis")
    parent?.add(this)
  }

  private fun add(scope: VueTemplateScope) {
    this.children.add(scope)
  }

  /**
   * This method is called on every provided scope and allows for providing resolve results from enclosing scopes.
   */
  fun processAllScopesInHierarchy(processor: Processor<in ResolveResult>): Boolean {
    var scope: VueTemplateScope? = this
    while (scope != null) {
      if (!scope.process(processor)) {
        return false
      }
      scope = scope.parent
    }
    return true
  }

  abstract fun resolve(consumer: Consumer<in ResolveResult>)

  open fun process(processor: Processor<in ResolveResult>): Boolean {
    var found = false
    resolve(Consumer { resolveResult ->
      if (!processor.process(resolveResult)) {
        found = true
      }
    })
    return !found
  }

}
