// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.asSafely
import com.intellij.util.indexing.FileBasedIndexImpl
import com.intellij.util.text.SemVer
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedName
import com.intellij.webSymbols.context.WebSymbolsContext
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.codeInsight.SETUP_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.withoutPreRelease
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.libraries.KIND_VUE_CLASS_COMPONENT_LIBRARY
import org.jetbrains.vuejs.libraries.KIND_VUE_STORE
import org.jetbrains.vuejs.libraries.VUE_STORE_PINIA
import org.jetbrains.vuejs.libraries.VUE_STORE_VUEX
import org.jetbrains.vuejs.libraries.componentDecorator.COMPONENT_DEC
import org.jetbrains.vuejs.libraries.componentDecorator.OPTIONS_DEC
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.VueWebSymbolsQueryConfigurator


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

fun getVueClassComponentLibrary(location: PsiElement): String? =
  WebSymbolsContext.get(KIND_VUE_CLASS_COMPONENT_LIBRARY, location)

fun getVueClassComponentDecoratorName(location: PsiElement): String =
  if (isVue3(location))
    OPTIONS_DEC
  else
    COMPONENT_DEC

fun isVue3(context: PsiElement): Boolean =
  isVueContext(context) && detectVueVersion(context).let {
    it == null || it >= VUE_3_0_0
  }

fun supportsScriptSetup(context: PsiElement?): Boolean =
  context
    ?.let { WebSymbolsQueryExecutorFactory.create(it, false) }
    ?.takeIf { it.framework == VueFramework.ID }
    ?.runNameMatchQuery(listOf(WebSymbolQualifiedName(WebSymbol.NAMESPACE_HTML, VueWebSymbolsQueryConfigurator.KIND_VUE_TOP_LEVEL_ELEMENTS,
                                                      HtmlUtil.SCRIPT_TAG_NAME),
                               WebSymbolQualifiedName(WebSymbol.NAMESPACE_HTML, WebSymbol.KIND_HTML_ATTRIBUTES, SETUP_ATTRIBUTE_NAME)))
    ?.firstOrNull() != null


private val VUE_3_0_0 = SemVer("3.0.0", 3, 0, 0)

private fun detectVueVersion(context: PsiElement): SemVer? {
  val vf = context.containingFile?.originalFile?.virtualFile
           ?: context.asSafely<PsiDirectory>()?.virtualFile
           ?: return null
  var fromRange: SemVer? = null
  var exact: SemVer? = null
  PackageJsonUtil.processUpPackageJsonFilesInAllScope(vf) { pkgJson ->
    val data = PackageJsonData.getOrCreate(pkgJson)
    fromRange = data.allDependencyEntries[VUE_MODULE]
      ?.takeIf { it.versionRange.let { range -> !range.contains(" ") && !range.startsWith('<') } }
      ?.parseVersion()
    exact = pkgJson.parent.findFileByRelativePath(NodeModuleUtil.NODE_MODULES + "/" + VUE_MODULE + "/" + PackageJsonUtil.FILE_NAME)
      ?.let { PackageJsonData.getOrCreate(it).version }
    fromRange == null && exact == null
  }
  return (exact ?: fromRange)?.withoutPreRelease()
}