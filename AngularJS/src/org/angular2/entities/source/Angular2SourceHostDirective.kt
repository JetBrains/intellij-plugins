// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveProperties
import org.angular2.entities.Angular2HostDirective

class Angular2SourceHostDirective(override val directive: Angular2Directive) : Angular2HostDirective {

  override val bindings: Angular2DirectiveProperties
    get() = CachedValuesManager.getManager(directive.sourceElement.project)
      .getCachedValue(directive as UserDataHolder) {
        CachedValueProvider.Result.create(
          Angular2DirectiveProperties(
            directive.hostDirectives.flatMap { it.inputs },
            directive.hostDirectives.flatMap { it.outputs }
          ),
          PsiModificationTracker.MODIFICATION_COUNT
        )
      }

  override fun equals(other: Any?): Boolean =
    other === this
    || (other is Angular2SourceHostDirective
        && other.directive == directive)

  override fun hashCode(): Int =
    directive.hashCode()

}