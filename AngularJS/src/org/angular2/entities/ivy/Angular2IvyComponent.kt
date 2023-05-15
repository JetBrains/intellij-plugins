// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import com.intellij.model.Pointer
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2DirectiveKind
import org.angular2.entities.Angular2DirectiveSelector

class Angular2IvyComponent(entityDef: Angular2IvySymbolDef.Component) : Angular2IvyDirective(entityDef), Angular2Component {

  override val templateFile: PsiFile?
    get() = null

  override val directiveKind: Angular2DirectiveKind
    get() = Angular2DirectiveKind.REGULAR

  override val cssFiles: List<PsiFile>
    get() = emptyList()

  // Try to fallback to metadata JSON information - Angular 9.0.x case
  override val ngContentSelectors: List<Angular2DirectiveSelector>
    get() {
      val result = getNullableLazyValue(IVY_NG_CONTENT_SELECTORS) {
        (myEntityDef as Angular2IvySymbolDef.Component)
          .ngContentSelectors
          ?.map { createSelectorFromStringLiteralType(it) }
      }
      return result
             ?: (getMetadataDirective(typeScriptClass) as? Angular2Component)
               ?.ngContentSelectors
             ?: emptyList()
    }

  override fun createPointer(): Pointer<Angular2IvyComponent> {
    val entityDef = myEntityDef.createPointer()
    return Pointer {
      val newEntityDef = entityDef.dereference() as? Angular2IvySymbolDef.Component
      if (newEntityDef != null) Angular2IvyComponent(newEntityDef) else null
    }
  }

  companion object {

    private val IVY_NG_CONTENT_SELECTORS = Key<List<Angular2DirectiveSelector>?>("ng.ivy.content-selectors")
  }
}
