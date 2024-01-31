// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface AngularConfigProvider {

  fun findAngularConfig(project: Project, context: VirtualFile): AngularConfig?

  companion object {

    private val EP_NAME = ExtensionPointName.create<AngularConfigProvider>("org.angular2.configProvider")

    fun findAngularProject(project: Project, context: VirtualFile): AngularProject? {
      return findAngularConfig(project, context)?.getProject(context)
    }

    fun findAngularConfig(project: Project, context: VirtualFile): AngularConfig? =
      EP_NAME.extensionList.firstNotNullOfOrNull { it.findAngularConfig(project, context) }

  }
}
