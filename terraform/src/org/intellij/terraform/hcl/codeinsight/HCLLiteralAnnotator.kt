// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.ide.lightEdit.LightEditCompatible
import com.intellij.lang.annotation.AnnotationBuilder
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.intellij.terraform.config.inspection.TfVARSIncorrectElementInspection
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLSyntaxHighlighter
import org.intellij.terraform.hcl.psi.*
import org.jetbrains.annotations.Nls

/**
 * Inspired by com.intellij.json.codeinsight.JsonLiteralAnnotator
 */
class HCLLiteralAnnotator : Annotator, DumbAware, LightEditCompatible {

  private val DEBUG = ApplicationManager.getApplication().isUnitTestMode

  private fun addBlockNameAnnotation(holder: AnnotationHolder,
                                     @Nls name: String,
                                     textAttributes: TextAttributesKey,
                                     unifix: LocalQuickFixAndIntentionActionOnPsiElement?) {
    var builder: AnnotationBuilder
    if (DEBUG) {
      builder = holder.newAnnotation(HighlightSeverity.INFORMATION, name).textAttributes(textAttributes)
    }
    else {
      builder = holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(textAttributes)
    }
    if (unifix != null) {
      builder = builder.newFix(unifix).universal().registerFix()
    }
    builder.create()
  }

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (element is HCLExpression && HCLPsiUtil.isPropertyKey(element)) {
      if (DEBUG) {
        holder.newAnnotation(HighlightSeverity.INFORMATION,
                             HCLBundle.message("hcl.literal.annotator.property.key")).textAttributes(HCLSyntaxHighlighter.HCL_PROPERTY_KEY).create()
      }
      else {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(HCLSyntaxHighlighter.HCL_PROPERTY_KEY).create()
      }
    }
    if (element is LeafPsiElement) {
      val parent = element.parent
      if (parent is HCLForIntro) {
        if (element.textMatches("for") || element.textMatches("in")) {
          holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(HCLSyntaxHighlighter.HCL_KEYWORD).create()
        }
      }
    }
    if (element is HCLStringLiteral || element is HCLIdentifier) {
      val parent = element.parent
      if (parent is HCLBlock) {
        val ne = parent.nameElements
        if (ne.size == 1 && ne[0] === element) {
          if (DEBUG) {
            holder.newAnnotation(HighlightSeverity.INFORMATION,
                                 HCLBundle.message("hcl.literal.annotator.block.only.name.identifier")).textAttributes(HCLSyntaxHighlighter.HCL_BLOCK_ONLY_NAME_KEY).create()
          }
          else {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(HCLSyntaxHighlighter.HCL_BLOCK_ONLY_NAME_KEY).create()
          }
        } else for (i in ne.indices) {
          if (ne[i] === element) {
            val fix: TfVARSIncorrectElementInspection.ConvertToHCLStringQuickFix?
            if (i != 0 && element is HCLIdentifier) {
              fix = TfVARSIncorrectElementInspection.ConvertToHCLStringQuickFix(element)
            }
            else {
              fix = null
            }
            when (i) {
              ne.lastIndex -> {
                addBlockNameAnnotation(holder, HCLBundle.message("hcl.literal.annotator.block.name.identifier"), HCLSyntaxHighlighter.HCL_BLOCK_NAME_KEY, fix)
              }
              0 -> {
                addBlockNameAnnotation(holder, HCLBundle.message("hcl.literal.annotator.block.type.1.element"),
                                       HCLSyntaxHighlighter.HCL_BLOCK_FIRST_TYPE_KEY, unifix = null)
              }
              1 -> {
                addBlockNameAnnotation(holder, HCLBundle.message("hcl.literal.annotator.block.type.2.element"), HCLSyntaxHighlighter.HCL_BLOCK_SECOND_TYPE_KEY, fix)
              }
              else -> {
                addBlockNameAnnotation(holder, HCLBundle.message("hcl.literal.annotator.block.type.3.element"), HCLSyntaxHighlighter.HCL_BLOCK_OTHER_TYPES_KEY, fix)
              }
            }

            break
          }
        }
      }
    }
  }
}

