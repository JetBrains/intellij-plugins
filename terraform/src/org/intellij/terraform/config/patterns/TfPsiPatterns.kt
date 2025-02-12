// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.patterns

import com.intellij.openapi.util.Ref
import com.intellij.patterns.*
import com.intellij.patterns.StandardPatterns.or
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.Constants.HCL_BACKEND_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_CONNECTION_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DATASOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DYNAMIC_BLOCK_CONTENT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_DYNAMIC_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_LIFECYCLE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_LOCALS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_MOVED_BLOCK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_OUTPUT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVISIONER_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_RESOURCE_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_REQUIRED_PROVIDERS
import org.intellij.terraform.config.Constants.HCL_VARIABLE_IDENTIFIER
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.*

object TfPsiPatterns {
  val TerraformFile: PsiFilePattern.Capture<HCLFile> =
    PlatformPatterns.psiFile(HCLFile::class.java)
      .withLanguage(TerraformLanguage)
  val TerraformVariablesFile: PsiFilePattern.Capture<HCLFile> =
    PlatformPatterns.psiFile(HCLFile::class.java)
      .withLanguage(TerraformLanguage)
      .inVirtualFile(PlatformPatterns.virtualFile().withExtension(TerraformFileType.TFVARS_EXTENSION))
  val TerraformConfigFile: PsiFilePattern.Capture<HCLFile> =
    PlatformPatterns.psiFile(HCLFile::class.java)
      .withLanguage(TerraformLanguage)
      .andNot(TerraformVariablesFile)
  val ConfigOverrideFile: PsiFilePattern.Capture<HCLFile> =
    PlatformPatterns.psiFile(HCLFile::class.java)
      .and(TerraformConfigFile)
      .inVirtualFile(
        PlatformPatterns.virtualFile().withName(StandardPatterns.string().with(object : PatternCondition<String?>("Terraform override file name") {
          override fun accepts(t: String, context: ProcessingContext?): Boolean {
            val suffix = "override." + TerraformFileType.DEFAULT_EXTENSION
            if (!t.endsWith(suffix)) return false
            // previous line enforces t.length >= suffix.length
            return t.length == suffix.length || t[t.length - suffix.length - 1] == '_'
          }
        })))

  val RootBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .withParent(TerraformConfigFile)

  val RootBlockForHCLFiles: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .withParent(HCLFile::class.java)

  val ModuleRootBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .and(RootBlock)
      .with(createBlockPattern(HCL_MODULE_IDENTIFIER))

  val VariableRootBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .and(RootBlock)
      .with(createBlockPattern(HCL_VARIABLE_IDENTIFIER))

  val OutputRootBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .and(RootBlock)
      .with(createBlockPattern(HCL_OUTPUT_IDENTIFIER))

  val ResourceRootBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .and(RootBlock)
      .with(createBlockPattern(HCL_RESOURCE_IDENTIFIER))

  val DataSourceRootBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .and(RootBlock)
      .with(createBlockPattern(HCL_DATASOURCE_IDENTIFIER))

  val ProviderRootBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .withParent(TerraformConfigFile)
      .with(createBlockPattern(HCL_PROVIDER_IDENTIFIER))

  val ProvisionerBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .withParent(or(ResourceRootBlock))
      .with(createBlockPattern(HCL_PROVISIONER_IDENTIFIER))

  val ResourceLifecycleBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .withParent(or(ResourceRootBlock))
      .with(createBlockPattern(HCL_LIFECYCLE_IDENTIFIER))

  val ResourceConnectionBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .withParent(or(ResourceRootBlock, ProvisionerBlock))
      .with(createBlockPattern(HCL_CONNECTION_IDENTIFIER))

  val TerraformRootBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .and(RootBlock)
      .with(createBlockPattern(HCL_TERRAFORM_IDENTIFIER))

  val RequiredProvidersBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(createBlockPattern(HCL_TERRAFORM_REQUIRED_PROVIDERS))
      .withSuperParent(2, TerraformRootBlock)

  val RequiredProvidersData: PsiElementPattern.Capture<HCLProperty> =
    PlatformPatterns.psiElement(HCLProperty::class.java)
      .withSuperParent(2, RequiredProvidersBlock)

  val RequiredProvidersSource: PsiElementPattern.Capture<HCLProperty> =
    PlatformPatterns.psiElement(HCLProperty::class.java)
      .withSuperParent(2, RequiredProvidersData)
      .with(object : PatternCondition<HCLProperty?>("HCLProperty(source)") {
        override fun accepts(t: HCLProperty, context: ProcessingContext?): Boolean {
          return t.name == "source"
        }
      })


  val LocalsRootBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .and(RootBlock)
      .with(createBlockPattern(HCL_LOCALS_IDENTIFIER))

  val Backend: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(createBlockPattern(HCL_BACKEND_IDENTIFIER))
      .withSuperParent(2, TerraformRootBlock)

  private val MovedBlock: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
    .and(RootBlock)
    .with(createBlockPattern(HCL_MOVED_BLOCK_IDENTIFIER))

  val DynamicBlock: PsiElementPattern.Capture<HCLBlock>
  val DynamicBlockContent: PsiElementPattern.Capture<HCLBlock>

  init {
    val dynamicContentRef = Ref<ElementPattern<HCLBlock>>()
    DynamicBlock = PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(createBlockPattern(HCL_DYNAMIC_BLOCK_IDENTIFIER))
      .withSuperParent(2, HCLPatterns.Block)
      .inside(true, or(ResourceRootBlock, DataSourceRootBlock, ProviderRootBlock, ProvisionerBlock, LazyInitElementPattern(dynamicContentRef)))
    DynamicBlockContent = PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(createBlockPattern(HCL_DYNAMIC_BLOCK_CONTENT_IDENTIFIER)).withSuperParent(2, DynamicBlock)
    dynamicContentRef.set(DynamicBlockContent)
  }


  val DynamicLabels: PsiElementPattern.Capture<HCLProperty> = propertyWithName("labels").withSuperParent(2, DynamicBlock)
  val DynamicBlockIterator: PsiElementPattern.Capture<HCLProperty> = propertyWithName("iterator").withSuperParent(2, DynamicBlock)

  val ModuleWithEmptySource: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .and(ModuleRootBlock)
      .with(object : PatternCondition<HCLBlock?>("ModuleWithEmptySource") {
        override fun accepts(t: HCLBlock, context: ProcessingContext?): Boolean {
          val source = t.`object`?.findProperty("source") ?: return true
          return source.value?.text?.isEmpty() == true
        }
      })

  val PropertyUnderModuleProvidersPOB: PsiElementPattern.Capture<HCLProperty> = PlatformPatterns.psiElement(HCLProperty::class.java)
    .withSuperParent(1, HCLPatterns.Object)
    .withSuperParent(2, PlatformPatterns.psiElement().and(HCLPatterns.PropertyOrBlock).andOr(propertyWithName("providers"), PlatformPatterns.psiElement(HCLBlock::class.java).with(object : PatternCondition<HCLBlock?>("HCLBlock(providers)") {
      override fun accepts(t: HCLBlock, context: ProcessingContext?): Boolean {
        return t.nameElements.size == 1 && t.name == "providers"
      }
    }))).withSuperParent(3, HCLPatterns.Object)
    .withSuperParent(4, ModuleRootBlock)

  val ForVariable: PsiElementPattern.Capture<HCLIdentifier> = PlatformPatterns.psiElement(HCLIdentifier::class.java)
    .withParent(HCLForIntro::class.java)
    .with(object : PatternCondition<HCLIdentifier?>("ForVariable") {
      override fun accepts(t: HCLIdentifier, context: ProcessingContext?): Boolean {
        val intro = t.parent as? HCLForIntro ?: return false
        return t === intro.var1 || t === intro.var2
      }
    })

  val ResourceProviderProperty: PsiElementPattern.Capture<HCLProperty> = propertyWithName("provider")
    .withParent(HCLObject::class.java)
    .withSuperParent(2, or(ResourceRootBlock, DataSourceRootBlock))

  val StringLiteralAnywhereInVariable: PsiElementPattern.Capture<HCLStringLiteral> =
    PlatformPatterns.psiElement(HCLStringLiteral::class.java)
      .inside(true, VariableRootBlock)
  val HeredocContentAnywhereInVariable: PsiElementPattern.Capture<HCLHeredocContent> =
    PlatformPatterns.psiElement(HCLHeredocContent::class.java)
      .inside(true, VariableRootBlock)

  val DependsOnPattern: PsiElementPattern.Capture<HCLProperty> =
    PlatformPatterns.psiElement(HCLProperty::class.java)
      .withSuperParent(1, HCLObject::class.java)
      .withSuperParent(2, or(ResourceRootBlock, DataSourceRootBlock, ModuleRootBlock, OutputRootBlock))
      .with(object : PatternCondition<HCLProperty?>("HCLProperty(depends_on)") {
        override fun accepts(t: HCLProperty, context: ProcessingContext?): Boolean {
          return t.name == "depends_on"
        }
      })

  val FromPropertyInMovedBlock: PsiElementPattern.Capture<HCLProperty> = PlatformPatterns.psiElement(HCLProperty::class.java)
    .withSuperParent(1, HCLObject::class.java)
    .withSuperParent(2, MovedBlock)
    .with(object : PatternCondition<HCLProperty?>("HCLProperty(from)") {
      override fun accepts(t: HCLProperty, context: ProcessingContext?): Boolean {
        return t.name == "from"
      }
    })

  val LocalProperty: PsiElementPattern.Capture<HCLProperty> = PlatformPatterns.psiElement(HCLProperty::class.java)
    .withSuperParent(1, HCLObject::class.java)
    .withSuperParent(2, LocalsRootBlock)

  val DescriptionProperty: PsiElementPattern.Capture<HCLProperty> = propertyWithName("description")

  fun createBlockPattern(type: String): PatternCondition<HCLBlock?> {
    return object : PatternCondition<HCLBlock?>("HCLBlock($type)") {
      override fun accepts(t: HCLBlock, context: ProcessingContext?): Boolean {
        return t.getNameElementUnquoted(0) == type
      }
    }
  }

  fun propertyWithName(name: String): PsiElementPattern.Capture<HCLProperty> {
    return PlatformPatterns.psiElement(HCLProperty::class.java).with(object : PatternCondition<HCLProperty>("HCLProperty($name)") {
      override fun accepts(t: HCLProperty, context: ProcessingContext?): Boolean {
        return name == t.name
      }
    })
  }

  private class LazyInitElementPattern<T>(private val delegate: Ref<ElementPattern<T>>) : ElementPattern<T> {
    override fun accepts(o: Any?): Boolean {
      return delegate.get().accepts(o)
    }

    override fun accepts(o: Any?, context: ProcessingContext?): Boolean {
      return delegate.get().accepts(o, context)
    }

    override fun getCondition(): ElementPatternCondition<T> {
      return delegate.get().condition
    }

  }
}
