// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.BasicInsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.components.service
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.TypeModel
import org.intellij.terraform.config.model.getProviderForBlockType
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.*

internal class BlockSubNameInsertHandler(val type: BlockType) : BasicInsertHandler<LookupElement>() {
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
    val insertHandlerService = project.service<InsertHandlerService>()
    if (parent is HCLBlock && insertHandlerService.isNextNameOnTheSameLine(element, context.document)) {
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
          insertHandlerService.scheduleBasicCompletion(context)
        }
      }
    } else {
      return
    }

    if (already < expected) {
      offset = editor.caretModel.offset + 2
      insertHandlerService.addArguments(expected - already, editor)
      if (expected - already != 1) { // Do not invoke completion for last name
        insertHandlerService.scheduleBasicCompletion(context)
      }
    }

    val provider = getProviderForBlockType(type)
    if (provider != null && !TypeModel.collectProviderLocalNames(file).containsKey(provider.type)) {
      insertHandlerService.addRequiredProvidersBlock(provider, file)
    }

    if (offset != null) {
      editor.caretModel.moveToOffset(offset)
    }
    if (type.properties.isNotEmpty()) {
      insertHandlerService.addHCLBlockRequiredPropertiesAsync(file, editor, project)
    }
    PsiDocumentManager.getInstance(project).commitDocument(editor.document)
  }
}