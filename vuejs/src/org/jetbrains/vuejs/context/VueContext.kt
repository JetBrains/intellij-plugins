// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.library.JSCDNLibManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.*
import com.intellij.psi.impl.source.html.HtmlLikeFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.xml.XmlTag
import com.intellij.testFramework.LightVirtualFileBase
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.index.VUE_MODULE
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage

private val VUE_CONTEXT_KEY: Key<CachedValue<Boolean>> = Key("vue.isContext")
private val VUE_PREV_CONTEXT_KEY = Key<Boolean>("vue.isContext.prev")
private val VUE_CONTEXT_RELOAD_MARKER_KEY = Key<Any>("vue.isContext.reloadMarker")
private val reloadMonitor = Any()

fun isVueContext(context: PsiElement): Boolean {
  if (!context.isValid) {
    return false
  }
  if (context is PsiDirectory) {
    return withContextChangeCheck(context, context.project) { isVueContext(context) }
  }
  val psiFile = InjectedLanguageManager.getInstance(context.project).getTopLevelFile(context) ?: return false
  val file = psiFile.originalFile.virtualFile
  if ((file != null && file.fileType == VueFileType.INSTANCE)
      || (file == null && psiFile.language == VueLanguage.INSTANCE)
      || isSimpleVueHtml(psiFile)) {
    return true
  }

  @Suppress("DEPRECATION")
  return isVueContext(if (file != null && file.isInLocalFileSystem) file
                      else psiFile.project.baseDir ?: return false,
                      psiFile.project)
}

fun isVueContext(contextFile: VirtualFile, project: Project): Boolean {
  // TODO merge with Angular
  var file: VirtualFile? = contextFile
  while (file is LightVirtualFileBase) {
    file = file.originalFile
  }
  val psiDir = file?.parent
                 ?.let { if (it.isValid) PsiManager.getInstance(project).findDirectory(it) else null }
               ?: return false
  return withContextChangeCheck(contextFile, project) {
    isVueContext(psiDir) && !isVueContextForbiddenFromProviders(contextFile, project)
  }
}

private fun isVueContext(directory: PsiDirectory): Boolean {
  return CachedValuesManager.getCachedValue(directory, VUE_CONTEXT_KEY) {
    isVueContextFromProviders(directory)
  }
}

fun isSimpleVueHtml(psiFile: PsiFile): Boolean {
  if (psiFile is HtmlLikeFile) {
    return CachedValuesManager.getCachedValue(psiFile) {
      var level = 0
      var result = false
      psiFile.acceptChildren(object : XmlRecursiveElementVisitor() {
        override fun visitXmlTag(tag: XmlTag) {
          if (HtmlUtil.isScriptTag(tag) && hasVueScriptLink(tag)) {
            result = true
          }
          if (++level < 3) {
            tag.subTags.forEach { it.accept(this) }
            level--
          }
        }

        private fun hasVueScriptLink(tag: XmlTag): Boolean {
          val link = tag.getAttribute(HtmlUtil.SRC_ATTRIBUTE_NAME)?.value
          if (link == null || !link.contains("vue")) {
            return false
          }
          if (JSCDNLibManager.getLibraryForUrl(link)?.libraryName == VUE_MODULE) {
            return true
          }
          val fileName = VfsUtil.extractFileName(link)
          return fileName != null && fileName.startsWith("vue.") && fileName.endsWith(".js")
        }
      })
      CachedValueProvider.Result.create(result, psiFile)
    }
  }
  return false
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

private fun isVueContextForbiddenFromProviders(context: VirtualFile, project: Project): Boolean =
  VueContextProvider.VUE_CONTEXT_PROVIDER_EP.extensionList.any { it.isVueContextForbidden(context, project) }


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

private fun withContextChangeCheck(dataHolder: UserDataHolder, project: Project, stateProvider: () -> Boolean): Boolean {
  val currentState = stateProvider()
  val prevState = dataHolder.getUserData(VUE_PREV_CONTEXT_KEY)
  if (prevState != null && prevState != currentState) {
    reloadProject(project)
  }
  dataHolder.putUserData(VUE_PREV_CONTEXT_KEY, currentState)
  return currentState
}

private fun reloadProject(project: Project) {
  synchronized(reloadMonitor) {
    if (project.getUserData(VUE_CONTEXT_RELOAD_MARKER_KEY) != null) {
      return
    }
    project.putUserData(VUE_CONTEXT_RELOAD_MARKER_KEY, true)
  }
  ApplicationManager.getApplication().invokeLater(
    Runnable {
      WriteAction.run<RuntimeException> {
        ProjectRootManagerEx.getInstanceEx(project)
          .makeRootsChange(EmptyRunnable.getInstance(), false, true)
        project.putUserData(VUE_CONTEXT_RELOAD_MARKER_KEY, null)
      }
    }, project.disposed)
}
