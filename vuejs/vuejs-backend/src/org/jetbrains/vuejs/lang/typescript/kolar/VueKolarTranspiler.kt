package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.javascript.typeEngine.JSServicePoweredTypeEngineUsageContext
import com.intellij.lang.javascript.dialects.ECMA6LanguageDialect
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.typescript.kolar.KolarCodegenContext
import com.intellij.lang.typescript.kolar.KolarFileInfo
import com.intellij.lang.typescript.kolar.KolarScriptSnapshot
import com.intellij.lang.typescript.kolar.KolarTranspiledFile
import com.intellij.lang.typescript.kolar.KolarTranspiler
import com.intellij.lang.typescript.kolar.KolarVirtualCode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ThreeState
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.html.hasVueFileType

class VueKolarTranspiler(
  private val project: Project,
) : KolarTranspiler {

  override fun isHighlightingCandidate(file: VirtualFile): Boolean =
    file.isInLocalFileSystem
    && file.hasVueFileType

  override fun isEnabled(file: VirtualFile): Boolean =
    file.isInLocalFileSystem
    && file.hasVueFileType
    && isVueContext(file, project)

  override fun getFileInfo(file: VirtualFile): KolarFileInfo? {
    if (NodeModuleUtil.hasNodeModulesDirInPath(file, null))
      return null

    return VueTranspiledFile(project, file)
  }

  override fun getRelatedFiles(files: Collection<VirtualFile>): Collection<VirtualFile> =
    VueKolarContext.getRelatedFiles(project, files)

  override fun supportsInjectedFile(file: PsiFile): Boolean =
    file.language is VueJSLanguage
    || file.language is VueTSLanguage

  override fun supportsTypeEvaluation(
    virtualFile: VirtualFile,
    element: PsiElement,
  ): Boolean =
    when (element.language) {
      // setup
      TypeScriptLanguageDialect,
      ECMA6LanguageDialect,

      // interpolations
      VueTSLanguage,
      VueJSLanguage,
        -> true

      else -> false
    }

  override fun isTypeEvaluationEnabledInUsageContext(
    usageContext: JSServicePoweredTypeEngineUsageContext,
  ): ThreeState =
    ThreeState.YES
}

private data class VueTranspiledFile(
  private val project: Project,
  private val file: VirtualFile,
) : KolarTranspiledFile {

  override fun createVirtualCode(
    snapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
  ): KolarVirtualCode? {
    val psiFile = file.findPsiFile(project) as? VueFile
                  ?: return null

    // The mappings point into the PSI text, so they are valid only for the same text as the snapshot.
    if (!psiFile.viewProvider.contents.contentEquals(snapshot.charSeq)) {
      val message = "PSI text of ${file.path} differs from the Kolar script snapshot"
      if (ApplicationManager.getApplication().isUnitTestMode)
        thisLogger().error(message)
      else
        thisLogger().warn(message)
      return null
    }

    val transpiledFile = VueTranspiledFileBuilder.getInstance()
                           .getTranspiledFile(psiFile)
                         ?: return null

    return KolarVirtualCode(
      id = "main",
      fsVirtualFile = file,
      snapshot = KolarScriptSnapshot.create(transpiledFile.generatedCode),
      mappings = transpiledFile.mappings,
      associatedScriptMappings = emptyMap(),
    )
  }
}
