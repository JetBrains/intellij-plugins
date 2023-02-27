/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLElementGenerator
import org.intellij.terraform.hcl.psi.HCLExpression
import org.intellij.terraform.hcl.psi.HCLStringLiteral

class UnwrapHCLStringQuickFix(element: HCLStringLiteral) : LocalQuickFixAndIntentionActionOnPsiElement(element) {
  override fun getText() = HCLBundle.message("hil.literal.annotator.unwrap.string.quick.fix.text")
  override fun getFamilyName() = text
  override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
    val element = startElement as? HCLStringLiteral ?: return
    val key: HCLExpression
    try {
      key = HCLElementGenerator(project).createPropertyKey(element.value)
    } catch (e: IllegalStateException) {
      return
    }
    element.replace(key)
  }
}