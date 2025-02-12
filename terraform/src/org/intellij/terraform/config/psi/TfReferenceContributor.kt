// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.psi.tree.TokenSet
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.model.Module
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.patterns.TfPsiPatterns.DependsOnPattern
import org.intellij.terraform.config.patterns.TfPsiPatterns.ResourceProviderProperty
import org.intellij.terraform.config.patterns.TfPsiPatterns.TerraformConfigFile
import org.intellij.terraform.config.patterns.TfPsiPatterns.TerraformVariablesFile
import org.intellij.terraform.config.patterns.TfPsiPatterns.propertyWithName
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.LiteralExpression
import org.intellij.terraform.hil.psi.HCLElementLazyReference
import org.intellij.terraform.hil.psi.HCLElementLazyReferenceBase

class TfReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(
      psiElement()
        .and(PlatformPatterns.or(HCLPatterns.Identifier, HCLPatterns.Literal))
        .inFile(TerraformConfigFile)
        .withParent(ResourceProviderProperty), ResourceProviderReferenceProvider)

    registrar.registerReferenceProvider(
      psiElement()
        .and(PlatformPatterns.or(HCLPatterns.Identifier, HCLPatterns.Literal))
        .inFile(TerraformConfigFile)
        .withParent(HCLPatterns.SelectExpression)
        .withSuperParent(2, ResourceProviderProperty), ResourceProviderReferenceProvider)

    // 'depends_on' in resources, data sources, modules and outputs
    registrar.registerReferenceProvider(
        psiElement(HCLStringLiteral::class.java)
            .inFile(TerraformConfigFile)
            .withSuperParent(1, psiElement(HCLArray::class.java))
          .withSuperParent(2, DependsOnPattern)
        , DependsOnReferenceProvider)

    // Resolve variables usage in .tfvars
    registrar.registerReferenceProvider(
        psiElement(HCLIdentifier::class.java)
            .inFile(TerraformVariablesFile)
            .withParent(psiElement(HCLProperty::class.java))
        , VariableReferenceProvider)
    registrar.registerReferenceProvider(
        psiElement().withElementType(TokenSet.create(HCLElementTypes.IDENTIFIER, HCLElementTypes.STRING_LITERAL))
            .inFile(TerraformVariablesFile)
            .withSuperParent(1, HCLProperty::class.java)
            .withSuperParent(2, HCLObject::class.java)
            .withSuperParent(3, HCLProperty::class.java)
        , MapVariableIndexReferenceProvider)

    // 'module' source
    registrar.registerReferenceProvider(
        psiElement(HCLStringLiteral::class.java)
            .inFile(TerraformConfigFile)
            .withParent(propertyWithName("source"))
            .withSuperParent(3, TfPsiPatterns.ModuleRootBlock)
        , ModuleSourceReferenceProvider)

    // 'module' variable setter
    registrar.registerReferenceProvider(
        psiElement(HCLIdentifier::class.java)
            .inFile(TerraformConfigFile)
            .withParent(psiElement(HCLProperty::class.java).with(object : PatternCondition<HCLProperty?>("HCLProperty(!source)") {
              override fun accepts(t: HCLProperty, context: ProcessingContext?): Boolean {
                return "source" != t.name
              }
            }))
            .withSuperParent(3, TfPsiPatterns.ModuleRootBlock)
        , ModuleVariableReferenceProvider)

    // 'module' providers key/value
    registrar.registerReferenceProvider(
        psiElement().and(PlatformPatterns.or(HCLPatterns.Identifier, HCLPatterns.Literal, HCLPatterns.SelectExpression))
            .inFile(TerraformConfigFile)
            .withParent(TfPsiPatterns.PropertyUnderModuleProvidersPOB)
        , ModuleProvidersReferenceProvider)

    //Documentation reference
    registrar.registerReferenceProvider(
      psiElement(HCLStringLiteral::class.java)
        .and(HCLPatterns.BlockTypeIdentifierLiteral)
        .inFile(TerraformConfigFile)
      , WebDocumentationReferenceProvider)
  }
}

internal object WebDocumentationReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement,
                                      context: ProcessingContext): Array<PsiReference> {
    val webReference = TfDocReference(element)
    return arrayOf(webReference)
  }
}

object ResourceProviderReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (element !is HCLStringLiteral && element !is HCLIdentifier) {
      return PsiReference.EMPTY_ARRAY
    }
    if (element !is HCLExpression) return PsiReference.EMPTY_ARRAY
    if (!HCLPsiUtil.isPropertyValue(element)) return PsiReference.EMPTY_ARRAY
    return arrayOf(HCLElementLazyReference(element, false) { incomplete, _ ->
      @Suppress("NAME_SHADOWING")
      val element = this.element
      val module = (element as HCLElement).getTerraformModule()
      if (incomplete) {
        module.getDefinedProviders().map { it.first }
      }
      else {
        getElementText(element)?.let { text -> module.findProviders(text) } ?: emptyList()
      }
    })
  }

  private fun getElementText(expression: BaseExpression): String? {
    if (expression !is LiteralExpression && expression !is Identifier) return null
    val parent = expression.parent
    if (parent is HCLSelectExpression && parent !is HCLIndexSelectExpression) {
      return parent.text
    }
    return when (expression) {
      is LiteralExpression -> expression.unquotedText
      is Identifier -> expression.name
      else -> null
    }
  }
}

object ModuleSourceReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (element !is HCLStringLiteral) return PsiReference.EMPTY_ARRAY
    if (!HCLPsiUtil.isPropertyValue(element)) return PsiReference.EMPTY_ARRAY
    return FileReferenceSet.createSet(element, true, false, false).allReferences
  }
}

// TODO: Fix renaming: add range to reference
object DependsOnReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (element !is HCLStringLiteral) return PsiReference.EMPTY_ARRAY
    if (element.parent !is HCLArray) return PsiReference.EMPTY_ARRAY
    if (!HCLPsiUtil.isPropertyValue(element.parent)) return PsiReference.EMPTY_ARRAY
    return arrayOf(DependsOnLazyReference(element))
  }
}

class DependsOnLazyReference(element: HCLStringLiteral) : HCLElementLazyReferenceBase<HCLStringLiteral>(element, false) {
  override fun resolve(incompleteCode: Boolean, includeFake: Boolean): List<HCLElement> {
    val block = element.parent?.parent?.parent?.parent as? HCLBlock ?: return emptyList()

    val module = element.getTerraformModule()
    if (incompleteCode) {
      val resources = module.getDeclaredResources()
        .associateBy { "${it.getNameElementUnquoted(1)}.${it.name}" }.toMutableMap()
      val datas = module.getDeclaredDataSources()
        .associateBy { "data.${it.getNameElementUnquoted(1)}.${it.name}" }.toMutableMap()
      val modules = module.getDefinedModules()
        .associateBy { "module.${it.name}" }.toMutableMap()
      val variables = module.getAllVariables().map { it.declaration }
        .associateBy { "var.${it.name}" }.toMutableMap()

      when (block.getNameElementUnquoted(0)) {
        "data" -> datas.remove("data.${block.getNameElementUnquoted(1)}.${block.name}")
        "resource" -> resources.remove("${block.getNameElementUnquoted(1)}.${block.name}")
        "module" -> modules.remove("module.${block.name}")
        "variable" -> variables.remove("var.${block.name}")
      }

      return resources.values.asSequence().plus(datas.values).plus(modules.values).plus(variables.values).toList()
    } else {
      val split = element.value.split('.')
      if (split.size == 2) {
        return when (split[0]) {
          "var" -> module.findVariables(split[1]).map { it.declaration }
          "module" -> module.findModules(split[1])
          else -> module.findResources(split[0], split[1])
        }
      } else if (split.size == 3) {
        when (split[0]) {
          "data" -> return module.findDataSource(split[1], split[2])
          "module" -> return module.findModules(split[1]).mapNotNull { Module.getAsModuleBlock(it) }.flatMap { it.getDefinedOutputs() }.filter { it.name == split[2] }
        }
      }
    }
    return emptyList()
  }

  private fun getRangeInElementForRename(): TextRange {
    if (element.parent?.parent?.parent?.parent !is HCLBlock) return rangeInElement
    val value = element.value
    val split = value.split('.')
    if ((split.size == 3 && split[0] == "data") || split.size == 2) {
      val ll = split.last().length
      val tl = element.textLength
      return TextRange.create(tl - 1 - ll, tl - 1)
    }
    return rangeInElement
  }

  override fun handleElementRename(newElementName: String): PsiElement? {
    return ElementManipulators.handleContentChange(element, getRangeInElementForRename(), newElementName)
  }
}

object VariableReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (element !is HCLIdentifier) return PsiReference.EMPTY_ARRAY
    if (!HCLPsiUtil.isPropertyKey(element)) return PsiReference.EMPTY_ARRAY

    val varReference = HCLElementLazyReference(element, false) { incomplete, _ ->
      @Suppress("NAME_SHADOWING")
      val element = this.element
      if (incomplete) {
        element.getTerraformModule().getAllVariables().map { it.declaration }
      } else {
        val value = element.id
        element.getTerraformModule().findVariables(value.substringBefore('.')).map { it.declaration }
      }
    }

    val value = element.id
    val dotIndex = value.indexOf('.')
    if (dotIndex != -1) {
      // Mapped variable
      // Two references: variable name (hard) and variable subvalue (soft)
      val subReference = HCLElementLazyReference(element, true) { incomplete, _ ->
        @Suppress("NAME_SHADOWING")
        val element = this.element
        @Suppress("NAME_SHADOWING")
        val value = element.id
        val variables = element.getTerraformModule().findVariables(value.substringBefore('.'))
        val defaults = variables.mapNotNull { it.getDefault() as? HCLObject }
        if (incomplete) {
          defaults.flatMap { it.propertyList }
        } else {
          defaults.mapNotNull { it.findProperty(value.substringAfter('.')) }
        }
      }
      varReference.rangeInElement = TextRange(0, dotIndex)
      subReference.rangeInElement = TextRange(dotIndex + 1, value.length)
      return arrayOf(varReference, subReference)
    }

    return arrayOf(varReference)
  }

}

object MapVariableIndexReferenceProvider : PsiReferenceProvider() {
  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
    if (element !is HCLElement) return PsiReference.EMPTY_ARRAY
    if (element !is HCLIdentifier && element !is HCLStringLiteral) return PsiReference.EMPTY_ARRAY

    if (!HCLPsiUtil.isPropertyKey(element)) return PsiReference.EMPTY_ARRAY

    val pObj = element.parent.parent as? HCLObject ?: return PsiReference.EMPTY_ARRAY

    if (pObj.parent !is HCLProperty) return PsiReference.EMPTY_ARRAY

    val subReference = HCLElementLazyReference(element, true) { incomplete, _ ->
      @Suppress("NAME_SHADOWING")
      val element = this.element
      if (element.parent?.parent?.parent !is HCLProperty) {
        return@HCLElementLazyReference emptyList()
      }
      val value = (element.parent?.parent?.parent as HCLProperty).name
      val variables = element.getTerraformModule().findVariables(value)
      val defaults = variables.mapNotNull { it.getDefault() as? HCLObject }
      if (incomplete) {
        defaults.flatMap { it.propertyList }
      } else {
        val name = element.name ?: return@HCLElementLazyReference emptyList()
        defaults.mapNotNull { it.findProperty(name) }
      }
    }
    return arrayOf(subReference)
  }
}
