package org.angular2.lang.expr.service.kolar

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.lang.typescript.kolar.KolarCodegenContext
import com.intellij.lang.typescript.kolar.KolarScriptSnapshot
import com.intellij.lang.typescript.kolar.KolarServiceScript
import com.intellij.lang.typescript.kolar.KolarTranspiler
import com.intellij.lang.typescript.kolar.KolarVirtualCode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.asSafely
import com.intellij.util.indexing.SubstitutedFileType
import org.angular2.Angular2DecoratorUtil.isHostBindingExpression
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder
import org.angular2.lang.html.Angular2HtmlDialect
import org.intellij.images.fileTypes.impl.SvgFileType

private const val ANGULAR_HTML_LANG = "angular-html"

internal class AngularKolarTranspiler(private val project: Project) : KolarTranspiler<AngularVirtualCode> {

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
  ): AngularVirtualCode? {
    val fileName = file.name
    if (languageId == "typescript" &&
        !fileName.endsWith(".d.ts") &&
        !NodeModuleUtil.hasNodeModulesDirInPath(file, null)) {
      val virtualCode = AngularVirtualCode(file)
      return updateVirtualCode(file, virtualCode, snapshot, ctx)
    }
    return null
  }

  override fun updateVirtualCode(
    file: VirtualFile,
    virtualCode: AngularVirtualCode,
    newSnapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
  ): AngularVirtualCode {
    val transpiledFile = if (ApplicationManager.getApplication().isReadAccessAllowed)
      getTranspiledDirectiveFile(file)
    else
      ReadAction.computeCancellable<Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile?, Throwable> {
        getTranspiledDirectiveFile(file)
      }
    return virtualCode.sourceFileUpdated(newSnapshot, ctx, transpiledFile)
  }

  override fun getServiceScript(root: KolarVirtualCode): KolarServiceScript? {
    if (root !is AngularVirtualCode) return null

    return KolarServiceScript(
      code = root,
      preventLeadingOffset = root.preventLeadingOffset,
      fileName = root.file.path
    )
  }

  private fun getTranspiledDirectiveFile(file: VirtualFile): Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile? =
    PsiManager.getInstance(project).findFile(file)
      ?.let { Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveFile(it) }

  private fun isAcceptableHtmlFile(file: VirtualFile): Boolean =
    file.isInLocalFileSystem && file.fileType.let {
      (it is HtmlFileType || it is SvgFileType) && SubstitutedFileType.substituteFileType(file, it, project)
        .asSafely<SubstitutedFileType>()?.language is Angular2HtmlDialect
    }

}