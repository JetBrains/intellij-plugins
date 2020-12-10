// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.model

import com.intellij.lang.javascript.psi.JSType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.util.text.SemVer
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStore

interface NuxtApplication {

  val project: Project

  val configFile: VirtualFile

  val config: NuxtConfig

  val packageJson: VirtualFile?

  val sourceDir: VirtualFile?

  val nuxtVersion: SemVer?

  val vuexStore: VuexStore?

  val staticResourcesDir: PsiDirectory?

  fun getNuxtConfigType(context: PsiElement): JSType?

}