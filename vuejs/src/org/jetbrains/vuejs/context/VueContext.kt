// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.indexing.FileBasedIndexImpl
import com.intellij.util.text.SemVer
import com.intellij.webSymbols.context.WebSymbolsContext
import org.jetbrains.vuejs.codeInsight.withoutPreRelease
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.libraries.KIND_VUE_STORE
import org.jetbrains.vuejs.libraries.VUE_STORE_PINIA
import org.jetbrains.vuejs.libraries.VUE_STORE_VUEX
import org.jetbrains.vuejs.web.VueFramework


fun isVueContext(context: PsiElement): Boolean = VueFramework.instance.isInContext(context)

fun isVueContext(contextFile: VirtualFile, project: Project): Boolean = VueFramework.instance.isInContext(contextFile, project)

fun hasVueFiles(project: Project): Boolean =
  CachedValuesManager.getManager(project).getCachedValue(project) {
    CachedValueProvider.Result.create(
      FileBasedIndexImpl.disableUpToDateCheckIn<Boolean, Exception> {
        FileTypeIndex.containsFileOfType(VueFileType.INSTANCE, GlobalSearchScope.projectScope(project))
      },
      VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
      DumbService.getInstance(project)
    )
  }

fun hasPinia(context: PsiElement) =
  WebSymbolsContext.get(KIND_VUE_STORE, context) == VUE_STORE_PINIA

fun hasVuex(context: PsiElement) =
  WebSymbolsContext.get(KIND_VUE_STORE, context) == VUE_STORE_VUEX

fun isVue3(context: PsiElement): Boolean =
  isVueContext(context) && detectVueVersion(context).let {
    it == null || it >= VUE_3_0_0
  }

private val VUE_3_0_0 = SemVer("3.0.0", 3, 0, 0)

private fun detectVueVersion(context: PsiElement): SemVer? {
  val vf = context.containingFile.originalFile.virtualFile ?: return null
  var fromRange: SemVer? = null
  var exact: SemVer? = null
  PackageJsonUtil.processUpPackageJsonFilesInAllScope(vf) { pkgJson ->
    val data = PackageJsonData.getOrCreate(pkgJson)
    fromRange = data.allDependencyEntries[VUE_MODULE]
      ?.takeIf { it.versionRange.let { range -> !range.contains(" ") && !range.startsWith('<') } }
      ?.parseVersion()
    exact = pkgJson.parent.findFileByRelativePath(NodeModuleUtil.NODE_MODULES + "/" + VUE_MODULE + "/" + PackageJsonUtil.FILE_NAME)
      ?.let { PackageJsonData.getOrCreate(it).version }
    fromRange != null && exact != null
  }
  return (exact ?: fromRange)?.withoutPreRelease()
}