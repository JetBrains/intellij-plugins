// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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