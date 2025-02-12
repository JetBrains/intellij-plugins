// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.hints

import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering
import com.intellij.codeInsight.hints.codeVision.ReferencesCodeVisionProvider
import com.intellij.openapi.options.advanced.AdvancedSettings.Companion.getInt
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.PsiSearchHelper.SearchCostResult
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import org.intellij.terraform.config.model.restrictToTerraformFiles
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.getElementName
import org.intellij.terraform.isTerraformCompatiblePsiFile
import java.util.concurrent.atomic.AtomicInteger

internal class TfReferencesCodeVisionProvider : ReferencesCodeVisionProvider() {
  override fun acceptsFile(file: PsiFile): Boolean = isTerraformCompatiblePsiFile(file)

  override fun acceptsElement(element: PsiElement): Boolean = when (element) {
    is HCLBlock ->
      TfPsiPatterns.VariableRootBlock.accepts(element) ||
      TfPsiPatterns.DataSourceRootBlock.accepts(element) ||
      TfPsiPatterns.ResourceRootBlock.accepts(element)
    is HCLProperty -> TfPsiPatterns.LocalProperty.accepts(element)
    else -> false
  }

  override fun getHint(element: PsiElement, file: PsiFile): String? {
    if (element !is HCLElement)
      return null

    val elementName = element.getElementName() ?: return null
    val scope = GlobalSearchScope.projectScope(element.project).restrictToTerraformFiles(element.project)
    val costSearch = PsiSearchHelper.getInstance(element.project).isCheapEnoughToSearch(elementName, scope, file)
    if (costSearch == SearchCostResult.TOO_MANY_OCCURRENCES)
      return HCLBundle.message("terraform.inlay.hints.indefinite.usages.text")

    val usagesCount = AtomicInteger()
    val limit = getInt("org.intellij.terraform.code.vision.usages.limit")

    ReferencesSearch.search(ReferencesSearch.SearchParameters(element, scope, false))
      .allowParallelProcessing()
      .forEach(Processor { it == null || usagesCount.incrementAndGet() <= limit })

    val result = usagesCount.get()
    return CodeVisionInfo(
      HCLBundle.message("terraform.inlay.hints.usages.text", result.coerceAtMost(limit), if (result > limit) 1 else 0),
      result,
      result <= limit
    ).text
  }

  override val relativeOrderings: List<CodeVisionRelativeOrdering>
    get() = emptyList()
  override val id: String
    get() = ID

  companion object {
    const val ID: String = "tf.references"
  }
}