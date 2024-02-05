// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli.config

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import org.angular2.cli.AngularCliUtil

object AngularConfigProvider {

  private val ANGULAR_CLI_CONFIG_KEY = Key.create<CachedAngularConfig>("ANGULAR_CONFIG_KEY")
  private val LOG = Logger.getInstance(AngularConfigProvider::class.java)

  @JvmStatic
  fun getAngularProject(project: Project, context: VirtualFile): AngularProject? {
    return getAngularConfig(project, context)?.getProject(context)
  }

  @JvmStatic
  fun getAngularConfig(project: Project, context: VirtualFile): AngularConfig? {
    ProgressManager.checkCanceled()
    val angularCliJson = AngularCliUtil.findAngularCliFolder(project, context)?.let {
      AngularCliUtil.findCliJson(it)
    } ?: return null
    val document = FileDocumentManager.getInstance().getCachedDocument(angularCliJson)
    val documentModStamp = document?.modificationStamp ?: -1
    val fileModStamp = angularCliJson.modificationStamp
    var cached = ANGULAR_CLI_CONFIG_KEY[angularCliJson]
    if (cached == null
        || cached.docTimestamp != documentModStamp
        || cached.fileTimestamp != fileModStamp) {
      val config = try {
        AngularConfig(document?.charsSequence ?: VfsUtilCore.loadText(angularCliJson), angularCliJson)
      }
      catch (e: ProcessCanceledException) {
        throw e
      }
      catch (e: Exception) {
        LOG.warn("Cannot load " + angularCliJson.name + ": " + e.message)
        null
      }
      cached = CachedAngularConfig(config, documentModStamp, fileModStamp)
      ANGULAR_CLI_CONFIG_KEY[angularCliJson] = cached
    }
    return cached.config
  }

  private data class CachedAngularConfig(
    val config: AngularConfig?,
    val docTimestamp: Long,
    val fileTimestamp: Long
  )
}
