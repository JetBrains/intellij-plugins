// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueGlobal
import org.jetbrains.vuejs.model.VuePlugin
import org.jetbrains.vuejs.model.webtypes.json.WebTypes

class VueWebTypesPlugin(project: Project, packageJson: VirtualFile?,
                        webTypes: WebTypes, owner: VueEntitiesContainer)
  : VueWebTypesEntitiesContainer(project, packageJson, webTypes, owner), VuePlugin {

  override val global: VueGlobal? = null
  override val moduleName: String? = null

}
