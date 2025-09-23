// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.ContainerUtil

interface AngularConfigProvider {

  fun findAngularConfig(project: Project, context: VirtualFile): AngularConfig?

  companion object {

    private val EP_NAME = ExtensionPointName.create<AngularConfigProvider>("org.angular2.configProvider")

    fun findAngularProject(project: Project, context: VirtualFile): AngularProject? {
      val config = findAngularConfig(project, context) ?: return null
      val configFile = PsiManager.getInstance(project).findFile(config.file)
                       ?: return config.getProject(context)
      return CachedValuesManager.getCachedValue(configFile) {
        CachedValueProvider.Result.create(ContainerUtil.createConcurrentWeakMap<VirtualFile, AngularProject>(),
                                          configFile, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
      }.computeIfAbsent(context) {
        config.getProject(it)
      }
    }

    fun findAngularConfig(project: Project, context: VirtualFile): AngularConfig? =
      EP_NAME.extensionList.firstNotNullOfOrNull { it.findAngularConfig(project, context) }

  }
}
