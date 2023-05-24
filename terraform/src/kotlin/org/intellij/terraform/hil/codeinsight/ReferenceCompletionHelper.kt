// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.codeinsight

import org.intellij.terraform.hcl.navigation.HCLQualifiedNameProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.config.model.Module

object ReferenceCompletionHelper {
  /**
   * @return null or List<HCLProperty or HCLBlock>
   */
  fun findByFQNRef(fqn: String, module: Module): List<Any>? {
    val parts = fqn.split('.')
    if (parts.size < 2) return null
    val root = parts[0]
    if (parts[1].startsWith('#')) {
      // Specials
      // TODO: Support
      return null
    }
    val second: List<HCLBlock> = when (root) {
      "resource" -> {
        return module.findResources(parts[1], null).map { "${parts[1]}.${it.name}.${parts.subList(2, parts.size).joinToString(".")}" }
      }
      "data" -> {
        return module.findDataSource(parts[1], null).map { "data.${parts[1]}.${it.name}.${parts.subList(2, parts.size).joinToString(".")}" }
      }
      "provider" -> {
        // TODO: Check it would found any providers given type with other aliases
        module.findProviders(parts[1])
      }
      else -> null
    } ?: return null
    if (parts.size == 2) return second
    return second.mapNotNull { find(it, parts.subList(2, parts.size), fqn) }
  }

  /**
   * @return either HCLProperty or HCLBlock or null
   */
  private fun find(block: HCLBlock, parts: List<String>, fqn: String): Any? {
    if (parts.isEmpty()) return null
    val obj = block.`object` ?: return null
    val property = obj.findProperty(parts.first())
    if (property != null) {
      return if (parts.size == 1) property else null
    }
    // TODO: Support many blocks with same name
    val blk = obj.blockList.find { it -> HCLQualifiedNameProvider.getQualifiedModelName(it)?.let { it == fqn || fqn.startsWith("$it.") } ?: false }
    if (blk != null) {
      return if (parts.size == 1) blk else find(blk, parts.subList(1, parts.size), fqn)
    }
    return null
  }
}