// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.angular2.cli.AngularCliUtil

class AngularConfigProvider private constructor() {

  companion object {

    private val ANGULAR_CLI_CONFIG_KEY = Key.create<CachedValue<AngularConfig>>("ANGULAR_CONFIG_KEY")
    private val LOG = Logger.getInstance(AngularConfigProvider::class.java)

    @JvmStatic
    fun getAngularProject(project: Project, context: VirtualFile): AngularProject? {
      return getAngularConfig(project, context)?.getProject(context)
    }

    @JvmStatic
    fun getAngularConfig(project: Project, context: VirtualFile): AngularConfig? {
      val angularCliJson = AngularCliUtil.findAngularCliFolder(project, context)?.let {
        AngularCliUtil.findCliJson(it)
      } ?: return null
      val psiFile = PsiManager.getInstance(project).findFile(angularCliJson) ?: return null
      return CachedValuesManager.getManager(project).getCachedValue(psiFile, ANGULAR_CLI_CONFIG_KEY, {
        val cachedDocument = FileDocumentManager.getInstance().getCachedDocument(angularCliJson)
        val config =
          try {
            AngularConfig(cachedDocument?.charsSequence ?: VfsUtilCore.loadText(angularCliJson),
                          angularCliJson, psiFile.project)
          }
          catch (e: ProcessCanceledException) {
            throw e
          }
          catch (e: Exception) {
            LOG.warn("Cannot load " + angularCliJson.name + ": " + e.message)
            null
          }
        CachedValueProvider.Result.create(config, cachedDocument ?: angularCliJson)
      }, false)
    }
  }
}
