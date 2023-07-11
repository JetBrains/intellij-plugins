// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveProperties
import org.angular2.entities.Angular2HostDirective
import org.angular2.entities.source.Angular2SourceHostDirectiveWithMappings.Companion.createHostDirectiveProperties

class Angular2IvyHostDirective(override val directive: Angular2Directive,
                               private val inputsMap: Map<String, String>,
                               private val outputsMap: Map<String, String>) : Angular2HostDirective {
  override val bindings: Angular2DirectiveProperties
    get() = CachedValuesManager.getManager(directive.sourceElement.project).getCachedValue(directive as UserDataHolder) {
      CachedValueProvider.Result.create(createHostDirectiveProperties(directive, inputsMap, outputsMap),
                                        PsiModificationTracker.MODIFICATION_COUNT)
    }

}