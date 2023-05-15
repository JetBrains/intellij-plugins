// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.model.Pointer
import org.angular2.entities.Angular2EntitiesProvider.getDeclarationToModuleMap
import java.util.*

/**
 * Also known as [Declarable](https://angular.io/guide/glossary#declarable).
 *
 *
 * Common interface of entities that can be added to `@NgModule.declarations` array (only if not standalone).
 */
interface Angular2Declaration : Angular2Entity {

  val isStandalone: Boolean

  val allDeclaringModules: Collection<Angular2Module>
    get() =
      if (isStandalone)
        emptyList()
      else
        Collections.unmodifiableCollection(getDeclarationToModuleMap(sourceElement.project).get(this))

  override fun createPointer(): Pointer<out Angular2Declaration>
}
