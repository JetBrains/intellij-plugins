// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation.psi

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.config.psi.TerraformElementGenerator
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

@Service(Service.Level.PROJECT)
class FakeHCLElementPsiFactory(val project: Project) {

  val emptyHCLBlock: HCLBlock? by lazy {
    createFakeHCLBlock("", "")
  }

  private val terraformElementGenerator = TerraformElementGenerator(project)

  fun createFakeHCLBlock(blockName: String, blockType: String, original: PsiFile? = null): HCLBlock? {
    val hclBlock = try {
      terraformElementGenerator.createBlock(blockName, emptyMap(), blockType, original = original)
    }
    catch (e: IllegalStateException) {
      fileLogger().warnWithDebug("Failed to create HCLBlock for content: ${blockName} ${blockType}. ${e.message}", e)
      null
    }
    return hclBlock
  }

  fun createFakeHCLProperty(block: HCLBlock, property: PropertyOrBlockType): HCLProperty? {
    val dummyBlock = try {
      terraformElementGenerator.createBlock(block.getNameElementUnquoted(0) ?: "",
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
