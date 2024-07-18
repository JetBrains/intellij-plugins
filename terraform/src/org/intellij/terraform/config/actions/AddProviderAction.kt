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
import com.intellij.openapi.application.readAndWriteAction
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
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.concurrency.annotations.RequiresWriteLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.codeinsight.TfInsertHandlerService
import org.intellij.terraform.config.codeinsight.TerraformCompletionUtil
import org.intellij.terraform.config.codeinsight.TfModelHelper.getAllTypesForBlockByIdentifier
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
    val possibleTypes = (startElement as? HCLBlock)?.createSmartPointer()?.let { getAllTypesForBlockByIdentifier(it) } ?: emptyList()
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
    if (editor != null && startElement is HCLBlock) {//It runs in write action, so we can get pointers etc. safely
      project.service<ImportProviderService>().addProvider(editor, startElement.createSmartPointer())
    }
  }

  override fun isAvailable(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement): Boolean {
    return editor != null
           && startElement is HCLBlock
           && startElement.nameElements.size >= 2
  }
}

@Service(Service.Level.PROJECT)
private class ImportProviderService(val coroutineScope: CoroutineScope) {

  private fun showResourceSelectionPopup(title: String, possibleTypes: List<BlockType>, editor: Editor, filePointer: SmartPsiElementPointer<PsiFile>) {
    JBPopupFactory.getInstance()
      .createListPopup(SelectUnknownResourceStep(title, possibleTypes, filePointer, coroutineScope))
      .showInBestPositionFor(editor)
  }

  @RequiresReadLock
  fun showProvidersNotFoundBalloon(startElementPointer: SmartPsiElementPointer<HCLBlock>, editor: Editor) {
    val startElement = startElementPointer.element ?: return
    val blockType = startElement.getNameElementUnquoted(0) ?: return
    val blockIdentifier = startElement.getNameElementUnquoted(1) ?: return
    val resourcePrefix = TypeModel.getResourcePrefix(blockIdentifier)
    JBPopupFactory.getInstance()
      .createHtmlTextBalloonBuilder(HCLBundle.message("popup.content.could.not.find.bundled.provider.for", blockType, blockIdentifier, resourcePrefix), MessageType.WARNING)
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


  fun addProvider(editor: Editor, blockPointer: SmartPsiElementPointer<HCLBlock>) {
    coroutineScope.launch(Dispatchers.Default) {
      val possibleTypes = readAction { getAllTypesForBlockByIdentifier(blockPointer) }
      if (possibleTypes.isEmpty()) {
        withContext(Dispatchers.EDT) {
          showProvidersNotFoundBalloon(blockPointer, editor)
        }
      }
      else {
        val title = HCLBundle.message("terraform.add.provider.dialog.title", possibleTypes.first().name.replaceFirstChar { it.titlecase() })
        val filePointer = readAction { blockPointer.element?.containingFile?.createSmartPointer() } ?: return@launch
        if (possibleTypes.size == 1) {
          addRequiredProvider(filePointer, title, possibleTypes.first())
        }
        else {
          withContext(Dispatchers.EDT) {
            showResourceSelectionPopup(title, possibleTypes, editor, filePointer)
          }
        }
      }
    }
  }
}

internal suspend fun addRequiredProvider(filePointer: SmartPsiElementPointer<PsiFile>, commandName: @Nls String, blockType: BlockType) {
  readAndWriteAction {
    val file = filePointer.element ?: return@readAndWriteAction value(Unit)
    val project = file.project
    writeCommandAction(project, commandName) {
      getProviderForBlockType(blockType)?.let { TfInsertHandlerService.getInstance(project).addRequiredProvidersBlockToConfig(it, file) }
    }
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
    if (selectedValue != null) {
      coroutineScope.launch(Dispatchers.Default) {
        val commandName = HCLBundle.message("terraform.add.provider.dialog.title", selectedValue.literal)
        addRequiredProvider(pointer, commandName, selectedValue)
      }
    }
    return FINAL_CHOICE
  }
}
