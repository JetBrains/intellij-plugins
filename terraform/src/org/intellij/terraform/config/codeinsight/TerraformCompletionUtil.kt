// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.DebugUtil
import org.intellij.terraform.config.model.*
import org.intellij.terraform.config.model.Function
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hil.codeinsight.FunctionInsertHandler
import org.intellij.terraform.hil.codeinsight.ScopeSelectInsertHandler
import org.intellij.terraform.nullize
import java.util.*

object TerraformCompletionUtil {
  val Scopes: Set<String> = setOf("data", "var", "self", "path", "count", "terraform", "local", "module")
  val GlobalScopes: SortedSet<String> = sortedSetOf("var", "path", "data", "module", "local")
  val RootBlockKeywords: Set<String> = TypeModel.RootBlocks.map(BlockType::literal).toHashSet()
  val RootBlockSorted: List<BlockType> = TypeModel.RootBlocks.sortedBy { it.literal }

  fun createPropertyOrBlockType(value: PropertyOrBlockType, lookupString: String? = null, psiElement: PsiElement? = null): LookupElementBuilder {
    val elementBuilder = when {
      psiElement == null -> LookupElementBuilder.create(value, lookupString ?: value.name)
      else -> LookupElementBuilder.create(value, lookupString ?: value.name).withPsiElement(psiElement)
    }
    return elementBuilder
      .withRenderer(TerraformLookupElementRenderer())
      .withInsertHandler(
        when (value) {
          is BlockType -> ResourceBlockNameInsertHandler(value)
          is PropertyType -> ResourcePropertyInsertHandler
          else -> null
        }
      )
  }

  fun createScope(value: String): LookupElementBuilder = LookupElementBuilder.create(value)
    .withInsertHandler(ScopeSelectInsertHandler)
    .withRenderer(object : LookupElementRenderer<LookupElement?>() {
      override fun renderElement(element: LookupElement?, presentation: LookupElementPresentation?) {
        presentation?.icon = AllIcons.Nodes.Tag
        presentation?.itemText = element?.lookupString
      }
    })

  fun createFunction(function: Function): LookupElementBuilder = LookupElementBuilder.create(function.name)
    .withInsertHandler(FunctionInsertHandler)
    .withRenderer(
      object : LookupElementRenderer<LookupElement?>() {
        override fun renderElement(element: LookupElement?, presentation: LookupElementPresentation?) {
          presentation?.icon = AllIcons.Nodes.Method // or Function
          presentation?.itemText = element?.lookupString
        }
      })

  fun dumpPsiFileModel(element: PsiElement): () -> String = { DebugUtil.psiToString(element.containingFile, true) }

  fun getOriginalObject(parameters: CompletionParameters, obj: HCLObject): HCLObject {
    val originalObject = parameters.originalFile.findElementAt(obj.textRange.startOffset)?.parent
    return originalObject as? HCLObject ?: obj
  }

  fun getClearTextValue(element: PsiElement?): String? = when {
    element == null -> null
    element is HCLIdentifier -> element.id
    element is HCLStringLiteral -> element.value
    element.node?.elementType == HCLElementTypes.ID -> element.text
    HCLTokenTypes.STRING_LITERALS.contains(element.node?.elementType) -> HCLPsiUtil.stripQuotes(element.text)
    else -> null
  }

  fun getIncomplete(parameters: CompletionParameters): String? {
    val position = parameters.position
    val text = getClearTextValue(position) ?: position.text
    if (text == CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED) return null
    return text.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "").nullize(true)
  }
}