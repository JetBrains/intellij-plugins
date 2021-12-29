// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.vuejs.codeInsight.documentation.VueItemDocumentation
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier

class VueLocallyDefinedRegularComponent
private constructor(override val delegate: VueRegularComponent, source: PsiElement)
  : VueDelegatedContainer<VueRegularComponent>(), VueRegularComponent {

  constructor(delegate: VueRegularComponent, source: JSLiteralExpression)
    : this(delegate, source as PsiElement)

  constructor(delegate: VueRegularComponent, source: JSPsiNamedElementBase)
    : this(delegate, source as PsiElement)

  private val mySource = source

  override val nameElement: PsiElement
    get() = if (mySource is JSPsiNamedElementBase) source else mySource

  override val source: PsiElement by lazy(LazyThreadSafetyMode.NONE) {
    if (mySource is JSPsiNamedElementBase)
      mySource.resolveIfImportSpecifier()
    else
      (delegate.source ?: mySource)
  }

  override val defaultName: String?
    get() = (source as? JSPsiNamedElementBase)?.name
            ?: getTextIfLiteral(source as JSLiteralExpression)

  override val documentation: VueItemDocumentation
    get() = delegate.documentation


  override fun createPointer(): Pointer<VueLocallyDefinedRegularComponent> {
    val delegate = this.delegate.createPointer()
    val source = this.source.createSmartPointer()
    return Pointer {
      val newDelegate = delegate.dereference() ?: return@Pointer null
      val newSource = source.dereference() ?: return@Pointer null
      VueLocallyDefinedRegularComponent(newDelegate, newSource)
    }
  }

  override fun equals(other: Any?): Boolean =
    other is VueLocallyDefinedRegularComponent
    && other.delegate == delegate
    && other.mySource == mySource

  override fun hashCode(): Int =
    delegate.hashCode() + mySource.hashCode()
}