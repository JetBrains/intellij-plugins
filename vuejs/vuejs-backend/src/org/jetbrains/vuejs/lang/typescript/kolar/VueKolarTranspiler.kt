package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.typescript.kolar.KolarCodegenContext
import com.intellij.lang.typescript.kolar.KolarFileInfo
import com.intellij.lang.typescript.kolar.KolarScriptSnapshot
import com.intellij.lang.typescript.kolar.KolarTranspiledFile
import com.intellij.lang.typescript.kolar.KolarTranspiler
import com.intellij.lang.typescript.kolar.KolarVirtualCode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.isVueFile
import org.jetbrains.vuejs.lang.typescript.kolar.VueTranspiledFileBuilder.TranspiledFile

internal class VueKolarTranspiler(
  private val project: Project,
) : KolarTranspiler {

  override fun isHighlightingCandidate(file: VirtualFile): Boolean =
    file.isInLocalFileSystem
    && file.fileType is VueFileType

  override fun isEnabled(file: VirtualFile): Boolean =
    isVueContext(file, project)
    && file.isVueFile

  override fun getFileInfo(file: VirtualFile): KolarFileInfo? {
    if (NodeModuleUtil.hasNodeModulesDirInPath(file, null))
      return null

    return VueTranspiledFile(project, file)
  }

  override fun supportsInjectedFile(file: PsiFile): Boolean =
    file.language is VueJSLanguage
    || file.language is VueTSLanguage
}

private data class VueTranspiledFile(
  val project: Project,
  val file: VirtualFile,
) : KolarTranspiledFile {

  override fun createVirtualCode(
    snapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
  ): KolarVirtualCode? {
    val transpiledFile = getTranspiledFile()
                         ?: return null

    return KolarVirtualCode(
      id = "main",
      fsVirtualFile = file,
      snapshot = KolarScriptSnapshot.create(transpiledFile.generatedCode),
      mappings = transpiledFile.mappings,
      associatedScriptMappings = emptyMap(),
    )
  }

  private fun getTranspiledFile(): TranspiledFile? =
    PsiManager.getInstance(project).findFile(file)
      ?.let { VueTranspiledFileBuilder.getTranspiledFile(it) }
}
