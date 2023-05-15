// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.patterns

import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.*
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.TerraformLanguage

object TerraformPatterns {
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

  val ModuleRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("module"))

  val VariableRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("variable"))

  val OutputRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("output"))

  val ResourceRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("resource"))

  val DataSourceRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("data"))

  val ProviderRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .withParent(TerraformConfigFile)
          .with(createBlockPattern("provider"))

  val ProvisionerBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .withParent(or(ResourceRootBlock))
          .with(createBlockPattern("provisioner"))

  val ResourceLifecycleBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .withParent(or(ResourceRootBlock))
          .with(createBlockPattern("lifecycle"))

  val ResourceConnectionBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .withParent(or(ResourceRootBlock, ProvisionerBlock))
          .with(createBlockPattern("connection"))

  val TerraformRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("terraform"))

  val LocalsRootBlock: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(RootBlock)
          .with(createBlockPattern("locals"))

  val Backend: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .with(createBlockPattern("backend"))
          .withSuperParent(2, TerraformRootBlock)

  val DynamicBlock: PsiElementPattern.Capture<HCLBlock>
  val DynamicBlockContent: PsiElementPattern.Capture<HCLBlock>

  init {
    val dynamicContentRef = Ref<ElementPattern<HCLBlock>>()
    DynamicBlock = PlatformPatterns.psiElement(HCLBlock::class.java)
        .with(createBlockPattern("dynamic"))
        .withSuperParent(2, HCLPatterns.Block)
        .inside(true, or(ResourceRootBlock, DataSourceRootBlock, ProviderRootBlock, ProvisionerBlock, LazyInitElementPattern(dynamicContentRef)))
    DynamicBlockContent = PlatformPatterns.psiElement(HCLBlock::class.java)
        .with(createBlockPattern("content")).withSuperParent(2, DynamicBlock)
    dynamicContentRef.set(DynamicBlockContent)
  }


  val DynamicLabels: PsiElementPattern.Capture<HCLProperty> = propertyWithName("labels").withSuperParent(2, DynamicBlock)
  val DynamicBlockIterator: PsiElementPattern.Capture<HCLProperty> = propertyWithName("iterator").withSuperParent(2, DynamicBlock)

  val ModuleWithEmptySource: PsiElementPattern.Capture<HCLBlock> =
      PlatformPatterns.psiElement(HCLBlock::class.java)
          .and(ModuleRootBlock)
          .with(object: PatternCondition<HCLBlock?>("ModuleWithEmptySource") {
            override fun accepts(t: HCLBlock, context: ProcessingContext?): Boolean {
              val source = t.`object`?.findProperty("source")?.value as? HCLStringLiteral ?: return true
              return StringUtil.isEmptyOrSpaces(source.value)
            }
          })

  val IsBlockNameIdentifier: PatternCondition<PsiElement> = object : PatternCondition<PsiElement>("IsBlockNameIdentifier") {
    override fun accepts(t: PsiElement, context: ProcessingContext?): Boolean {
      val parent = t.parent as? HCLBlock ?: return false
      return parent.nameIdentifier === t
    }
  }

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


  private fun createBlockPattern(type: String): PatternCondition<HCLBlock?> {
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
