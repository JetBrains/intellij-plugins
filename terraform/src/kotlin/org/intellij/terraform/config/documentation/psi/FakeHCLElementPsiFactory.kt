// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation.psi

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import org.intellij.terraform.config.model.PropertyOrBlockType
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementGenerator
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

@Service(Service.Level.PROJECT)
internal class FakeHCLElementPsiFactory(val project: Project) {

  private val generator = HCLElementGenerator(project)

  fun createFakeHCLBlock(blockName: String, blockType: String): HCLBlock = generator.createBlock(blockName, emptyMap(), blockType)

  fun createFakeHCLProperty(block: HCLBlock, property: PropertyOrBlockType): HCLProperty? {
    val dummyBlock = generator.createBlock(block.getNameElementUnquoted(0)?:"", mapOf(property.name to "\"\""), block.getNameElementUnquoted(1) ?: "\"\"", block.getNameElementUnquoted(2) ?: "\"\"")
    val hclProperty = generator.createProperty(property.name, "\"\"")
    dummyBlock.`object`?.propertyList?.add(hclProperty)
    return dummyBlock.`object`?.findProperty(property.name)
  }

}
