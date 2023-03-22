// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.model.Pointer
import org.angular2.entities.Angular2EntityUtils.forEachEntity
import org.angular2.entities.Angular2EntityUtils.forEachModule

interface Angular2Module : Angular2Entity, Angular2ImportsOwner {

  val declarations: Set<Angular2Declaration>

  val exports: Set<Angular2Entity>

  val isPublic: Boolean

  val allExportedDeclarations: Set<Angular2Declaration>

  override val declarationsInScope: Set<Angular2Declaration>
    get() {
      val result = HashSet(declarations)
      forEachEntity(
        imports,
        { module -> result.addAll(module.allExportedDeclarations) },
        { declaration -> if (declaration.isStandalone) result.add(declaration) }
      )
      forEachModule(exports) { module -> result.addAll(module.allExportedDeclarations) }
      return result
    }

  fun areDeclarationsFullyResolved(): Boolean

  fun areExportsFullyResolved(): Boolean

  override fun createPointer(): Pointer<out Angular2Module>

  companion object {

    val EMPTY_ARRAY = arrayOfNulls<Angular2Module>(0)
  }
}
