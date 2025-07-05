package org.angular2.lang.expr.service

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceCommand
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.typescript.compiler.languageService.protocol.JSLanguageServiceObjectWithStateUpdater
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringHash
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.angular2.lang.expr.service.protocol.commands.Angular2TranspiledTemplateCommand
import org.angular2.lang.expr.service.protocol.commands.toAngular2TranspiledTemplateRequestArgs
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile
import java.util.concurrent.ConcurrentHashMap

class Angular2LanguageServiceCache(project: Project) : TypeScriptLanguageServiceCache(project) {

  private val transpiledComponentCache: MutableMap<VirtualFile, TranspiledComponentInfo> = ConcurrentHashMap()

  override fun tryGetServiceObjectWithoutStateUpdater(input: JSLanguageServiceCommand): JSLanguageServiceObjectWithStateUpdater? =
    if (input is Angular2TranspiledTemplateCommand)
      null
    else
      super.tryGetServiceObjectWithoutStateUpdater(input)

  override fun getServiceObjectWithStateUpdater(input: JSLanguageServiceCommand): JSLanguageServiceObjectWithStateUpdater? =
    if (input is Angular2TranspiledTemplateCommand)
      getUpdateTemplateServiceObject(input)
    else
      super.getServiceObjectWithStateUpdater(input)


  private fun getUpdateTemplateServiceObject(input: Angular2TranspiledTemplateCommand): JSLanguageServiceObjectWithStateUpdater? {
    val templateFile = PsiManager.getInstance(myProject).findFile(input.templateFile)
                       ?: return null
    val componentFile = Angular2TranspiledDirectiveFileBuilder.findDirectiveFile(templateFile)
    val componentVirtualFile = componentFile?.virtualFile ?: return null
    val newContents = Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveFile(componentFile)

    if (newContents == null) {
      transpiledComponentCache.remove(componentVirtualFile)
      return null
    }

    val newInfo = TranspiledComponentInfo(newContents)
    val oldInfo = transpiledComponentCache[componentVirtualFile]

    if (oldInfo == newInfo) {
      return null
    }
    val serviceObject = newContents.toAngular2TranspiledTemplateRequestArgs(myProject, componentVirtualFile)
    return JSLanguageServiceObjectWithStateUpdater(
      listOf(object : Angular2TranspiledTemplateCommand(componentVirtualFile), JSLanguageServiceSimpleCommand {
        override fun toSerializableObject(): JSLanguageServiceObject = serviceObject
      }),
      listOf({
               transpiledComponentCache[componentVirtualFile] = newInfo
             })
    )
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
