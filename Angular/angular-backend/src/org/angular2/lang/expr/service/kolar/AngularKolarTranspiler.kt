package org.angular2.lang.expr.service.kolar

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.typescript.kolar.CodeMapping
import com.intellij.lang.typescript.kolar.KolarCodeInformation
import com.intellij.lang.typescript.kolar.KolarCodegenContext
import com.intellij.lang.typescript.kolar.KolarScriptSnapshot
import com.intellij.lang.typescript.kolar.KolarTranspiler
import com.intellij.lang.typescript.kolar.KolarVirtualCode
import com.intellij.lang.typescript.kolar.sourceMap.KolarMapping
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.util.asSafely
import com.intellij.util.indexing.SubstitutedFileType
import org.angular2.Angular2DecoratorUtil.isHostBindingExpression
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.SourceMappingFlag
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder
import org.angular2.lang.html.Angular2HtmlDialect
import org.intellij.images.fileTypes.impl.SvgFileType
import java.util.EnumSet

private const val ANGULAR_HTML_LANG = "angular-html"

internal class AngularKolarTranspiler(private val project: Project) : KolarTranspiler {

  override fun getLanguageId(file: VirtualFile): String? =
    if (isAcceptableHtmlFile(file)) ANGULAR_HTML_LANG else null

  override fun isAssociatedFileOnly(file: VirtualFile, languageId: String): Boolean =
    languageId == ANGULAR_HTML_LANG

  override fun supportsInjectedFile(file: PsiFile): Boolean =
    file.language is Angular2ExprDialect || file.language is Angular2HtmlDialect

  override fun preventHighlighting(file: PsiFile): Boolean =
    (file.language is Angular2HtmlDialect || file.language is Angular2ExprDialect)
    && Angular2EntitiesProvider.findTemplateComponent(file) == null
    && !isHostBindingExpression(file)

  override fun createVirtualCode(
    file: VirtualFile,
    languageId: String,
    snapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
  ): KolarVirtualCode? {
    val fileName = file.name
    if (languageId == "typescript" &&
        !fileName.endsWith(".d.ts") &&
        !NodeModuleUtil.hasNodeModulesDirInPath(file, null)) {
      return buildVirtualCode(file, snapshot, ctx)
    }
    return null
  }

  private fun getTranspiledDirectiveFile(file: VirtualFile): Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile? =
    PsiManager.getInstance(project).findFile(file)
      ?.let { Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveFile(it) }

  private fun isAcceptableHtmlFile(file: VirtualFile): Boolean =
    file.isInLocalFileSystem && file.fileType.let {
      (it is HtmlFileType || it is SvgFileType) && SubstitutedFileType.substituteFileType(file, it, project)
        .asSafely<SubstitutedFileType>()?.language is Angular2HtmlDialect
    }

  fun buildVirtualCode(
    file: VirtualFile,
    snapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
  ): KolarVirtualCode {
    val transpiledFile = if (ApplicationManager.getApplication().isReadAccessAllowed)
      getTranspiledDirectiveFile(file)
    else
      ReadAction.computeCancellable<Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile?, Throwable> {
        getTranspiledDirectiveFile(file)
      }
    val fileName = file.path
    if (transpiledFile != null /*&& isTemplateInSync(snapshot, ctx, transpiledFile, fileName)*/) {
      val newMappings = mutableListOf<CodeMapping>()
      val newAssociatedScriptMappings = mutableMapOf<String, MutableList<CodeMapping>>()

      transpiledFile.fileMappings.values.forEach { fileMapping ->
        val normalizedMappingFileName = fileMapping.fileName.replace('\\', '/')
        val mappingsWithData: MutableList<CodeMapping> = if (normalizedMappingFileName == fileName.replace('\\', '/')) {
          newMappings
        }
        else {
          // Do not register source -> generated mappings for external files
          val scriptId = if (fileMapping.externalFile) {
            normalizedMappingFileName
          }
          else {
            ctx.getAssociatedScript(fileMapping.fileName)?.id
          }

          if (scriptId != null) {
            newAssociatedScriptMappings.computeIfAbsent(scriptId) { mutableListOf() }
          }
          else {
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
      return KolarVirtualCode(
        transpiler = this,
        id = "main",
        // Create a new non-physical VirtualFile for the transpiled template
        virtualFile = createLightVirtualFileWithParent(fileName, transpiledFile.generatedCode),
        languageId = "typescript",
        snapshot = KolarScriptSnapshot.create(transpiledFile.generatedCode),
        mappings = newMappings,
        associatedScriptMappings = newAssociatedScriptMappings,
      )
    }
    else {
      return KolarVirtualCode(
        transpiler = this,
        id = "main",
        // Point to the existing virtual file from the local filesystem if possible
        virtualFile = file,
        languageId = "typescript",
        snapshot = snapshot,
        mappings = listOf(
          KolarMapping(
            generatedOffsets = intArrayOf(0),
            sourceOffsets = intArrayOf(0),
            lengths = intArrayOf(snapshot.length),
            data = KolarCodeInformation(
              format = true,
              completion = KolarCodeInformation.CompletionInfo.Enabled,
              navigation = KolarCodeInformation.NavigationInfo.Enabled,
              semantic = KolarCodeInformation.SemanticInfo.Enabled,
              structure = true,
              verification = KolarCodeInformation.VerificationInfo.Enabled
            )
          )
        ),
        associatedScriptMappings = emptyMap(),
      )
    }
  }

  private fun isTemplateInSync(
    snapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
    transpiledFile: Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile,
    fileName: String,
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
      }
      else {
        false
        //val associatedSnapshot = ctx.getAssociatedScript(fileMapping.fileName)?.snapshot
        //associatedSnapshot != null &&
        //associatedSnapshot.getText(0, associatedSnapshot.getLength()) != fileMapping.sourceFile.text
      }
    }
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
    }
    else {
      LightVirtualFile(normalizedPath, TypeScriptFileType, content)
    }
  }
}