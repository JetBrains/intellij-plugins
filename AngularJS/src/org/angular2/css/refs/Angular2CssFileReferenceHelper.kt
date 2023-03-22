// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.css.refs

import com.intellij.lang.javascript.frameworks.modules.langs.TildeFileSystemItemCompletion
import com.intellij.lang.javascript.frameworks.modules.langs.getSyntheticResolveContext
import com.intellij.lang.javascript.frameworks.modules.resolver.JSDefaultFileReferenceContext
import com.intellij.lang.javascript.frameworks.modules.resolver.JSParsedPathElement
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.css.CssSupportLoader
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceHelper
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSetParameters
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.Processor
import org.angular2.cli.config.AngularConfigProvider
import org.angular2.cli.config.AngularProject
import java.util.*

class Angular2CssFileReferenceHelper : FileReferenceHelper() {

  override fun isMine(project: Project, hostFile: VirtualFile): Boolean {
    val psiFile = getPsiFileSystemItem(project, hostFile)
    return psiFile is StylesheetFile || CssSupportLoader.isInFileThatSupportsEmbeddedCss(psiFile)
  }

  override fun processContexts(parameters: FileReferenceSetParameters,
                               hostFile: VirtualFile,
                               bind: Boolean,
                               processor: Processor<in PsiFileSystemItem>): Boolean {
    val hasTilde = parameters.pathString.startsWith("~")

    val element = parameters.element
    val angularProject = angularProject(element)
    if (angularProject == null) return true

    hostFile.parent?.let {
      processor.process(TildeFileSystemItemCompletion(element.project, it, Angular2OverrideContextFilesProvider(angularProject, element)))
    }

    //webpack-specific case for angular
    if (hasTilde) {
      getOverrideTildeContexts(angularProject, parameters.pathString.substring(1), element).forEach(processor::process)
      return false //stop(!) processing
    }

    getContexts(angularProject, element).forEach(processor::process)
    return true
  }

  private fun getContexts(angularProject: AngularProject, element: PsiElement): Collection<PsiFileSystemItem> {
    val psiManager = element.manager
    return angularProject.stylePreprocessorIncludeDirs.mapNotNull { psiManager.findDirectory(it) }
  }
}

private fun getOverrideTildeContexts(angularProject: AngularProject,
                                     actualText: String,
                                     element: PsiElement): Collection<PsiFileSystemItem> {
  val mappingContext = object : JSDefaultFileReferenceContext(actualText, element, null) {
    override fun getDefaultRoots(project: Project, moduleName: String, contextFile: VirtualFile): Collection<VirtualFile> {
      return getOverrideContextFiles(angularProject, element)
    }
  }

  val elements = JSParsedPathElement.parseReferenceText(actualText, false)
  val item = getSyntheticResolveContext(element, mappingContext, elements, actualText, true) ?: return emptyList()
  return listOf(item)
}

private class Angular2OverrideContextFilesProvider(val angularProject: AngularProject,
                                                   val element: PsiElement) : TildeFileSystemItemCompletion.AdditionalContextsProvider {
  override fun get(): Collection<VirtualFile> =
    getOverrideContextFiles(angularProject, element)

  override fun equals(other: Any?): Boolean =
    other is Angular2OverrideContextFilesProvider
    && other.angularProject == angularProject
    && other.element == element

  override fun hashCode(): Int =
    Objects.hash(angularProject, element)

}

private fun getOverrideContextFiles(angularProject: AngularProject, element: PsiElement): MutableSet<VirtualFile> {
  val result = mutableSetOf<VirtualFile>()
  val tsConfig = TypeScriptConfigService.Provider.parseConfigFile(element.project, angularProject.tsConfigFile)
  val baseUrl = tsConfig?.baseUrl
  if (baseUrl != null) {
    result.add(baseUrl)
  }
  else {
    angularProject.cssResolveRootDir?.let { result.add(it) }
  }
  return result
}

private fun angularProject(element: PsiElement): AngularProject? {
  val file = PsiUtilCore.getVirtualFile(element) ?: return null
  return AngularConfigProvider.getAngularProject(element.project, file)
}