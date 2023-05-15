// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.inspection

import com.intellij.BundleBase
import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.codeInspection.*
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceOwner
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.HCLPsiUtil
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.hil.HILLanguage
import org.intellij.terraform.hil.codeinsight.isResourceInstanceReference
import org.intellij.terraform.hil.codeinsight.isResourcePropertyReference
import org.intellij.terraform.hil.codeinsight.isScopeElementReference
import org.intellij.terraform.hil.psi.impl.getHCLHost
import org.intellij.terraform.hil.psi.resolve
import org.jetbrains.annotations.Nls

class HILUnresolvedReferenceInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    if (holder.file.language !in listOf(HCLLanguage, TerraformLanguage, HILLanguage)) return PsiElementVisitor.EMPTY_VISITOR
    val file = InjectedLanguageManager.getInstance(holder.project).getTopLevelFile(holder.file)
    val ft = file.fileType
    if (ft != TerraformFileType) {
      return PsiElementVisitor.EMPTY_VISITOR
    }

    return MyEV(holder)
  }

  companion object {
    private val LOG = Logger.getInstance(HILUnresolvedReferenceInspection::class.java)

  }

  inner class MyEV(val holder: ProblemsHolder) : PsiElementVisitor() {
    override fun visitElement(element: PsiElement) {
      if (element is Identifier) return visitIdentifier(element)
      ProgressIndicatorProvider.checkCanceled()
    }

    private fun visitIdentifier(element: Identifier) {
      ProgressIndicatorProvider.checkCanceled()
      element.getHCLHost() ?: return

      val parent = element.parent as? SelectExpression<*> ?: return
      if (parent.from === element) {
        checkReferences(element)
      } else if (isScopeElementReference(element, parent)) {
        // TODO: Check scope parameter reference
        checkReferences(element)
      } else if (isResourceInstanceReference(element, parent)) {
        // TODO: Check and report "no such resource of type" error
        checkReferences(element)
      } else if (isResourcePropertyReference(element, parent)) {
        // TODO: Check and report "no such resource property" error (only if there such resource)
        checkReferences(element)
      }
    }

    private fun checkReferences(value: PsiElement) {
      doCheckRefs(value, HCLPsiUtil.getReferencesSelectAware(value))
    }

    private fun doCheckRefs(value: PsiElement, references: Array<out PsiReference>) {
      for (reference in references) {
        ProgressManager.checkCanceled()
        // In case of 'a.*.b' '*' bypasses references from 'a'
        if (reference.element !== value) continue
        if (isUrlReference(reference)) continue
        if (!hasBadResolve(reference, false)) {
          continue
        }
        val description = getErrorDescription(reference)

        //        val startOffset = reference.element.textRange.startOffset
        val referenceRange = reference.rangeInElement

        // logging for IDEADEV-29655
        if (referenceRange.startOffset > referenceRange.endOffset) {
          LOG.error("Reference range start offset > end offset:  " + reference +
              ", start offset: " + referenceRange.startOffset + ", end offset: " + referenceRange.endOffset)
        }

        var fixes: Array<out LocalQuickFix> = emptyArray()
        if (reference is LocalQuickFixProvider) {
          reference.quickFixes?.let { fixes = it }
        }
        holder.registerProblem(value, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, referenceRange/*.shiftRight(startOffset)*/, *fixes)
      }
    }
  }


  fun isUrlReference(reference: PsiReference): Boolean {
    return reference is FileReferenceOwner// || reference is com.intellij.xml.util.AnchorReference
  }

  @Nls
  fun getErrorDescription(reference: PsiReference): String {
    val messagePattern: String
    if (reference is EmptyResolveMessageProvider) {
      messagePattern = reference.unresolvedMessagePattern
    } else {
      // although the message has a parameter, it must be taken uninterpolated as it will be applied later
      @Suppress("InvalidBundleOrProperty")
      messagePattern = HCLBundle.message("hil.unresolved.reference.inspection.unresolved.reference.error.message")
    }

    @Nls var description: String
    try {
      description = BundleBase.format(messagePattern, reference.canonicalText) // avoid double formatting
    } catch (ex: IllegalArgumentException) {
      // unresolvedMessage provided by third-party reference contains wrong format string (e.g. {}), tolerate it
      description = messagePattern
    }

    return description
  }

  fun hasBadResolve(reference: PsiReference, checkSoft: Boolean): Boolean {
    if (!checkSoft && reference.isSoft) return false
    return resolve(reference, false, true).isEmpty()
  }
}
