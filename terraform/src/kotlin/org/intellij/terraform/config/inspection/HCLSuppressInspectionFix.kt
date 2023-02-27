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
package org.intellij.terraform.config.inspection

import com.intellij.codeInsight.daemon.impl.actions.AbstractBatchSuppressByNoInspectionCommentFix
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.annotations.Nls

class HCLSuppressInspectionFix<out T : PsiElement>(id: String,
                                                   private val clazz: Class<T>,
                                                   private val textProvider: (HCLSuppressInspectionFix<T>) -> String)
  : AbstractBatchSuppressByNoInspectionCommentFix(id, false) {

  constructor(id: String, text: @Nls String, clazz: Class<T>) : this(id, clazz, { text })

  override fun getText() = textProvider(this)

  override fun getContainer(context: PsiElement?): T? {
    return PsiTreeUtil.getParentOfType(context, clazz, false)
  }
}