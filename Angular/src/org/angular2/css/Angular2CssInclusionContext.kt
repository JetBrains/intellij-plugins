// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css

import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.css.StylesheetFile
import com.intellij.psi.css.resolve.CssInclusionContext
import com.intellij.psi.css.resolve.CssResolveManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlFile
import com.intellij.util.AstLoadingFilter
import org.angular2.cli.AngularCliUtil
import org.angular2.cli.config.AngularConfigProvider
import org.angular2.cli.config.AngularProject
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2EntitiesProvider
import org.jetbrains.annotations.NonNls

private class Angular2CssInclusionContext : CssInclusionContext() {
  override fun getContextFiles(current: PsiFile): Array<PsiFile> {
    return getComponentContext(current)?.cssFiles ?: PsiFile.EMPTY_ARRAY
  }

  override fun processAllCssFilesOnResolving(context: PsiElement): Boolean {
    val componentContext = getComponentContext(context)
    return componentContext != null && !componentContext.isAngularCli
  }

  override fun getLocalUseScope(file: PsiFile): Array<PsiFile> {
    if (file is StylesheetFile) {
      val component = Angular2EntitiesProvider.findTemplateComponent(file)
      if (component != null) {
        val files = ArrayList(component.cssFiles)
        component.templateFile?.let { files.add(it) }
        return files.toTypedArray<PsiFile>()
      }
    }
    return PsiFile.EMPTY_ARRAY
  }
}

private class ComponentCssContext(private val myComponent: Angular2Component, file: PsiFile) {
  private val myAngularCliJson: VirtualFile?

  val dependencies: Array<Any>
    get() = listOfNotNull(PsiModificationTracker.MODIFICATION_COUNT, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, myAngularCliJson)
      .toTypedArray()

  val cssFiles: Array<PsiFile>
    get() {
      val project = myComponent.sourceElement.project
      val cssFilesList = ArrayList(myComponent.cssFiles)
      val ngProject: AngularProject? = myAngularCliJson?.let { AngularConfigProvider.findAngularProject(project, it) }
      if (ngProject != null) {
        val psiManager = PsiManager.getInstance(project)
        val html = ngProject.indexHtmlFile?.let { psiManager.findFile(it) }
        if (html is XmlFile) {
          AstLoadingFilter.forceAllowTreeLoading<Boolean, RuntimeException>(html) {
            cssFilesList.addAll(CssResolveManager.getInstance().newResolver.resolveStyleSheets(html, null))
          }
        }
        cssFilesList.addAll(ngProject.globalStyleSheets.mapNotNull { file -> psiManager.findFile(file) as? StylesheetFile })
      }
      return cssFilesList.toTypedArray<PsiFile>()
    }

  val isAngularCli: Boolean
    get() = myAngularCliJson != null

  init {
    val original = InjectedLanguageManager.getInstance(file.project).getTopLevelFile(
      CompletionUtil.getOriginalOrSelf(file))
    val angularCliFolder = AngularCliUtil.findAngularCliFolder(
      file.project, original.originalFile.viewProvider.virtualFile)
    myAngularCliJson = AngularCliUtil.findCliJson(angularCliFolder)
  }
}

@NonNls
private val COMPONENT_CONTEXT_KEY = Key<CachedValue<ComponentCssContext>>("ng.component.context")

private fun getComponentContext(context: PsiElement): ComponentCssContext? {
  val file = context.containingFile
  return CachedValuesManager.getCachedValue(file, COMPONENT_CONTEXT_KEY) {
    val component = Angular2EntitiesProvider.findTemplateComponent(file)
    if (component != null) {
      val componentCssContext = ComponentCssContext(component, file)
      create(componentCssContext,
             *componentCssContext.dependencies)
    }
    else {
      create(null,
             PsiModificationTracker.MODIFICATION_COUNT,
             VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }
  }
}

