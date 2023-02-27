/*
 * Copyright 2000-2018 JetBrains s.r.o.
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
package org.intellij.terraform.hil.psi

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.FakePsiElement

/**
 * Based on com.intellij.psi.impl.RenameableFakePsiElement
 */
abstract class RenameableFakePsiElement protected constructor(private val myParent: PsiElement) : FakePsiElement() {

  override fun getParent(): PsiElement {
    return myParent
  }

  override fun getContainingFile(): PsiFile {
    return myParent.containingFile
  }

  abstract override fun getName(): String?

  override fun getLanguage(): Language {
    return containingFile.language
  }

  override fun getProject(): Project {
    return myParent.project
  }

  override fun getManager(): PsiManager {
    return PsiManager.getInstance(project)
  }

  override fun getTextRange(): TextRange? {
    return TextRange.from(0, 0)
  }
}
