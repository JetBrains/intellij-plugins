// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation.psi

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.NamedType
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.config.model.ProviderType
import org.intellij.terraform.config.model.getProviderForBlockType
import org.intellij.terraform.config.psi.TfElementGenerator
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

internal val FAKE_PROVIDER_KEY = Key.create<ProviderType>("org.intellij.terraform.config.documentation.psi.provider")

@Service(Service.Level.PROJECT)
internal class FakeHCLElementPsiFactory(val project: Project) {

  val emptyHCLBlock: HCLBlock? by lazy {
    createFakeHCLBlock("", "")
  }

  private val myTfElementGenerator = TfElementGenerator(project)

  fun createFakeHCLBlock(block: NamedType, original: PsiFile? = null): HCLBlock? {
    val provider = getProviderForBlockType(block as BlockType) ?: return null
    val fakeBlock = createFakeHCLBlock(block.literal, block.type, original) ?: return null
    fakeBlock.putUserData(FAKE_PROVIDER_KEY, provider)
    return fakeBlock
  }

  fun createFakeHCLBlock(blockName: String, blockType: String, original: PsiFile? = null): HCLBlock? {
    val hclBlock = try {
      myTfElementGenerator.createBlock(blockName, emptyMap(), blockType, original = original)
    }
    catch (e: IllegalStateException) {
      fileLogger().warnWithDebug("Failed to create HCLBlock for content: ${blockName} ${blockType}. ${e.message}", e)
      null
    }
    return hclBlock
  }

  fun createFakeHCLProperty(block: HCLBlock, property: PropertyOrBlockType): HCLProperty? {
    val dummyBlock = try {
      myTfElementGenerator.createBlock(block.getNameElementUnquoted(0) ?: "",
                                       mapOf(property.name to "\"\""),
                                       namedElements = arrayOf(block.getNameElementUnquoted(1) ?: "\"\"",
                                                                    block.getNameElementUnquoted(2) ?: "\"\""),
                                       original = block.containingFile.originalFile)
    }
    catch (e: IllegalStateException) {
      fileLogger().warnWithDebug("Failed to create HCLProperty: ${property} for block ${block}. ${e.message}", e)
      null
    }
    val hclProperty = dummyBlock?.`object`?.findProperty(property.name)
    return hclProperty
  }

}
