// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.index

import com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy
import org.jetbrains.vuejs.libraries.nuxt.NUXT_CONFIG_FILE

class NuxtIndexExcludePolicy(val project: Project) : DirectoryIndexExcludePolicy {

  override fun getExcludeUrlsForProject(): Array<String> {
    val manager = NodeModulesDirectoryManager.getInstance(project)
    return manager.computeFileIndexExtensionPreventingRecursion {
      // We cannot use index to search for nuxt config files here,
      // so we workaround by checking for nuxt config next to node_modules
      manager.nodeModulesDirectories
        .mapNotNull { it.nodeModulesDir.parent }
        .filter { it.findChild(NUXT_CONFIG_FILE) != null }
        .map { it.url + "/.nuxt" }
        .toTypedArray()
    } ?: emptyArray()
  }

}