// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.deployment

import org.intellij.terraform.config.model.BlockType
import org.intellij.terraform.config.model.TfTypeModel.Companion.Locals

internal val TfDeployRootBlocks: List<BlockType> = listOf(
  Locals
)