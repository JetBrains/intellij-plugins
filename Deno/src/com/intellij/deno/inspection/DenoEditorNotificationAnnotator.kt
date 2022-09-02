package com.intellij.deno.inspection

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoSettings
import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclaration
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.dialects.JSLanguageFeature
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException

class DenoEditorNotificationAnnotator : Annotator {
  companion object {
    const val DISABLE_KEY = "use.deno.dismiss"
  }

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element !is JSFile ||
        !DialectDetector.hasFeature(element, JSLanguageFeature.IMPORT_DECLARATIONS) ||
        DenoSettings.getService(element.project).isUseDeno() ||
        PropertiesComponent.getInstance(element.project).getBoolean(DISABLE_KEY, false)) return

    val imports = ES6ImportPsiUtil.getImportDeclarations(element)
    var hasDenoMarkers = false
    for (es6Import in imports) {
      if (es6Import is ES6ImportExportDeclaration) {
        val importModuleText = ES6ImportPsiUtil.getUnquotedFromClauseOrModuleText(es6Import)
        if (importModuleText != null) {
          if (JSFileReferencesUtil.findExtension(importModuleText,
                                                 TypeScriptUtil.TYPESCRIPT_EXTENSIONS) != null) {
            hasDenoMarkers = true
            break
          }
        }
      }
    }
    if (!hasDenoMarkers) {
      val refDeno = Ref.create(false)
      (object : JSRecursiveWalkingElementVisitor() {
        override fun visitJSReferenceExpression(node: JSReferenceExpression) {
          if (node.qualifier == null && "Deno" == node.referenceName) {
            refDeno.set(true)
            stopWalking()
          }
          super.visitJSReferenceExpression(node)
        }
      }).visitFile(element)
      hasDenoMarkers = refDeno.get()
    }

    if (hasDenoMarkers) {
      addNotification(holder, element)
    }
  }

  private fun addNotification(holder: AnnotationHolder, file: JSFile) {
    holder.newAnnotation(HighlightSeverity.WARNING, DenoBundle.message("deno.use"))
      .range(file)
      .fileLevel()
      .withFix(createApply())
      .withFix(createDismissIntention()).create()
  }


  private fun createDismissIntention(): IntentionAction {
    return object : IntentionAction {
      override fun getText(): String = DenoBundle.message("deno.use.dismiss")

      override fun getFamilyName(): String = DenoBundle.message("deno.use")

      override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean = true

      @Throws(IncorrectOperationException::class)
      override fun invoke(project: Project,
                          editor: Editor,
                          file: PsiFile) {
        PropertiesComponent.getInstance(project).setValue(DISABLE_KEY, true)
        DaemonCodeAnalyzer.getInstance(project).restart()
      }

      override fun startInWriteAction(): Boolean = false
    }
  }

  private fun createApply(): IntentionAction {
    return object : IntentionAction {
      override fun getText(): String = DenoBundle.message("deno.use.apply")

      override fun getFamilyName(): String = DenoBundle.message("deno.use")

      override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean = true

      @Throws(IncorrectOperationException::class)
      override fun invoke(project: Project,
                          editor: Editor,
                          file: PsiFile) {
        DenoSettings.getService(project).setUseDenoAndReload(true)
      }

      override fun startInWriteAction(): Boolean = false
    }
  }

}