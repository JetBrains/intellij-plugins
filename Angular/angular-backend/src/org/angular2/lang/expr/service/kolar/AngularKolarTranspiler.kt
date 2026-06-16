package org.angular2.lang.expr.service.kolar

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.javascript.typeEngine.JSServicePoweredTypeEngineUsageContext
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.typescript.compiler.languageService.TypeScriptAnnotationErrorFilter
import com.intellij.lang.typescript.kolar.CodeMapping
import com.intellij.lang.typescript.kolar.KolarAssociatedFile
import com.intellij.lang.typescript.kolar.KolarCodeInformation
import com.intellij.lang.typescript.kolar.KolarCodegenContext
import com.intellij.lang.typescript.kolar.KolarFileInfo
import com.intellij.lang.typescript.kolar.KolarScriptSnapshot
import com.intellij.lang.typescript.kolar.KolarTranspiledFile
import com.intellij.lang.typescript.kolar.KolarTranspiler
import com.intellij.lang.typescript.kolar.KolarVirtualCode
import com.intellij.lang.typescript.kolar.TypeScriptHover
import com.intellij.lang.typescript.kolar.TypeScriptInlayHint
import com.intellij.lang.typescript.kolar.sourceMap.KolarMapping
import com.intellij.lang.typescript.lsp.LspAnnotationError
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.ThreeState
import com.intellij.util.asSafely
import com.intellij.util.indexing.SubstitutedFileType
import org.angular2.Angular2DecoratorUtil.isHostBindingExpression
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.expr.service.Angular2AnnotationErrorFilter
import org.angular2.lang.expr.service.Angular2LanguageServiceQuickFixFilter
import org.angular2.lang.expr.service.isAngularTypeScriptServiceEnabled
import org.angular2.lang.expr.service.repositionInlayHint
import org.angular2.lang.expr.service.tcb.Angular2TemplateTranspiler.SourceMappingFlag
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveAndTopLevelSourceFile
import org.angular2.lang.expr.service.translateNamesInErrors
import org.angular2.lang.html.Angular2HtmlDialect
import org.intellij.images.fileTypes.impl.SvgFileType
import java.util.EnumSet
import java.util.concurrent.Callable

internal class AngularKolarTranspiler(private val project: Project) : KolarTranspiler {

  override fun isHighlightingCandidate(file: VirtualFile): Boolean =
    file.isInLocalFileSystem && file.fileType.let { it is HtmlFileType || it is SvgFileType }

  override fun isEnabled(file: VirtualFile): Boolean =
    isAngularTypeScriptServiceEnabled(project, file)

  override fun getFileInfo(file: VirtualFile): KolarFileInfo? =
    when {
      isAcceptableHtmlFile(file) -> AngularAssociatedHtmlFile(project, file)
      file.name.let { it.endsWith(".ts") && !it.endsWith(".d.ts") }
      && !NodeModuleUtil.hasNodeModulesDirInPath(file, null) -> AngularTranspiledFile(project, file)
      else -> null
    }

  override fun supportsTypeEvaluation(virtualFile: VirtualFile, element: PsiElement): Boolean =
    (element.language.let { it is Angular2ExprDialect || it is Angular2HtmlDialect }
     && Angular2EntitiesProvider.findTemplateComponent(element) != null)

  override fun isTypeEvaluationEnabledInUsageContext(usageContext: JSServicePoweredTypeEngineUsageContext): ThreeState =
    ThreeState.YES

  override fun supportsInjectedFile(file: PsiFile): Boolean =
    file.language is Angular2ExprDialect || file.language is Angular2HtmlDialect

  override fun preventHighlighting(file: PsiFile): Boolean =
    (file.language is Angular2HtmlDialect || file.language is Angular2ExprDialect)
    && Angular2EntitiesProvider.findTemplateComponent(file) == null
    && !isHostBindingExpression(file)

  private fun isAcceptableHtmlFile(file: VirtualFile): Boolean =
    file.isInLocalFileSystem && file.fileType.let {
      (it is HtmlFileType || it is SvgFileType) && SubstitutedFileType.substituteFileType(file, it, project)
        .asSafely<SubstitutedFileType>()?.language is Angular2HtmlDialect
    }
}

private abstract class AngularFileInfo(
  val project: Project,
  val file: VirtualFile,
) : KolarFileInfo {
  override fun createAnnotationErrorFilter(): TypeScriptAnnotationErrorFilter =
    Angular2AnnotationErrorFilter

  override fun postProcessErrors(file: PsiFile, errors: List<LspAnnotationError>): List<LspAnnotationError> =
    super.postProcessErrors(file, errors)
      .translateNamesInErrors(file)
      .filterQuickFixes(file)

  override fun postProcessInlayHints(hints: List<TypeScriptInlayHint>): List<TypeScriptInlayHint> =
    ReadAction.nonBlocking(Callable {
      super.postProcessInlayHints(hints)
        .reposition(project, file)
    }).executeSynchronously()

  override fun postProcessHover(hover: TypeScriptHover): TypeScriptHover? =
    ReadAction.nonBlocking(Callable {
      hover.translateNamesInDefinition(project, file)
    }).executeSynchronously()
}

private class AngularAssociatedHtmlFile(
  project: Project,
  file: VirtualFile,
) : AngularFileInfo(project, file), KolarAssociatedFile {
  override fun getTargetFile(): VirtualFile? =
    PsiManager.getInstance(project).findFile(file)
      ?.let { getTranspiledDirectiveAndTopLevelSourceFile(it) }
      ?.first
      ?.originalFile
      ?.virtualFile
}

private class AngularTranspiledFile(
  project: Project,
  file: VirtualFile,
) : AngularFileInfo(project, file), KolarTranspiledFile {

  override fun createVirtualCode(
    snapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
  ): KolarVirtualCode? {
    val transpiledFile = getTranspiledDirectiveFile(file)
    val fileName = ctx.normalizePath(file.path)
    if (transpiledFile != null) {
      val newMappings = mutableListOf<CodeMapping>()
      val newAssociatedScriptMappings = mutableMapOf<String, MutableList<CodeMapping>>()

      transpiledFile.fileMappings.values.forEach { fileMapping ->
        val normalizedMappingFileName = ctx.normalizePath(fileMapping.fileName.replace('\\', '/'))
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
        id = "main",
        fsVirtualFile = file,
        snapshot = KolarScriptSnapshot.create(transpiledFile.generatedCode),
        mappings = newMappings,
        associatedScriptMappings = newAssociatedScriptMappings,
      )
    }
    else
      return null
  }

  private fun getTranspiledDirectiveFile(file: VirtualFile): Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile? =
    PsiManager.getInstance(project).findFile(file)
      ?.let { Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveFile(it) }

  private fun createCodeInformation(flags: EnumSet<SourceMappingFlag>, verification: Boolean): KolarCodeInformation =
    KolarCodeInformation(
      format = flags.contains(SourceMappingFlag.FORMAT),
      completion = if (flags.contains(SourceMappingFlag.COMPLETION)) KolarCodeInformation.CompletionInfo.Enabled else null,
      navigation = if (flags.contains(SourceMappingFlag.NAVIGATION)) KolarCodeInformation.NavigationInfo.Enabled else null,
      semantic = if (flags.contains(SourceMappingFlag.SEMANTIC)) KolarCodeInformation.SemanticInfo.Enabled else null,
      structure = flags.contains(SourceMappingFlag.STRUCTURE),
      verification = if (verification) KolarCodeInformation.VerificationInfo.Enabled else null,
      types = flags.contains(SourceMappingFlag.TYPES),
      reverseTypes = flags.contains(SourceMappingFlag.REVERSE_TYPES),
    )

  override fun equals(other: Any?): Boolean =
    other === this || other is AngularTranspiledFile
    && other.file == file
    && other.project == project

  override fun hashCode(): Int =
    file.hashCode() * 31 + project.hashCode()
}

private fun List<LspAnnotationError>.translateNamesInErrors(file: PsiFile): List<LspAnnotationError> =
  getTranspiledDirectiveAndTopLevelSourceFile(file)
    ?.let { (transpiledDirectiveFile, topLevelFile) ->
      translateNamesInErrors(
        this, transpiledDirectiveFile,
        topLevelFile, LspAnnotationError::class,
      ) { error, newDescription, newTooltip ->
        error.copyWith(
          description = newDescription,
          tooltip = newTooltip,
        )
      }
    }
  ?: this

private fun List<TypeScriptInlayHint>.reposition(project: Project, file: VirtualFile): List<TypeScriptInlayHint> {
  val psiFile = PsiManager.getInstance(project).findFile(file) ?: return this
  val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return this
  return mapNotNull {
    repositionInlayHint(psiFile, document, it)
  }
}

private fun List<LspAnnotationError>.filterQuickFixes(file: PsiFile): List<LspAnnotationError> {
  val document = PsiDocumentManager.getInstance(file.project).getDocument(file)
                 ?: return this
  return this.map {
    if (Angular2LanguageServiceQuickFixFilter.accept(file, document, it))
      it
    else
      it.copyWith(quickFixes = emptyList())
  }
}

private fun TypeScriptHover.translateNamesInDefinition(project: Project, file: VirtualFile): TypeScriptHover {
  val content = markdownContent ?: return this
  val definitionStart = content.indexOf("```")
  val definitionEnd = content.indexOf("```", definitionStart + 4)
  if (definitionStart < 0 || definitionEnd < 0) return this

  val psiFile = PsiManager.getInstance(project).findFile(file) ?: return this
  val (transpiledFile, _) = getTranspiledDirectiveAndTopLevelSourceFile(psiFile) ?: return this
  val mappings = mutableMapOf<String, String>()
  transpiledFile.nameMaps[psiFile]?.values?.forEach { map ->
    mappings.putAll(map)
  }
  if (mappings.isEmpty()) return this

  val definition = content.substring(definitionStart + 3, definitionEnd)
  val newDefinition = definition.replace(Regex("(?<=[.<>():+=!*\\s-]|^)(_t[0-9]+)(?=[.<>():+=!*\\s-]|$)")) {
    val id = it.value
    mappings[id] ?: id
  }
  markdownContent = content.replaceRange(definitionStart + 3, definitionEnd, newDefinition)
  return this
}


