// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.model.Pointer
import com.intellij.psi.PsiFile
import org.angular2.entities.Angular2EntityUtils.forEachEntity

interface Angular2Component : Angular2Directive, Angular2ImportsOwner {

  val templateFile: PsiFile?

  val cssFiles: List<PsiFile>

  val ngContentSelectors: List<Angular2DirectiveSelector>

  override val isComponent: Boolean
    get() = true

  override val imports: Set<Angular2Entity>
    get() = emptySet()

  override val declarationsInScope: Set<Angular2Declaration>
    get() {
      val result = HashSet<Angular2Declaration>()
      result.add(this)
      forEachEntity(
        imports,
        { module -> result.addAll(module.allExportedDeclarations) },
        { declaration -> if (declaration.isStandalone) result.add(declaration) }
      )
      return result
    }

  override fun createPointer(): Pointer<out Angular2Component>
}
