package org.jetbrains.plugins.cucumber.inspections

import com.intellij.psi.PsiFile
import org.jetbrains.plugins.cucumber.BDDFrameworkType

data class CucumberStepDefinitionCreationContext(var psiFile: PsiFile? = null, var frameworkType: BDDFrameworkType? = null)