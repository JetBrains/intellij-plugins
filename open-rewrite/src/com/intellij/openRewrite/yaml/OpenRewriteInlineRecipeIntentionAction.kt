package com.intellij.openRewrite.yaml

import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.hint.ParameterInfoTaskRunnerUtil
import com.intellij.codeInsight.intention.HighPriorityAction
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openRewrite.isRecipe
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.ThrowableRunnable
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequence
import java.util.concurrent.Callable
import java.util.function.Consumer
import javax.swing.Icon

internal class OpenRewriteInlineRecipeIntentionAction : IntentionAction, HighPriorityAction, Iconable {
  override fun getFamilyName(): String = OpenRewriteBundle.message("open.rewrite.yaml.inline.recipe")

  override fun startInWriteAction(): Boolean = false

  override fun getText(): String = OpenRewriteBundle.message("open.rewrite.yaml.inline.recipe")

  override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
    if (!isRecipe(psiFile)) return false

    val offset = editor.caretModel.offset
    val element = PsiUtilCore.getElementAtOffset(psiFile, offset).parent
    if (element !is YAMLScalar) return false
    val type = getSequenceItemType(element.parent) ?: return false

    val descriptor =
      OpenRewriteRecipeService.getInstance(project).findDescriptor(element.textValue, psiFile, type) ?: return false
    return descriptor.isComposite
  }

  override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
    if (!FileModificationService.getInstance().prepareFileForWrite(psiFile)) return

    val offset = editor.caretModel.offset
    val scalar = PsiUtilCore.getElementAtOffset(psiFile, offset).parent as? YAMLScalar ?: return
    val recipeName = scalar.textValue
    val parent = scalar.parent ?: return
    val type = getSequenceItemType(parent) ?: return
    val textRange = parent.textRange

    val action = ReadAction.nonBlocking(Callable<String?> {
      val descriptor = OpenRewriteRecipeService.getInstance(project).findDescriptor(recipeName, psiFile, type) ?: return@Callable null
      val document = descriptor.declaration.retrieve() as? YAMLDocument ?: return@Callable null
      val sequence = (document.topLevelValue as? YAMLMapping)?.getKeyValueByKey(type.listKey)?.value as? YAMLSequence
                     ?: return@Callable null
      sequence.text
    })
      .coalesceBy(this, project)
      .inSmartMode(project)
      .expireWhen {
        if (editor.isDisposed || !psiFile.isValid) return@expireWhen true

        val available = isAvailable(project, editor, psiFile)
        if (!available) {
          HintManager.getInstance().showInformationHint(editor,
                                                        OpenRewriteBundle.message("open.rewrite.yaml.inline.recipe.modified"))
        }
        return@expireWhen !available
      }

    val runnable = Runnable {
      ParameterInfoTaskRunnerUtil.runTask(project, action, Consumer { text ->
        if (text == null) {
          HintManager.getInstance().showInformationHint(editor,
                                                        OpenRewriteBundle.message("open.rewrite.yaml.inline.recipe.not.found", recipeName))
          return@Consumer
        }
        WriteCommandAction.writeCommandAction(project)
          .withName(OpenRewriteBundle.message("open.rewrite.yaml.inline.recipe.command"))
          .run(ThrowableRunnable {
            editor.document.replaceString(textRange.startOffset, textRange.endOffset, text)
          })
      }, OpenRewriteBundle.message("open.rewrite.yaml.inline.recipe"), editor)
    }

    // Invoke late to ensure the intention popup is closed and the editor is a focus owner.
    ApplicationManager.getApplication().invokeLater(runnable) { editor.isDisposed }
  }

  override fun getIcon(flags: Int): Icon = OpenRewriteIcons.OpenRewrite
}