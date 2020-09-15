// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.index

import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy
import org.jetbrains.vuejs.libraries.nuxt.NUXT_CONFIG_FILE

class NuxtIndexExcludePolicy(val project: Project) : DirectoryIndexExcludePolicy {

  override fun getExcludeUrlsForProject(): Array<String> {
    return PackageJsonFileManager.getInstance(project)
      .validPackageJsonFiles
      .asSequence()
      .mapNotNull { it.parent }
      .filter { it.findChild(NUXT_CONFIG_FILE) != null }
      .map { "${it.url}/.nuxt" }
      .toList()
      .toTypedArray()
  }
}