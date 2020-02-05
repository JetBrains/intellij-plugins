// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.testFramework.LightVirtualFileBase
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage

private val VUE_CONTEXT_KEY: Key<CachedValue<Boolean>> = Key("vue.isContext")

fun isVueContext(context: PsiElement): Boolean {
  if (!context.isValid) {
    return false
  }
  if (context is PsiDirectory) {
    return isVueContext(context)
  }
  val psiFile = InjectedLanguageManager.getInstance(context.project).getTopLevelFile(context) ?: return false
  if (psiFile.language == VueLanguage.INSTANCE) return true
  val file = psiFile.originalFile.virtualFile
  @Suppress("DEPRECATION")
  return isVueContext(if (file != null && file.isInLocalFileSystem) file
                      else psiFile.project.baseDir ?: return false,
                      psiFile.project)
}

fun isVueContext(context: VirtualFile, project: Project): Boolean {
  // TODO merge with Angular
  var file: VirtualFile? = context
  while (file is LightVirtualFileBase) {
    file = file.originalFile
  }
  val psiDir = file?.parent
                 ?.let { if (it.isValid) PsiManager.getInstance(project).findDirectory(it) else null }
               ?: return false
  return isVueContext(psiDir)
}

private fun isVueContext(directory: PsiDirectory): Boolean {
  return CachedValuesManager.getCachedValue(directory, VUE_CONTEXT_KEY) {
    isVueContextFromProviders(directory)
  }
}

fun enableVueTSService(project: Project): Boolean {
  if (DumbService.isDumb(project)) return false

  return CachedValuesManager.getManager(project).getCachedValue(project) {
    @Suppress("DEPRECATION")
    val fromProviders =
      project.baseDir
        ?.let { PsiManager.getInstance(project).findDirectory(it) }
        ?.let { isVueContextFromProviders(it) }
    if (fromProviders?.value == true) {
      fromProviders
    }
    else {
      val result = FileTypeIndex.containsFileOfType(VueFileType.INSTANCE, GlobalSearchScope.projectScope(project))
      CachedValueProvider.Result.create(result,
                                        VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                                        ProjectRootModificationTracker.getInstance(project))
    }
  }
}

private fun isVueContextFromProviders(psiDir: PsiDirectory): CachedValueProvider.Result<Boolean> {
  val dependencies = mutableSetOf<Any>()
  for (provider in VueContextProvider.VUE_CONTEXT_PROVIDER_EP.extensionList) {
    val result = provider.isVueContext(psiDir)
    if (result.value == true) {
      return result
    }
    dependencies.addAll(result.dependencyItems)
  }
  return CachedValueProvider.Result(false, *dependencies.toTypedArray())
}
