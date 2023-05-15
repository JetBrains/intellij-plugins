// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.generate

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.impl.TemplateImpl

class GenerateModule : AbstractGenerate() {
  override val template: Template by lazy {
    val t = TemplateImpl("", "")
    t.addTextSegment("module \"")
    t.addVariable("name", InvokeCompletionExpression, InvokeCompletionExpression, true)
    t.addTextSegment("\" {\n  source = \"")
    t.addEndVariable()
    t.addTextSegment("\"\n}\n")
    t.isToReformat = true
    t.isToIndent = true
    t
  }
}