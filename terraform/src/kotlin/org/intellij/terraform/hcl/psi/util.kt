// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hil.psi.impl.getHCLHost

fun HCLBlock.getNameElementUnquoted(i: Int): String? {
  val elements = this.nameElements
  if (elements.size < i + 1) return null
  return when (val element = elements[i]) {
    is PsiNamedElement -> (element as PsiNamedElement).name
    is HCLIdentifier -> element.id
    is HCLStringLiteral -> element.value
    else -> null
  }
}

fun PsiElement.getPrevSiblingNonWhiteSpace(): PsiElement? {
  var prev = this.prevSibling
  while (prev != null && prev is PsiWhiteSpace) {
    prev = prev.prevSibling
  }
  return prev
}

fun PsiElement.getNextSiblingNonWhiteSpace(): PsiElement? {
  var next = this.nextSibling
  while (next != null && next is PsiWhiteSpace) {
    next = next.nextSibling
  }
  return next
}

fun <T : PsiElement, Self : PsiElementPattern<T, Self>> PsiElementPattern<T, Self>.afterSiblingSkipping2(skip: ElementPattern<out Any>, pattern: ElementPattern<out PsiElement>): Self {
  return with(object : PatternCondition<T>("afterSiblingSkipping2") {
    override fun accepts(t: T, context: ProcessingContext): Boolean {
      var o = t.prevSibling
      while (o != null) {
        if (!skip.accepts(o, context)) {
          return pattern.accepts(o, context)
        }
        o = o.prevSibling
      }
      return false
    }
  })
}

fun <T : BaseExpression, H : HCLElement, Self : PsiElementPattern<T, Self>> PsiElementPattern<T, Self>.withHCLHost(pattern: ElementPattern<in H>): Self {
  return with(object : PatternCondition<T>("withHCLHost") {
    override fun accepts(t: T, context: ProcessingContext?): Boolean {
      val host = t.getHCLHost() ?: return false
      return pattern.accepts(host)
    }
  })
}

fun PsiElement.isInHCLFileWithInterpolations(): Boolean {
  var file = containingFile
  if (file == null) {
    Logger.getInstance("#org.intellij.terraform.hcl.psi.UtilKt").warn("Cannot obtain 'containingFile' for element $this")
    return false
  }
  if (file.containingDirectory == null) {
    // Probably injected language
    file = InjectedLanguageManager.getInstance(project).getTopLevelFile(this)
  }
  return file is HCLFile && file.isInterpolationsAllowed()
}

fun <T : PsiElement> PsiElement?.getParent(aClass: Class<T>, strict: Boolean = true): T? {
  if (this == null) return null
  return PsiTreeUtil.getParentOfType(this, aClass, strict)
}