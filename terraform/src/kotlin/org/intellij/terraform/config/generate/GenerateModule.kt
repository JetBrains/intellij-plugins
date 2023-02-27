/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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