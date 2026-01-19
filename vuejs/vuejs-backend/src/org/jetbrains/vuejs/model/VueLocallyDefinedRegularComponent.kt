// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier

class VueLocallyDefinedRegularComponent
private constructor(
  override val defaultName: String?,
  override val delegate: VueRegularComponent,
  source: PsiElement,
  override val vueProximity: VueModelVisitor.Proximity = VueModelVisitor.Proximity.LOCAL,
) : VueDelegatedContainer<VueRegularComponent>(),
    VueRegularComponent {

  companion object {
    fun create(delegate: VueRegularComponent, source: JSLiteralExpression): VueLocallyDefinedRegularComponent {
      return VueLocallyDefinedRegularComponent(getTextIfLiteral(source), delegate, source)
    }

    fun create(delegate: VueRegularComponent, source: JSPsiNamedElementBase): VueLocallyDefinedRegularComponent {
      return VueLocallyDefinedRegularComponent(source.name, delegate, source)
    }
  }

  private val mySource = source

  override val name: String
    get() = defaultName ?: "<unnamed>"

  override fun withNameAndProximity(name: String, proximity: VueModelVisitor.Proximity): VueComponent =
    VueLocallyDefinedRegularComponent(name, delegate, mySource, proximity)

  override val source: PsiElement
    get() = mySource

  override val nameElement: PsiElement
    get() = if (mySource is JSPsiNamedElementBase) componentSource else mySource

  override val componentSource: PsiElement by lazy(LazyThreadSafetyMode.PUBLICATION) {
    if (mySource is JSPsiNamedElementBase)
      mySource.resolveIfImportSpecifier()
    else
      (delegate.componentSource ?: mySource)
  }

  override val rawSource: PsiElement
    get() = mySource as? JSPsiNamedElementBase ?: delegate.rawSource ?: mySource

  override val description: String?
    get() = delegate.description

  override val typeParameters: List<TypeScriptTypeParameter>
    get() = delegate.typeParameters

  override fun createPointer(): Pointer<VueLocallyDefinedRegularComponent> {
    val defaultName = this.defaultName
    val vueProximity = this.vueProximity
    val delegate = this.delegate.createPointer()
    val source = this.mySource.createSmartPointer()
    return Pointer {
      val newDelegate = delegate.dereference() ?: return@Pointer null
      val newSource = source.dereference() ?: return@Pointer null
      VueLocallyDefinedRegularComponent(defaultName, newDelegate, newSource, vueProximity)
    }
  }

  override fun equals(other: Any?): Boolean =
    other is VueLocallyDefinedRegularComponent
    && other.name == name
    && other.delegate == delegate
    && other.mySource == mySource

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + delegate.hashCode()
    result = 31 * result + mySource.hashCode()
    return result
  }
}