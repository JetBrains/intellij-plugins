// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.angular2.web.Angular2Symbol

interface Angular2HostDirective {
  val directive: Angular2Directive?

  val inputs: Collection<Angular2DirectiveProperty>
    get() = bindings.inputs

  val outputs: Collection<Angular2DirectiveProperty>
    get() = bindings.outputs

  val inOuts: List<Angular2Symbol>
    get() = bindings.inOuts

  val bindings: Angular2DirectiveProperties

  val exportAs: Map<String, Angular2DirectiveExportAs>
    get() = directive?.let { directive ->
      CachedValuesManager.getManager(directive.sourceElement.project).getCachedValue(directive as UserDataHolder) {
        CachedValueProvider.Result.create(directive.exportAs, PsiModificationTracker.MODIFICATION_COUNT)
      }
    } ?: emptyMap()
}
