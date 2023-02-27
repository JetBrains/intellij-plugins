// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

open class Block(val type: BlockType, vararg val properties: PropertyOrBlock = emptyArray()) {
  fun toPOB(): PropertyOrBlock {
    return PropertyOrBlock(block = this)
  }
}
