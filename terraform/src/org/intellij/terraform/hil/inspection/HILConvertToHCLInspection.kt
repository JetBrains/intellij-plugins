// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.inspection

import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInspection.*
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.tree.CompositePsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.DocumentUtil
import com.intellij.util.SmartList
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.config.model.Types
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.model.getType
import org.intellij.terraform.config.psi.TfElementGenerator
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLExpression
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.HCLPsiUtil.isUnderPropertyInsideObjectArgument
import org.intellij.terraform.hcl.psi.HCLPsiUtil.isUnderPropertyUnderPropertyWithObjectValue
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hil.HILFileType
import org.intellij.terraform.hil.psi.*
import org.intellij.terraform.isTerraformCompatiblePsiFile
import java.util.function.BiConsumer

class HILConvertToHCLInspection : LocalInspectionTool(), CleanupLocalInspectionTool {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return isTerraformCompatiblePsiFile(file) || file.language == HCLLanguage
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return MyEV(holder)
  }

  inner class MyEV(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitStringLiteral(o: HCLStringLiteral) {
      if (!o.text.contains("\${")) return
      if (o.textFragments.any { it.second.startsWith("%{") }) return

      val module = o.getTerraformModule()
      if (!module.isHCL2Supported()) return

      val convertible = SmartList<TextRange>()

      findConvertibleInjections(o, BiConsumer { range, _ -> convertible.add(range) })

      // For now Terraform 0.12 does not support string concatenation via '+' operator
      if (convertible.size != 1) return
      // For same reason we should match string fully
      if (convertible.first().length != o.textLength - 2) return
      holder.registerProblem(o, HCLBundle.message("hil.convert.to.hcl.inspection.interpolation.could.be.replaced.with.hcl2.message"), ProblemHighlightType.WEAK_WARNING, ConvertToHCLFix(o))
    }
  }

  class ConvertToHCLFix(e: HCLStringLiteral) : LocalQuickFixAndIntentionActionOnPsiElement(e), BatchQuickFix {
    override fun getText(): String = HCLBundle.message("hil.convert.to.hcl.inspection.convert.to.hcl2.quick.fix.text")
    override fun getFamilyName(): String = text

    override fun startInWriteAction(): Boolean = false

    override fun isAvailable(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Boolean {
      if (file.language !in listOf(HCLLanguage, TerraformLanguage)) return false

      return super.isAvailable(project, file, startElement, endElement)
    }

    override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
      if (!FileModificationService.getInstance().prepareFileForWrite(file)) return
      val literal = startElement as? HCLStringLiteral ?: return
      val newValue = getReplacementValue(project, literal)
      WriteCommandAction.writeCommandAction(project).withName(text).withGroupId(familyName).run<Throwable> {
        replace(project, file, literal, newValue)
      }
    }

    override fun applyFix(project: Project, descriptors: Array<out CommonProblemDescriptor>, psiElementsToIgnore: MutableList<PsiElement>, refreshViews: Runnable?) {
      val targets = ArrayList<HCLStringLiteral>()
      for (descriptor in descriptors) {
        descriptor.fixes?.filterIsInstance<ConvertToHCLFix>()?.map { it.startElement }?.filterIsInstanceTo(targets, HCLStringLiteral::class.java)
      }

      if (!FileModificationService.getInstance().preparePsiElementsForWrite(targets)) return

      DocumentUtil.writeInRunUndoTransparentAction {
        targets.forEach {
          val replacement = getReplacementValue(project, it)
          replace(project, it.containingFile, it, replacement)
        }
      }
      psiElementsToIgnore.addAll(targets)
      refreshViews?.run()
    }

    companion object {
      private fun getReplacementText(element: HCLStringLiteral): String {
        val injections = SmartList<Pair<TextRange, String>>()
        findConvertibleInjections(element, BiConsumer { range, il ->
          injections.add(range to convert(il))
        })
        injections.sortWith(compareBy { it.first.startOffset })

        val initialText = element.text
        val parts = SmartList<String>()

        var offset = 1
        for (injection in injections) {
          val range = injection.first
          if (range.startOffset > offset) {
            parts.add("\"" + initialText.substring(offset, range.startOffset) + "\"")
          }
          parts.add(injection.second)
          offset = range.endOffset
        }
        if (offset < (initialText.length - 1)) {
          parts.add("\"" + initialText.substring(offset, initialText.length - 1) + "\"")
        }
        if (parts.size == 1 && HCLPsiUtil.isPropertyKey(element) && (isUnderPropertyUnderPropertyWithObjectValue(element) || isUnderPropertyInsideObjectArgument(element))) {
          // special case for object property key
          return "(${parts.first()})"
        }
        return parts.joinToString(separator = " + ")
      }

      @JvmStatic
      fun getReplacementValue(project: Project, literal: HCLStringLiteral): HCLExpression {
        return TfElementGenerator(project).createValue(getReplacementText(literal))
      }

      private fun replace(project: Project, file: PsiFile, element: HCLExpression, replacement: HCLExpression) {
        CodeStyleManager.getInstance(project).performActionWithFormatterDisabled {
          element.replace(replacement)
          file.subtreeChanged()
        }
      }
    }
  }


  companion object {

    private fun isConvertible(root: ILExpressionHolder): Boolean {
      // Don't convert empty interpolations for now
      if (root.expression == null) return false
      var convertible = true
      root.acceptChildren(object : ILElementVisitor() {
        override fun visitElement(element: PsiElement) {
          ProgressIndicatorProvider.checkCanceled()
          if (convertible) element.acceptChildren(this)
        }

        override fun visitILExpressionHolder(o: ILExpressionHolder) {
          convertible = false
          return
        }

        // For now Terraform 0.12 does not support string concatenation via '+' operator
        override fun visitILBinaryAdditionExpression(o: ILBinaryAdditionExpression) {
          val left = o.leftOperand
          val right = o.leftOperand
          val lType = left.getType()
          val rType = right.getType()
          if (lType == Types.String) {
            convertible = false
            return
          }
          if (rType == Types.String) {
            convertible = false
            return
          }
          super.visitILBinaryAdditionExpression(o)
        }

        override fun visitILSelectExpression(o: ILSelectExpression) {
          val field = o.field
          if (field is ILLiteralExpression) {
            if (field.text.contains("\${")) {
              convertible = false
              return
            }
          } else if (field is ILExpressionHolder) {
            convertible = false
            return
          } else if (field is ILVariable) {
            val text = field.text
            if (text.first().isDigit()) {
              // TF 0.12 does not support IDs starting with digit, our HCL2 parser does not support them too
              convertible = false
              return
            }
          }
          super.visitILSelectExpression(o)
        }
      })
      return convertible
    }

    private fun convert(root: ILExpressionHolder): String {
      val sb = StringBuilder()
      convert(root, sb)
      return sb.toString()
    }

    private fun convert(e: PsiElement, sb: StringBuilder) {
      when (e) {
        is ILIndexSelectExpression -> {
          convert(e.from, sb)
          sb.append('[')
          convert(e.field!!, sb)
          sb.append(']')
          return
        }
        is ILSelectExpression -> {
          val field = e.field
          convert(e.from, sb)
          if (field is ILLiteralExpression && field.number != null) {
            sb.append('[').append(field.text).append(']')
            return
          }
          sb.append('.')
          convert(e.field!!, sb)
          return
        }
        is ILExpressionHolder -> {
          convert(e.expression!!, sb)
          return
        }
        is LeafPsiElement -> {
          sb.append(e.text)
          return
        }
        is CompositePsiElement -> {
          for (it in e.children) {
            convert(it, sb)
          }
          return
        }
        else -> {
          sb.append(e.text)
          return
        }
      }
    }

    private fun findConvertibleInjections(o: HCLStringLiteral, consumer: BiConsumer<TextRange, ILExpressionHolder>) {
      if (o.textFragments.any { it.second.startsWith("%{") }) return
      InjectedLanguageManager.getInstance(o.project).enumerate(o) { injectedPsi, places ->
        ProgressIndicatorProvider.checkCanceled()
        if (injectedPsi.fileType == HILFileType) {
          val root = injectedPsi.firstChild
          if (root == injectedPsi.lastChild && root is ILExpressionHolder && places.size == 1) {
            if (isConvertible(root)) {
              consumer.accept(places.first().rangeInsideHost, root)
            }
          }
        }
      }
    }
  }
}