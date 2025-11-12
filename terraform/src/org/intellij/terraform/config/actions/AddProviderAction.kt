// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.readAndEdtWriteAction
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
import org.intellij.terraform.config.codeinsight.TfCompletionUtil
import org.intellij.terraform.config.codeinsight.TfInsertHandlerService
import org.intellij.terraform.config.codeinsight.TfModelHelper.getAllTypesForBlockByIdentifier
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.TfTypeModel
import org.intellij.terraform.config.model.getProviderForBlockType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.isTerraformCompatiblePsiFile
import javax.swing.Icon
import javax.swing.event.HyperlinkEvent

internal const val FADEOUT_TIME_MILLIS: Long = 10_000L

internal class AddProviderAction(element: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(element) {

  override fun generatePreview(project: Project, editor: Editor, psiFile: PsiFile): IntentionPreviewInfo {
    val possibleTypes = (startElement as? HCLBlock)?.createSmartPointer()?.let { getAllTypesForBlockByIdentifier(it) } ?: emptyList()
    if (possibleTypes.size == 1) return super.generatePreview(project, editor, psiFile)
    return IntentionPreviewInfo.EMPTY
  }

  override fun getText(): @IntentionName String {
    return HCLBundle.message("action.AddProviderAction.text")
  }

  override fun getFamilyName(): @IntentionFamilyName String {
    return HCLBundle.message("action.AddProviderAction.text")
  }

  override fun invoke(project: Project, psiFile: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
    if (editor != null && startElement is HCLBlock) {
      ImportProviderService.getInstance(project).scheduleAddProvider(editor, startElement.createSmartPointer())
    }
  }

  override fun isAvailable(project: Project, psiFile: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement): Boolean {
    return editor != null && isTerraformCompatiblePsiFile(psiFile)
           && startElement is HCLBlock
           && startElement.nameElements.size >= 2
  }
}

@Service(Service.Level.PROJECT)
internal class ImportProviderService(val coroutineScope: CoroutineScope) {

  private fun showResourceSelectionPopup(title: String, possibleTypes: List<BlockType>, editor: Editor, filePointer: SmartPsiElementPointer<PsiFile>) {
    JBPopupFactory.getInstance()
      .createListPopup(SelectUnknownResourceStep(title, possibleTypes, filePointer))
      .showInBestPositionFor(editor)
  }

  private fun showProvidersNotFoundBalloon(@NlsSafe message: String, editor: Editor) {
    JBPopupFactory.getInstance()
      .createHtmlTextBalloonBuilder(message, MessageType.WARNING)
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

  fun scheduleAddProvider(editor: Editor, blockPointer: SmartPsiElementPointer<HCLBlock>) {
    coroutineScope.launch(Dispatchers.Default) {
      val possibleTypes = readAction { getAllTypesForBlockByIdentifier(blockPointer) }
      if (possibleTypes.isEmpty()) {
        val message = readAction {
          blockPointer.element?.let { block ->
            HCLBundle.message("popup.content.could.not.find.bundled.provider.for",
                              block.getNameElementUnquoted(0) ?: "",
                              block.getNameElementUnquoted(1) ?: "",
                              TfTypeModel.getResourcePrefix(block.getNameElementUnquoted(1) ?: ""))
          }
        } ?: return@launch
        withContext(Dispatchers.EDT) {
          showProvidersNotFoundBalloon(message, editor)
        }
      }
      else {
        val filePointer = readAction { blockPointer.element?.containingFile?.createSmartPointer() } ?: return@launch
        if (possibleTypes.size == 1) {
          addRequiredProvider(possibleTypes.first(), filePointer)
        }
        else {
          withContext(Dispatchers.EDT) {
            val title = HCLBundle.message("terraform.add.provider.dialog.title", possibleTypes.first().name.replaceFirstChar { it.titlecase() })
            showResourceSelectionPopup(title, possibleTypes, editor, filePointer)
          }
        }
      }
    }
  }

  fun addSelectedProvider(blockType: BlockType, filePointer: SmartPsiElementPointer<PsiFile>) {
    coroutineScope.launch(Dispatchers.Default) {
      addRequiredProvider(blockType, filePointer)
    }
  }

  private suspend fun addRequiredProvider(blockType: BlockType, filePointer: SmartPsiElementPointer<PsiFile>) {
    readAndEdtWriteAction {
      val file = filePointer.element ?: return@readAndEdtWriteAction value(Unit)
      val project = file.project
      val commandName = HCLBundle.message("terraform.add.provider.dialog.title", blockType.literal)

      val terraformBlock = TfTypeModel.getTerraformBlockInModule(file)
      writeCommandAction(project, commandName) {
        getProviderForBlockType(blockType)?.let { TfInsertHandlerService.getInstance(project).addRequiredProvidersBlockToConfig(it, terraformBlock, file) }
      }
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): ImportProviderService = project.service()
  }
}


private class SelectUnknownResourceStep(
  @NlsSafe title: String,
  types: List<BlockType>,
  private val pointer: SmartPsiElementPointer<PsiFile>,
) : BaseListPopupStep<BlockType>(title, types) {

  override fun getTextFor(value: BlockType?): String {
    return value?.let { TfCompletionUtil.buildResourceFullString(value) }
           ?: HCLBundle.message("unknown.resource.identifier.inspection.display.name")
  }

  override fun getIconFor(value: BlockType?): Icon? {
    return value?.let { TfCompletionUtil.getLookupIcon(pointer.element ?: return@getIconFor null) }
           ?: AllIcons.General.QuestionDialog
  }

  override fun onChosen(selectedValue: BlockType?, finalChoice: Boolean): PopupStep<*>? {
    if (selectedValue != null) {
      val project = pointer.project
      ImportProviderService.getInstance(project).addSelectedProvider(selectedValue, pointer)
    }
    return FINAL_CHOICE
  }
}
