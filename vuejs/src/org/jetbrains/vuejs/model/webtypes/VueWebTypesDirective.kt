// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.openapi.project.Project
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.webtypes.json.Attribute_

class VueWebTypesDirective(attribute: Attribute_,
                           project: Project,
                           parent: VueEntitiesContainer,
                           sourceSymbolResolver: WebTypesSourceSymbolResolver)
  : VueWebTypesSourceEntity(project, attribute.source, sourceSymbolResolver), VueDirective {

  override val parents: List<VueEntitiesContainer> = listOf(parent)
  override val defaultName: String? = attribute.name
}
