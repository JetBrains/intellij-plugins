// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.command.WriteCommandAction
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
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.codeinsight.InsertHandlersUtil
import org.intellij.terraform.config.codeinsight.TerraformCompletionUtil
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.TypeModelProvider
import org.intellij.terraform.config.model.getProviderForBlockType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.Icons
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
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
    val possibleTypes = if (startElement is HCLBlock && editor != null) getTypesForBlock(startElement) else return
    val blockIdentifier = startElement.getNameElementUnquoted(1) ?: return
    if (possibleTypes.isEmpty()) {
      JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(HCLBundle.message("popup.content.could.not.find.bundled.provider.for", blockIdentifier), MessageType.INFO)
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
      return
    }
    if (possibleTypes.size == 1) {
      getProviderForBlockType(possibleTypes[0])?.let { InsertHandlersUtil.addRequiredProvidersBlock(it, file) }
      return
    }
    val blockTypeName = possibleTypes.first().name.replaceFirstChar { it.titlecase() }
    JBPopupFactory.getInstance()
      .createListPopup(SelectUnknownResourceStep(HCLBundle.message("command.name.add.provider", blockTypeName), possibleTypes, file.createSmartPointer()))
      .showInBestPositionFor(editor)
  }

  private fun getTypesForBlock(element: HCLBlock): List<BlockType> {
    val model = TypeModelProvider.getModel(element)
    val typeString = element.getNameElementUnquoted(0) ?: return emptyList()
    val identifier = element.getNameElementUnquoted(1) ?: return emptyList()
    val types = when (typeString) {
      HCL_RESOURCE_IDENTIFIER -> model.allResources().filter { it.type == identifier }
      HCL_DATASOURCE_IDENTIFIER -> model.allDatasources().filter { it.type == identifier }
      HCL_PROVIDER_IDENTIFIER -> model.allProviders().filter { it.type == identifier }
      else -> emptyList()
    }
    return types
  }

  override fun isAvailable(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement): Boolean {
    return editor != null
           && startElement is HCLBlock
           && startElement.nameElements.size >= 2
  }
}

private class SelectUnknownResourceStep(@NlsSafe title: String, types: List<BlockType>, private val pointer: SmartPsiElementPointer<PsiFile>) : BaseListPopupStep<BlockType>(title, types) {

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
      WriteCommandAction.runWriteCommandAction(file.project, title, null, Runnable {
        getProviderForBlockType(selectedValue)?.let { InsertHandlersUtil.addRequiredProvidersBlock(it, file) }
      }, file)
    }
    return FINAL_CHOICE
  }
}
