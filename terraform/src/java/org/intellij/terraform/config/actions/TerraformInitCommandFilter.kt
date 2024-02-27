// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.execution.filters.Filter
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.text.findTextRange


class TerraformInitCommandFilter(
  val project: Project,
  val directory: String,
) : Filter {

  val command: String = "terraform init"

  override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
    if (!line.contains(command)) return null
    val textRange = line.findTextRange(command) ?: return null
    return Filter.Result(entireLength - line.length + textRange.startOffset,
                         entireLength - line.length + textRange.endOffset
    ) {
      project.service<TerraformActionService>().scheduleTerraformInit(directory, notifyOnSuccess = true)
    }
  }

}

