// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.editor

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.template.psi.TftplFile

class TftplContextType : TemplateContextType(HCLBundle.message("live.template.context.name")) {
  override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
    return templateActionContext.file.originalFile is TftplFile
  }
}