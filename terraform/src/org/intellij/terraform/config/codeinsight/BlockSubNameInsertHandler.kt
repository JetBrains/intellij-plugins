// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.completion.BasicInsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.intellij.terraform.config.actions.ImportProviderService
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.ProviderTier
import org.intellij.terraform.config.model.TfTypeModel
import org.intellij.terraform.config.model.getProviderForBlockType
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.formatter.HclCodeStyleSettings
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLStringLiteral

internal class BlockSubNameInsertHandler(val type: BlockType) : BasicInsertHandler<LookupElement>() {

  val tiersNotToInsert = setOf(ProviderTier.TIER_LOCAL, ProviderTier.TIER_BUILTIN)

  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val project = context.project
    if (project.isDisposed) return

    val editor = context.editor
    val file = context.file

    var element = file.findElementAt(context.startOffset) ?: return
    val parent: PsiElement

    if (element is HCLIdentifier) {
      parent = element.parent ?: return
    } else if (element is HCLStringLiteral) {
      parent = element.parent ?: return
    } else if (HCLTokenTypes.IDENTIFYING_LITERALS.contains(element.node?.elementType)) {
      element = element.parent
      val p = element
      if (p is HCLElement) {
        parent = p as? HCLObject ?: p.parent ?: return
      } else {
        parent = p
      }
    } else {
      return
    }

    if (parent is HCLProperty) {
      // ??? Do nothing
      return
    }
    var offset: Int? = null
    val already: Int
    val expected = type.args
    if (parent is HCLBlock && TfInsertHandlerService.isNextNameOnTheSameLine(element, context.document)) {
      // Count existing arguments and add missing
      val elements = parent.nameElements
      already = elements.size - 1
      val i = elements.indexOf(element)
      assert(i != -1)

      // Locate caret to next argument
      // Do not invoke completion for last name
      if (i < elements.lastIndex && i + 1 != elements.lastIndex) {
        val next = elements[i + 1]
        if (next.textContains('"') && next.textLength < 3) {
          editor.caretModel.moveToOffset(next.textRange.endOffset - 1)
          TfInsertHandlerService.scheduleBasicCompletion(context)
        }
      }
    } else {
      return
    }

    if (already < expected) {
      offset = editor.caretModel.offset + 2
      TfInsertHandlerService.addArguments(expected - already, editor)
      if (expected - already != 1) { // Do not invoke completion for last name
        TfInsertHandlerService.scheduleBasicCompletion(context)
      }
    }

    val settings = CodeStyle.getCustomSettings(file, HclCodeStyleSettings::class.java)
    if (settings.IMPORT_PROVIDERS_AUTOMATICALLY) {
      val provider = getProviderForBlockType(type)
      if (provider != null && provider.tier !in tiersNotToInsert && !TfTypeModel.collectProviderLocalNames(file).containsValue(provider.fullName)) {
        ImportProviderService.getInstance(project).addSelectedProvider(provider, file.createSmartPointer())
      }
    }
    PsiDocumentManager.getInstance(project).commitDocument(editor.document)

    if (offset != null) {
      editor.caretModel.moveToOffset(offset)
    }
    if (type.properties.isNotEmpty()) {
      TfInsertHandlerService.getInstance(project).addBlockRequiredProperties(file, editor, project)
    }
  }
}