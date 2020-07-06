// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.model.impl

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import org.jetbrains.vuejs.libraries.nuxt.model.NuxtApplication
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStore

class NuxtApplicationImpl(private val configFile: VirtualFile, private val project: Project) : NuxtApplication {

  override fun getVuexStore(): VuexStore? =
    configFile.parent.findChild("store")?.let {
      PsiManager.getInstance(project).findDirectory(it)
    }?.let { NuxtVuexStore(it) }

  override fun getStaticResourcesDir(): PsiDirectory? =
    configFile.parent.findChild("static")?.let {
      PsiManager.getInstance(project).findDirectory(it)
    }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    return other is NuxtApplicationImpl
           && configFile == other.configFile
           && project == other.project
  }

  override fun hashCode(): Int {
    var result = configFile.hashCode()
    result = 31 * result + project.hashCode()
    return result
  }


}