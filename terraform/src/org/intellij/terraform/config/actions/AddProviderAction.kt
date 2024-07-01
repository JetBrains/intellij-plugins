// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.codeinsight.InsertHandlerService
import org.intellij.terraform.config.codeinsight.TerraformCompletionUtil
import org.intellij.terraform.config.codeinsight.TfModelHelper.getTypesForBlock
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.TypeModel
import org.intellij.terraform.config.model.getProviderForBlockType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.jetbrains.annotations.Nls
import javax.swing.Icon
import javax.swing.event.HyperlinkEvent

internal const val FADEOUT_TIME_MILLIS: Long = 10_000L

internal class AddProviderAction(element: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(element) {

  override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
    val possibleTypes = getTypesForBlock(startElement as HCLBlock)
    if (possibleTypes.size == 1) return super.generatePreview(project, editor, file)
    return IntentionPreviewInfo.EMPTY
  }

  override fun getText(): @IntentionName String {
    return HCLBundle.message("action.AddProviderAction.text")
  }

  override fun getFamilyName(): @IntentionFamilyName String {
    return HCLBundle.message("action.AddProviderAction.text")
  }

  override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
    project.service<ImportProviderService>().addProvider(file, editor, startElement)
  }

  override fun isAvailable(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement): Boolean {
    return editor != null
           && startElement is HCLBlock
           && startElement.nameElements.size >= 2
  }
}

@Service(Service.Level.PROJECT)
private class ImportProviderService(val coroutineScope: CoroutineScope) {

  private fun showResourceSelectionPopup(title: String, possibleTypes: List<BlockType>, editor: Editor, file: PsiFile) {
    JBPopupFactory.getInstance()
      .createListPopup(SelectUnknownResourceStep(title, possibleTypes, file.createSmartPointer(), coroutineScope))
      .showInBestPositionFor(editor)
  }

  fun showProvidersNotFoundBalloon(startElement: HCLBlock, editor: Editor) {
    val blockIdentifier = startElement.getNameElementUnquoted(1) ?: return
    val resourcePrefix = TypeModel.getResourcePrefix(blockIdentifier)
    JBPopupFactory.getInstance()
      .createHtmlTextBalloonBuilder(HCLBundle.message("popup.content.could.not.find.bundled.provider.for", blockIdentifier, resourcePrefix), MessageType.WARNING)
      { e ->
        if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
          if (e.url != null) {
            BrowserUtil.browse(e.url)
          }
        }
      }
      .setHideOnLinkClick(true)
      .setHideOnClickOutside(true)
      .setFadeoutTime(FADEOUT_TIME_MILLIS)
      .createBalloon()
      .show(JBPopupFactory.getInstance().guessBestPopupLocation(editor), Balloon.Position.below)
  }


  fun addProvider(file: PsiFile, editor: Editor?, startElement: PsiElement) {
    coroutineScope.launch(Dispatchers.Default) {
      val possibleTypes = if (startElement is HCLBlock && editor != null) readAction { getTypesForBlock(startElement) } else return@launch
      if (possibleTypes.isEmpty()) {
        withContext(Dispatchers.EDT) {
          showProvidersNotFoundBalloon(startElement, editor)
        }
      }
      else {
        val title = HCLBundle.message("terraform.add.provider.dialog.title", possibleTypes.first().name.replaceFirstChar { it.titlecase() })
        if (possibleTypes.size == 1) {
          addRequiredProvider(file, title, possibleTypes.first())
        }
        else {
          withContext(Dispatchers.EDT) {
            showResourceSelectionPopup(title, possibleTypes, editor, file)
          }
        }
      }
    }
  }
}

private suspend fun addRequiredProvider(file: PsiFile, commandName: @Nls String, blockType: BlockType) {
  val project = file.project
  writeCommandAction(project, commandName) {
    val insertHandlerService = project.service<InsertHandlerService>()
    getProviderForBlockType(blockType)?.let { insertHandlerService.addRequiredProvidersBlock(it, file) }
  }
}


private class SelectUnknownResourceStep(
  @NlsSafe title: String, types: List<BlockType>,
  private val pointer: SmartPsiElementPointer<PsiFile>,
  val coroutineScope: CoroutineScope,
) : BaseListPopupStep<BlockType>(title, types) {

  override fun getTextFor(value: BlockType?): String {
    return value?.let { TerraformCompletionUtil.buildResourceFullString(value) }
           ?: HCLBundle.message("unknown.resource.identifier.inspection.display.name")
  }

  override fun getIconFor(value: BlockType?): Icon? {
    return value?.let { TerraformIcons.Terraform }
           ?: AllIcons.General.QuestionDialog
  }

  override fun onChosen(selectedValue: BlockType?, finalChoice: Boolean): PopupStep<*>? {
    val file = selectedValue?.let { pointer.element }
    if (file != null && file.isWritable) {
      coroutineScope.launch(Dispatchers.Default) {
        val commandName = HCLBundle.message("terraform.add.provider.dialog.title", selectedValue.literal)
        addRequiredProvider(file, commandName, selectedValue)
      }
    }
    return FINAL_CHOICE
  }
}
