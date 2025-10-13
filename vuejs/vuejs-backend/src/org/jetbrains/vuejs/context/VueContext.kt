// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.hasFilesOfType
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.context.PolyContext
import com.intellij.polySymbols.html.HTML_ATTRIBUTES
import com.intellij.polySymbols.query.PolySymbolQueryExecutorFactory
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import com.intellij.util.text.SemVer
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.codeInsight.SETUP_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.VAPOR_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.withoutPreRelease
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.libraries.*
import org.jetbrains.vuejs.libraries.componentDecorator.COMPONENT_DEC
import org.jetbrains.vuejs.libraries.componentDecorator.OPTIONS_DEC
import org.jetbrains.vuejs.web.VUE_TOP_LEVEL_ELEMENTS
import org.jetbrains.vuejs.web.VueFramework

private val vueFrameworkInstance get() = WebFramework.get("vue")

fun isVueContext(context: PsiElement): Boolean = vueFrameworkInstance.isInContext(context)

fun isVueContext(contextFile: VirtualFile, project: Project): Boolean = vueFrameworkInstance.isInContext(contextFile, project)

fun hasVueFiles(project: Project): Boolean =
  hasFilesOfType(project, VueFileType)

fun hasPinia(context: PsiElement): Boolean =
  PolyContext.get(KIND_VUE_STORE, context) == VUE_STORE_PINIA

fun hasVuex(context: PsiElement): Boolean =
  PolyContext.get(KIND_VUE_STORE, context) == VUE_STORE_VUEX

fun hasNuxt(context: PsiElement): Boolean =
  PolyContext.get(KIND_VUE_SSR_FRAMEWORK, context) == VUE_FRAMEWORK_NUXT

fun supportsDefineComponent(context: PsiElement): Boolean =
  detectPkgVersion(context, VUE_MODULE).let {
    it == null || it >= VERSION_2_7_0
  }

fun getVueClassComponentLibrary(location: PsiElement): String? =
  PolyContext.get(KIND_VUE_CLASS_COMPONENT_LIBRARY, location)

fun getVueClassComponentDecoratorName(location: PsiElement): String =
  if (detectPkgVersion(location, VUE_CLASS_COMPONENT).let {
      it == null || it < VERSION_8_0_0
    })
    COMPONENT_DEC
  else
    OPTIONS_DEC

fun isVue3(context: PsiElement): Boolean =
  isVueContext(context) && detectPkgVersion(context, VUE_MODULE).let {
    it == null || it >= VERSION_3_0_0
  }

fun supportsScriptSetup(context: PsiElement?): Boolean =
  supportsScriptAttribute(context, SETUP_ATTRIBUTE_NAME)

fun supportsScriptVapor(context: PsiElement?): Boolean =
  supportsScriptAttribute(context, VAPOR_ATTRIBUTE_NAME)

private fun supportsScriptAttribute(
  context: PsiElement?,
  attributeName: String,
): Boolean =
  context
    ?.let { PolySymbolQueryExecutorFactory.create(it, false) }
    ?.takeIf { it.framework == VueFramework.ID }
    ?.nameMatchQuery(listOf(
      VUE_TOP_LEVEL_ELEMENTS.withName(HtmlUtil.SCRIPT_TAG_NAME),
      HTML_ATTRIBUTES.withName(attributeName),
    ))
    ?.exclude(PolySymbolModifier.ABSTRACT)
    ?.run()
    ?.firstOrNull() != null


private val VERSION_2_7_0 = SemVer("2.7.0", 2, 7, 0)
private val VERSION_3_0_0 = SemVer("3.0.0", 3, 0, 0)
private val VERSION_8_0_0 = SemVer("8.0.0", 8, 0, 0)

private fun detectPkgVersion(context: PsiElement, packageName: String): SemVer? {
  val vf = context.containingFile?.originalFile?.virtualFile
           ?: context.asSafely<PsiDirectory>()?.virtualFile
           ?: return null
  var fromRange: SemVer? = null
  var exact: SemVer? = null
  PackageJsonUtil.processUpPackageJsonFilesInAllScope(vf) { pkgJson ->
    val data = PackageJsonData.getOrCreate(pkgJson)
    fromRange = data.allDependencyEntries[packageName]
      ?.takeIf { it.versionRange.let { range -> !range.contains(" ") && !range.startsWith('<') } }
      ?.parseVersion()
    exact = pkgJson.parent.findFileByRelativePath(NodeModuleUtil.NODE_MODULES + "/" + packageName + "/" + PackageJsonUtil.FILE_NAME)
      ?.let { PackageJsonData.getOrCreate(it).version }
    fromRange == null && exact == null
  }
  return (exact ?: fromRange)?.withoutPreRelease()
}