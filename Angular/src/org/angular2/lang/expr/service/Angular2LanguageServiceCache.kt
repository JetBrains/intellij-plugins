package org.angular2.lang.expr.service

import com.intellij.lang.javascript.service.JSLanguageServiceQueue
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringHash
import com.intellij.openapi.vfs.VirtualFile
import org.angular2.lang.expr.service.protocol.commands.Angular2TranspiledTemplateCommand
import org.angular2.lang.expr.service.protocol.commands.toAngular2TranspiledTemplateRequestArgs
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile
import java.util.concurrent.ConcurrentHashMap

class Angular2LanguageServiceCache(project: Project) : TypeScriptLanguageServiceCache(project) {
  private val transpiledComponentCache: MutableMap<VirtualFile, TranspiledComponentInfo> = ConcurrentHashMap()

  suspend fun refreshAndCacheTranspiledTemplate(
    process: JSLanguageServiceQueue,
    componentVirtualFile: VirtualFile,
    newContents: TranspiledDirectiveFile?,
  ) {
    if (newContents == null) {
      transpiledComponentCache.remove(componentVirtualFile)
      return
    }

    val newInfo = TranspiledComponentInfo(newContents)
    val oldInfo = transpiledComponentCache[componentVirtualFile]

    if (oldInfo == newInfo) {
      return
    }
    val serviceObject = newContents.toAngular2TranspiledTemplateRequestArgs(myProject, componentVirtualFile)
    val command = Angular2TranspiledTemplateCommand(serviceObject)
    if (process.execute(command) != null) {
      transpiledComponentCache[componentVirtualFile] = newInfo
    }
  }

  private class TranspiledComponentInfo(contents: TranspiledDirectiveFile) {
    val contentsHash: Long = StringHash.buz(contents.generatedCode)
    val timestamps: Map<String, Long> = contents.fileMappings.values.associateBy({ it.fileName }, { it.sourceFile.modificationStamp })

    override fun equals(other: Any?): Boolean {
      return other === this || other is TranspiledComponentInfo
             && contentsHash == other.contentsHash
             && timestamps == other.timestamps
    }

    override fun hashCode(): Int {
      return 31 * contentsHash.hashCode() + timestamps.hashCode()
    }
  }

}
