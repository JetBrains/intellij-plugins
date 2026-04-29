package org.angular2.lang.expr.service.kolar

import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.typescript.kolar.CodeMapping
import com.intellij.lang.typescript.kolar.KolarCodeInformation
import com.intellij.lang.typescript.kolar.KolarCodegenContext
import com.intellij.lang.typescript.kolar.KolarScriptSnapshot
import com.intellij.lang.typescript.kolar.KolarVirtualCode
import com.intellij.lang.typescript.kolar.sourceMap.KolarMapping
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.SourceMappingFlag
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder
import java.util.EnumSet

internal class AngularVirtualCode(val fileName: String) : KolarVirtualCode {
  override val id: String = "main"

  override var virtualFile: VirtualFile = createLightVirtualFileWithParent(fileName, "")
    private set

  override val languageId: String = "typescript"

  override var snapshot: KolarScriptSnapshot = KolarScriptSnapshot.create("")
    private set

  override var mappings: List<CodeMapping> = emptyList()
    private set

  override var associatedScriptMappings: Map<String, List<CodeMapping>>? = emptyMap()
    private set

  var preventLeadingOffset: Boolean = true
    private set

  fun sourceFileUpdated(
    snapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
    transpiledFile: Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile?,
  ): AngularVirtualCode {
    if (transpiledFile != null && isTemplateInSync(snapshot, ctx, transpiledFile)) {

      val newMappings = mutableListOf<CodeMapping>()
      val newAssociatedScriptMappings = mutableMapOf<String, MutableList<CodeMapping>>()

      transpiledFile.fileMappings.values.forEach { fileMapping ->
        val normalizedMappingFileName = fileMapping.fileName.replace('\\', '/')
        val mappingsWithData: MutableList<CodeMapping> = if (normalizedMappingFileName == fileName.replace('\\', '/')) {
          newMappings
        } else {
          // Do not register source -> generated mappings for external files
          val scriptId = if (fileMapping.externalFile) {
            normalizedMappingFileName
          } else {
            ctx.getAssociatedScript(fileMapping.fileName)?.id
          }

          if (scriptId != null) {
            newAssociatedScriptMappings.computeIfAbsent(scriptId) { mutableListOf() }
          } else {
            return@forEach
          }
        }

        // Split the mapping set for Volar - iterate over source mappings
        fileMapping.sourceMappings.filter { !it.ignored }.forEach { sourceMapping ->
          val sourceOffset = sourceMapping.sourceOffset
          val sourceLength = sourceMapping.sourceLength
          val diagnosticsOffset = sourceMapping.diagnosticsOffset ?: -1
          val diagnosticsLength = sourceMapping.diagnosticsLength ?: -1
          val generatedOffset = sourceMapping.generatedOffset
          val generatedLength = sourceMapping.generatedLength

          mappingsWithData.add(
            KolarMapping(
              sourceOffsets = intArrayOf(sourceOffset),
              lengths = intArrayOf(sourceLength),
              generatedOffsets = intArrayOf(generatedOffset),
              generatedLengths = intArrayOf(generatedLength),
              data = createCodeInformation(sourceMapping.flags, diagnosticsOffset == sourceOffset && diagnosticsLength == sourceLength)
            )
          )

          if (diagnosticsOffset >= 0 && (diagnosticsOffset != sourceOffset || diagnosticsLength != sourceLength)) {
            mappingsWithData.add(
              KolarMapping(
                sourceOffsets = intArrayOf(diagnosticsOffset),
                lengths = intArrayOf(diagnosticsLength),
                generatedOffsets = intArrayOf(generatedOffset),
                generatedLengths = intArrayOf(generatedLength),
                data = KolarCodeInformation(
                  verification = KolarCodeInformation.VerificationInfo.Enabled,
                  format = false,
                  completion = null,
                  navigation = null,
                  semantic = null,
                  structure = false
                )
              )
            )
          }
        }
      }

      this.preventLeadingOffset = !requiresTrailingSpaces(snapshot, transpiledFile)
      this.snapshot = KolarScriptSnapshot.create(transpiledFile.generatedCode)

      // Create a new non-physical VirtualFile for the transpiled template
      this.virtualFile = createLightVirtualFileWithParent(fileName, transpiledFile.generatedCode)
      this.mappings = newMappings
      this.associatedScriptMappings = newAssociatedScriptMappings
    } else {
      this.preventLeadingOffset = true
      this.snapshot = snapshot

      // Point to the existing virtual file from the local filesystem if possible
      this.virtualFile = LocalFileSystem.getInstance().findFileByPath(fileName)
        ?: createLightVirtualFileWithParent(fileName, snapshot.text)

      this.mappings = listOf(
        KolarMapping(
          generatedOffsets = intArrayOf(0),
          sourceOffsets = intArrayOf(0),
          lengths = intArrayOf(snapshot.getLength()),
          data = KolarCodeInformation(
            format = true,
            completion = KolarCodeInformation.CompletionInfo.Enabled,
            navigation = KolarCodeInformation.NavigationInfo.Enabled,
            semantic = KolarCodeInformation.SemanticInfo.Enabled,
            structure = true,
            verification = KolarCodeInformation.VerificationInfo.Enabled
          )
        )
      )
      this.associatedScriptMappings = emptyMap()
    }
    return this
  }

  private fun isTemplateInSync(
    snapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
    transpiledFile: Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile,
  ): Boolean {
    val sourceText = snapshot.text
    val normalizedFileName = fileName.replace('\\', '/')

    // Find the file mapping for the current file
    val currentFileMapping = transpiledFile.fileMappings.values.find {
      it.fileName.replace('\\', '/') == normalizedFileName
    } ?: return false

    if (sourceText != currentFileMapping.sourceFile.text) {
      return false
    }

    return transpiledFile.fileMappings.values.none { fileMapping ->
      if (fileMapping.externalFile) {
        false
      } else {
        false
        //val associatedSnapshot = ctx.getAssociatedScript(fileMapping.fileName)?.snapshot
        //associatedSnapshot != null &&
        //associatedSnapshot.getText(0, associatedSnapshot.getLength()) != fileMapping.sourceFile.text
      }
    }
  }

  private fun requiresTrailingSpaces(
    snapshot: KolarScriptSnapshot,
    transpiledFile: Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile,
  ): Boolean {
    val sourceText = snapshot.text
    return !transpiledFile.generatedCode.startsWith(sourceText)
  }

  private fun createCodeInformation(flags: EnumSet<SourceMappingFlag>, verification: Boolean): KolarCodeInformation {
    return KolarCodeInformation(
      format = flags.contains(SourceMappingFlag.FORMAT),
      completion = if (flags.contains(SourceMappingFlag.COMPLETION)) KolarCodeInformation.CompletionInfo.Enabled else null,
      navigation = if (flags.contains(SourceMappingFlag.NAVIGATION)) KolarCodeInformation.NavigationInfo.Enabled else null,
      semantic = if (flags.contains(SourceMappingFlag.SEMANTIC)) KolarCodeInformation.SemanticInfo.Enabled else null,
      structure = flags.contains(SourceMappingFlag.STRUCTURE),
      verification = if (verification) KolarCodeInformation.VerificationInfo.Enabled else null,
      types = flags.contains(SourceMappingFlag.TYPES),
      reverseTypes = flags.contains(SourceMappingFlag.REVERSE_TYPES),
    )
  }

  private fun createLightVirtualFileWithParent(fullPath: String, content: String): VirtualFile {
    val normalizedPath = fullPath.replace('\\', '/')
    val lastSlashIndex = normalizedPath.lastIndexOf('/')

    return if (lastSlashIndex > 0) {
      val parentPath = normalizedPath.substring(0, lastSlashIndex)
      val fileName = normalizedPath.substring(lastSlashIndex + 1)
      val parentDir = LocalFileSystem.getInstance().findFileByPath(parentPath)

      object : LightVirtualFile(fileName, TypeScriptFileType, content) {
        override fun getParent(): VirtualFile? = parentDir
        override fun getPath(): String = normalizedPath
      }
    } else {
      LightVirtualFile(normalizedPath, TypeScriptFileType, content)
    }
  }
}


