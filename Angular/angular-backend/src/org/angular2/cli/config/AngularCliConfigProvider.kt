package org.angular2.cli.config

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import org.angular2.cli.AngularCliUtil

private val ANGULAR_CLI_CONFIG_KEY = Key.create<CachedAngularConfig>("ANGULAR_CONFIG_KEY")

class AngularCliConfigProvider : AngularConfigProvider {

  override fun findAngularConfig(project: Project, context: VirtualFile): AngularConfig? {
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
        AngularCliConfigImpl(document?.charsSequence ?: VfsUtilCore.loadText(angularCliJson), angularCliJson)
      }
      catch (e: ProcessCanceledException) {
        throw e
      }
      catch (e: Exception) {
        thisLogger().warn("Cannot load " + angularCliJson.name + ": " + e.message)
        null
      }
      cached = CachedAngularConfig(config, documentModStamp, fileModStamp)
      ANGULAR_CLI_CONFIG_KEY[angularCliJson] = cached
    }
    return cached.config
  }

}

private data class CachedAngularConfig(
  val config: AngularConfig?,
  val docTimestamp: Long,
  val fileTimestamp: Long
)