// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupElementRenderer
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
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

object CompletionUtil {
  val Scopes: Set<String> = setOf("data", "var", "self", "path", "count", "terraform", "local", "module")
  val GlobalScopes: SortedSet<String> = sortedSetOf("var", "path", "data", "module", "local")
  val RootBlockKeywords: Set<String> = TypeModel.RootBlocks.map(BlockType::literal).toHashSet()
  val RootBlockSorted: List<BlockType> = TypeModel.RootBlocks.sortedBy { it.literal }

  fun create(value: String): LookupElementBuilder {
    return LookupElementBuilder.create(value)
  }

  fun createScope(value: String): LookupElementBuilder {
    var builder = LookupElementBuilder.create(value)
    builder = builder.withInsertHandler(ScopeSelectInsertHandler)
    builder = builder.withRenderer(object : LookupElementRenderer<LookupElement?>() {
      override fun renderElement(element: LookupElement?, presentation: LookupElementPresentation?) {
        presentation?.icon = AllIcons.Nodes.Tag
        presentation?.itemText = element?.lookupString
      }
    })
    return builder
  }

  fun create(f: Function): LookupElementBuilder {
    var builder = LookupElementBuilder.create(f.name)
    builder = builder.withInsertHandler(FunctionInsertHandler)
    builder = builder.withRenderer(object : LookupElementRenderer<LookupElement?>() {
      override fun renderElement(element: LookupElement?, presentation: LookupElementPresentation?) {
        presentation?.icon = AllIcons.Nodes.Method // or Function
        presentation?.itemText = element?.lookupString
      }
    })
    return builder
  }

  fun create(value: PropertyOrBlockType, lookupString: String? = null): LookupElementBuilder {
    var builder = LookupElementBuilder.create(lookupString ?: value.name)
    builder = builder.withRenderer(TerraformLookupElementRenderer())
    return builder
  }

  fun dumpPsiFileModel(element: PsiElement): () -> String {
    return { DebugUtil.psiToString(element.containingFile, true) }
  }

  fun create(value: String, quote: Boolean = true): LookupElementBuilder {
    var builder = LookupElementBuilder.create(value)
    if (quote) {
      builder = builder.withInsertHandler(QuoteInsertHandler)
    }
    return builder
  }

  fun createWithInsertHandler(value: PropertyOrBlockType, lookupString: String? = null): LookupElementBuilder {
    var builder = LookupElementBuilder.create(value, lookupString ?: value.name)
    builder = builder.withRenderer(TerraformLookupElementRenderer())
    if (value is BlockType) {
      builder = builder.withInsertHandler(ResourceBlockNameInsertHandler(value))
    }
    else if (value is PropertyType) {
      builder = builder.withInsertHandler(ResourcePropertyInsertHandler)
    }
    return builder
  }

  fun failIfInUnitTestsMode(position: PsiElement, addition: String? = null) {
    TerraformConfigCompletionContributor.LOG.assertTrue(!ApplicationManager.getApplication().isUnitTestMode, {
      var ret = ""
      if (addition != null) {
        ret = "$addition\n"
      }
      ret += " Position: $position\nFile: " + DebugUtil.psiToString(position.containingFile, true)
      ret
    })
  }

  fun getOriginalObject(parameters: CompletionParameters, obj: HCLObject): HCLObject {
    val originalObject = parameters.originalFile.findElementAt(obj.textRange.startOffset)?.parent
    return originalObject as? HCLObject ?: obj
  }

  fun getClearTextValue(element: PsiElement?): String? {
    return when {
      element == null -> null
      element is HCLIdentifier -> element.id
      element is HCLStringLiteral -> element.value
      element.node?.elementType == HCLElementTypes.ID -> element.text
      HCLTokenTypes.STRING_LITERALS.contains(element.node?.elementType) -> HCLPsiUtil.stripQuotes(element.text)
      else -> return null
    }
  }

  fun getIncomplete(parameters: CompletionParameters): String? {
    val position = parameters.position
    val text = getClearTextValue(position) ?: position.text
    if (text == CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED) return null
    return text.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "").nullize(true)
  }
}