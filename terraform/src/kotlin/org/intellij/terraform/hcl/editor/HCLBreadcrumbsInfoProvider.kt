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
package org.intellij.terraform.hcl.editor

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLProperty

open class HCLBreadcrumbsInfoProvider : BreadcrumbsProvider {
  override fun getLanguages(): Array<Language> {
    return arrayOf(HCLLanguage)
  }

  override fun acceptElement(e: PsiElement): Boolean {
    return e is HCLBlock || e is HCLProperty
  }

  override fun getElementInfo(e: PsiElement): String {
    if (e is HCLBlock) {
      return e.fullName
    }
    if (e is HCLProperty) {
      return e.name
    }
    throw AssertionError("Only HCLBlock and HCLProperty supported, actual is " + e.javaClass.name)
  }

  override fun getElementTooltip(e: PsiElement): String? {
    return null
  }
}
