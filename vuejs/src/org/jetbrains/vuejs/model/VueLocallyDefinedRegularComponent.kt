// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.ecmascript6.psi.ES6Property
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.model.Pointer
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.documentation.VueItemDocumentation
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier

class VueLocallyDefinedRegularComponent(delegate: VueRegularComponent, source: JSPsiNamedElementBase)
  : VueContainerDelegate(delegate), VueRegularComponent {

  private val mySource = source

  override val nameElement: JSPsiNamedElementBase get() = source

  override val source: JSPsiNamedElementBase by lazy(LazyThreadSafetyMode.NONE) {
    mySource.resolveIfImportSpecifier()
  }

  override val defaultName: String?
    get() = source.name

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