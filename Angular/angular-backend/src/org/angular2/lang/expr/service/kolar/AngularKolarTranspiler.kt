package org.angular2.lang.expr.service.kolar

import com.intellij.lang.typescript.kolar.KolarCodegenContext
import com.intellij.lang.typescript.kolar.KolarScriptSnapshot
import com.intellij.lang.typescript.kolar.KolarServiceScript
import com.intellij.lang.typescript.kolar.KolarTranspiler
import com.intellij.lang.typescript.kolar.KolarVirtualCode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.angular2.Angular2DecoratorUtil.isHostBindingExpression
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.expr.Angular2ExprDialect
import org.angular2.lang.expr.service.tcb.Angular2TranspiledDirectiveFileBuilder
import org.angular2.lang.html.Angular2HtmlDialect

internal class AngularKolarTranspiler(private val project: Project) : KolarTranspiler<AngularVirtualCode> {
  override fun getLanguageId(scriptId: String): String? {
    return if (scriptId.endsWith(".html")) "html" else if (scriptId.endsWith(".ts")) "typescript" else null
  }

  override fun isAssociatedFileOnly(scriptId: String, languageId: String): Boolean {
    return languageId == "html"
  }

  override fun supportsInjectedFile(file: PsiFile): Boolean =
    file.language is Angular2ExprDialect || file.language is Angular2HtmlDialect

  override fun preventHighlighting(file: PsiFile): Boolean =
    (file.language is Angular2HtmlDialect || file.language is Angular2ExprDialect)
    && Angular2EntitiesProvider.findTemplateComponent(file) == null
    && !isHostBindingExpression(file)

  override fun createVirtualCode(
    scriptId: String,
    languageId: String,
    snapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
  ): AngularVirtualCode? {
    if (languageId == "typescript" &&
        !scriptId.endsWith(".d.ts") &&
        !scriptId.contains("/node_modules/")) {
      val normalizedScriptId = scriptId.replace('\\', '/')
      val virtualCode = AngularVirtualCode(normalizedScriptId)
      return updateVirtualCode(scriptId, virtualCode, snapshot, ctx)
    }
    return null
  }

  override fun updateVirtualCode(
    scriptId: String,
    virtualCode: AngularVirtualCode,
    newSnapshot: KolarScriptSnapshot,
    ctx: KolarCodegenContext,
  ): AngularVirtualCode {
    val normalizedScriptId = scriptId.replace('\\', '/')
    val transpiledFile = if (ApplicationManager.getApplication().isReadAccessAllowed) {
      getTranspiledDirectiveFile(normalizedScriptId)
    }
    else {
      runBlockingMaybeCancellable {
        readAction {
          getTranspiledDirectiveFile(normalizedScriptId)
        }
      }
    }
    return virtualCode.sourceFileUpdated(newSnapshot, ctx, transpiledFile)
  }

  override fun getServiceScript(root: KolarVirtualCode): KolarServiceScript? {
    if (root !is AngularVirtualCode) return null

    return KolarServiceScript(
      code = root,
      preventLeadingOffset = root.preventLeadingOffset,
      fileName = root.fileName
    )
  }

  private fun getTranspiledDirectiveFile(normalizedScriptId: String): Angular2TranspiledDirectiveFileBuilder.TranspiledDirectiveFile? {
    return LocalFileSystem.getInstance().findFileByPath(normalizedScriptId)
      ?.let { PsiManager.getInstance(project).findFile(it) }
      ?.let { Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveFile(it) }
  }

}