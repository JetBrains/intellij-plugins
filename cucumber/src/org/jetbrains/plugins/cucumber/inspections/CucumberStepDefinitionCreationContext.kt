package org.jetbrains.plugins.cucumber.inspections

import com.intellij.psi.PsiFile
import org.jetbrains.plugins.cucumber.BDDFrameworkType

data class CucumberStepDefinitionCreationContext(var psiFile: PsiFile? = null, var frameworkType: BDDFrameworkType? = null){
  //Only add unique files
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as CucumberStepDefinitionCreationContext

    if (psiFile != other.psiFile) return false

    return true
  }

  override fun hashCode(): Int {
    return psiFile?.hashCode() ?: 0
  }
}