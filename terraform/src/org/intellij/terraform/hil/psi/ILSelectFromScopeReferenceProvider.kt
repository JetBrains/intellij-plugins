// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants.HCL_SELF_IDENTIFIER
import org.intellij.terraform.config.codeinsight.TfModelHelper
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.hcl.psi.getHclBlockForSelfContext
import org.intellij.terraform.hil.codeinsight.AddVariableFix
import org.intellij.terraform.hil.psi.impl.getHCLHost

object ILSelectFromScopeReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    return getReferencesByElement(element)
  }

  private fun getReferencesByElement(element: PsiElement): Array<out PsiReference> {
    if (element !is Identifier) return PsiReference.EMPTY_ARRAY
    val host = element.getHCLHost() ?: return PsiReference.EMPTY_ARRAY

    val parent = element.parent as? SelectExpression<*> ?: return PsiReference.EMPTY_ARRAY
    val from = parent.from as? Identifier ?: return PsiReference.EMPTY_ARRAY

    if (from === element) return PsiReference.EMPTY_ARRAY
    return when (from.name) {
      "var" -> arrayOf(VariableReference(element))
      HCL_SELF_IDENTIFIER -> arrayOf(SelfResourceReference(element))
      "path" -> {
        // TODO: Resolve 'cwd' and 'root' paths
        if (element.name == "module") {
          val file = host.containingFile.originalFile
          return arrayOf(PsiReferenceBase.Immediate(element, true, file.containingDirectory ?: file))
        }
        else PsiReference.EMPTY_ARRAY
      }
      "module" -> arrayOf(ModuleReference(element))
      "local" -> arrayOf(LocalVariableReference(element))
      else -> return PsiReference.EMPTY_ARRAY
    }
  }

  class VariableReference(element: Identifier) : HCLElementLazyReference<Identifier>(element, false, doResolve = { _, _ ->
    this.element.name?.let { name ->
      this.element.getHCLHost()?.getTerraformModule()?.findVariables(name)?.map { it.declaration }
    } ?: emptyList()
  }), LocalQuickFixProvider {
    override fun getQuickFixes(): Array<LocalQuickFix> = arrayOf(AddVariableFix(this.element))
    override fun toString(): String {
      return "Variable Ref: " + super.toString()
    }
  }

  class SelfResourceReference(element: Identifier) : HCLElementLazyReferenceBase<Identifier>(element, false) {
    override fun resolve(incompleteCode: Boolean, includeFake: Boolean): List<HCLElement> {
      val name = this.element.name ?: return emptyList()
      val resource = getHclBlockForSelfContext(this.element) ?: return emptyList()

      val prop = resource.`object`?.findProperty(name)
      if (prop != null) return listOf(prop)
      val blocks = resource.`object`?.blockList?.filter { it.name == name }
      if (!blocks.isNullOrEmpty()) return blocks.map { it as HCLElement }

      if (includeFake) {
        val properties = TfModelHelper.getResourceProperties(resource)
        if (properties.containsKey(name)) {
          return listOf(FakeHCLProperty(name, resource))
        }
      }
      return emptyList()
    }
  }

  class ModuleReference(element: Identifier) : HCLElementLazyReference<Identifier>(element, false, doResolve = { _, _ ->
    this.element.name?.let { name -> this.element.getHCLHost()?.getTerraformModule()?.getDefinedModules(name) }
    ?: emptyList()
  })

  class LocalVariableReference(element: Identifier) : HCLElementLazyReference<Identifier>(element, false, doResolve = { _, _ ->
    listOfNotNull(this.element.name?.let { name ->
      this.element.getHCLHost()?.getTerraformModule()?.findLocal(name)?.second
    })
  })
}
