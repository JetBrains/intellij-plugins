// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.vuejs.index.GLOBAL
import org.jetbrains.vuejs.index.VueMixinBindingIndex
import org.jetbrains.vuejs.index.resolve
import org.jetbrains.vuejs.model.source.VueComponents.Companion.vueMixinDescriptorFinder

class VueGlobalMixinComponentDetailsProvider : VueAdvancedComponentDetailsProvider {
  override fun getIndexedData(descriptor: JSObjectLiteralExpression?, project: Project): Collection<JSImplicitElement> {
    return resolve(GLOBAL, GlobalSearchScope.projectScope(project), VueMixinBindingIndex.KEY) ?: emptyList()
  }

  override fun getDescriptorFinder(): (JSImplicitElement) -> JSObjectLiteralExpression? {
    return { vueMixinDescriptorFinder(it) }
  }
}
