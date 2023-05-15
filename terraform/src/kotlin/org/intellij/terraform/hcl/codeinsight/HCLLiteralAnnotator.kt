// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.lang.annotation.AnnotationBuilder
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.lang.annotations.Language
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLSyntaxHighlighterFactory
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.codeinsight.TerraformConfigCompletionContributor
import org.intellij.terraform.config.inspection.TFVARSIncorrectElementInspection
import org.intellij.terraform.config.model.getTerraformModule
import org.jetbrains.annotations.Nls
import java.util.regex.Pattern

/**
 * Inspired by com.intellij.json.codeinsight.JsonLiteralAnnotator
 */
class HCLLiteralAnnotator : Annotator {
  companion object {
    // TODO: Check HCL supported escapes
    @Language("RegExp")
    private val COMMON_REGEX = "[\\\\abfntrv]|([0-7]{3})|(X[0-9a-fA-F]{2})|(u[0-9a-fA-F]{4})|(U[0-9a-fA-F]{8})"
    private val DQS_VALID_ESCAPE = Pattern.compile("\\\\(\"|$COMMON_REGEX)")
    private val SQS_VALID_ESCAPE = Pattern.compile("\\\\(\'|$COMMON_REGEX)")
    // TODO: AFAIK that should be handled by lexer/parser
    private val VALID_NUMBER_LITERAL = Pattern.compile("-?(0x)?([0-9])\\d*(\\.\\d+)?([e][+-]?\\d+)?([kmgb][b]?)?", Pattern.CASE_INSENSITIVE)
    private val VALID_HEX_NUMBER_LITERAL = Pattern.compile("-?(0x)?([0-9a-f])+", Pattern.CASE_INSENSITIVE)

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

    fun isUnderPropertyUnderPropertyWithObjectValue(element: PsiElement?): Boolean {
      val property = PsiTreeUtil.getParentOfType(element, HCLProperty::class.java, true) ?: return false
      return property.parent is HCLObject && property.parent?.parent is HCLProperty
    }

    fun isUnderPropertyUnderPropertyWithObjectValueAndArray(element: PsiElement?): Boolean {
      val property = PsiTreeUtil.getParentOfType(element, HCLProperty::class.java, true) ?: return false
      return property.parent is HCLObject && property.parent?.parent is HCLArray && property.parent?.parent?.parent is HCLProperty
    }

    fun isUnderPropertyInsideObjectArgument(element: PsiElement?): Boolean {
      val property = PsiTreeUtil.getParentOfType(element, HCLProperty::class.java, true) ?: return false
      return property.parent is HCLObject && property.parent?.parent is HCLParameterList
    }
  }

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    val text = HCLPsiUtil.getElementTextWithoutHostEscaping(element)
    if (element is HCLExpression && HCLPsiUtil.isPropertyKey(element)) {
      if (DEBUG) {
        holder.newAnnotation(HighlightSeverity.INFORMATION,
                             HCLBundle.message("hcl.literal.annotator.property.key")).textAttributes(HCLSyntaxHighlighterFactory.HCL_PROPERTY_KEY).create()
      }
      else {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(HCLSyntaxHighlighterFactory.HCL_PROPERTY_KEY).create()
      }
    }
    if (element is LeafPsiElement) {
      val parent = element.parent
      if (parent is HCLForIntro) {
        if (element.textMatches("for") || element.textMatches("in")) {
          holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(HCLSyntaxHighlighterFactory.HCL_KEYWORD).create()
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
                                 HCLBundle.message("hcl.literal.annotator.block.only.name.identifier")).textAttributes(HCLSyntaxHighlighterFactory.HCL_BLOCK_ONLY_NAME_KEY).create()
          }
          else {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(HCLSyntaxHighlighterFactory.HCL_BLOCK_ONLY_NAME_KEY).create()
          }
        } else for (i in ne.indices) {
          if (ne[i] === element) {
            val fix: TFVARSIncorrectElementInspection.ConvertToHCLStringQuickFix?
            if (i != 0 && element is HCLIdentifier) {
              fix = TFVARSIncorrectElementInspection.ConvertToHCLStringQuickFix(element)
            }
            else {
              fix = null
            }
            if (i == ne.lastIndex) {
              addBlockNameAnnotation(holder, HCLBundle.message("hcl.literal.annotator.block.name.identifier"), HCLSyntaxHighlighterFactory.HCL_BLOCK_NAME_KEY, fix)

            } else if (i == 0) {
              addBlockNameAnnotation(holder, HCLBundle.message("hcl.literal.annotator.block.type.1.element"),
                                     HCLSyntaxHighlighterFactory.HCL_BLOCK_FIRST_TYPE_KEY, unifix = null)
            } else if (i == 1) {
              addBlockNameAnnotation(holder, HCLBundle.message("hcl.literal.annotator.block.type.2.element"), HCLSyntaxHighlighterFactory.HCL_BLOCK_SECOND_TYPE_KEY, fix)

            } else {
              addBlockNameAnnotation(holder, HCLBundle.message("hcl.literal.annotator.block.type.3.element"), HCLSyntaxHighlighterFactory.HCL_BLOCK_OTHER_TYPES_KEY, fix)
            }

            break
          }
        }
      }
    }
    if (element is HCLStringLiteral) {
      val length = text.length

      // Check that string literal is closed properly
      if (length <= 1 || text[0] != text[length - 1] || HCLPsiUtil.isEscapedChar(text, length - 1)) {
        holder.newAnnotation(HighlightSeverity.ERROR, HCLBundle.message("hcl.literal.annotator.missing.closing.quote"))
          .withFix(AddClosingQuoteQuickFix(element))
          .newFix(AddClosingQuoteQuickFix(element)).batch().registerFix()
          .create()
      }

      val pattern = when (element.quoteSymbol) {
        '\'' -> SQS_VALID_ESCAPE
        '\"' -> DQS_VALID_ESCAPE
        else -> {
          TerraformConfigCompletionContributor.failIfInUnitTestsMode(element, "Unexpected string quote symbol '${element.quoteSymbol}', start of the string: ${element.text.subSequence(0, 20)}")
          // Assume default string
          DQS_VALID_ESCAPE
        }
      }

      val elementOffset = element.node.startOffset
      for (fragment in element.textFragments) {
        val fragmentText = fragment.getSecond()
        if (fragmentText.length > 1 && fragmentText[0] == '\\' && !pattern.matcher(fragmentText).matches()) {
          val shifted = fragment.getFirst().shiftRight(elementOffset)
          val errText = when (fragmentText[1]) {
            in '0'..'7' -> HCLBundle.message("hcl.literal.annotator.illegal.octal.escape.sequence")
            'X' -> HCLBundle.message("hcl.literal.annotator.illegal.hex.escape.sequence")
            'u', 'U' -> HCLBundle.message("hcl.literal.annotator.illegal.unicode.escape.sequence")
            else -> HCLBundle.message("hcl.literal.annotator.illegal.escape.sequence")
          }
          errText.let { holder.newAnnotation(HighlightSeverity.ERROR, errText).range(shifted).create() }
        }
      }
      // HCL2/TF0.12 compliance
      if (HCLPsiUtil.isPartOfPropertyKey(element) || HCLPsiUtil.isBlockNameElement(element, 0)) {
        if (element.getTerraformModule().isHCL2Supported()) {
          // see test-data/terraform/annotator/HCL2StringKeys.hcl for details of if case
          if (!isUnderPropertyUnderPropertyWithObjectValue(element)
              && !isUnderPropertyUnderPropertyWithObjectValueAndArray(element)
              && !isUnderPropertyInsideObjectArgument(element)) {
            holder.newAnnotation(HighlightSeverity.ERROR, HCLBundle.message("hcl.literal.annotator.argument.names.must.not.be.quoted"))
              .withFix(UnwrapHCLStringQuickFix(element))
              .newFix(UnwrapHCLStringQuickFix(element)).batch().registerFix()
              .create()
          }
        }
      }
    } else if (element is HCLNumberLiteral) {
      if (!VALID_NUMBER_LITERAL.matcher(text).matches() && !VALID_HEX_NUMBER_LITERAL.matcher(text).matches()) {
        holder.newAnnotation(HighlightSeverity.ERROR, HCLBundle.message("hcl.literal.annotator.illegal.number.literal"))
          .create()
      }
    }
  }
}

