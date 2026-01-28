// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.polySymbols.utils.PolySymbolDeclaredInPsi
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.codeInsight.toAsset
import org.jetbrains.vuejs.web.VueComponentSourceNavigationTarget
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

sealed class VueLocallyDefinedComponent<T : PsiElement>(
  override val name: String,
  delegate: VueComponent,
  val sourceElement: T,
  val isCompositionAppComponent: Boolean = false,
) : VueDelegatedComponent<VueComponent>(delegate) {

  companion object {

    fun createFromInitializerTextLiteral(
      delegate: VueComponent,
      source: JSInitializerOwner,
    ): VueLocallyDefinedComponent<*>? =
      source.asSafely<JSTypeOwner>()?.jsType
        ?.asSafely<JSStringLiteralTypeImpl>()?.literal
        ?.let { VueInitializerTextLiteralLocallyDefinedComponent(it, delegate, source) }

    fun create(
      delegate: VueComponent,
      source: JSPsiNamedElementBase,
    ): VueLocallyDefinedComponent<*>? =
      source.name
        ?.let { toAsset(it, true) }
        ?.let { VuePsiNamedElementLocallyDefinedComponent(it, delegate, source) }

    fun create(
      delegate: VueComponent,
      source: JSLiteralExpression,
      isCompositionAppComponent: Boolean = false,
    ): VueLocallyDefinedComponent<*>? =
      getTextIfLiteral(source)
        ?.let { VueStringLiteralLocallyDefinedComponent(it, delegate, source, isCompositionAppComponent) }

    fun create(
      delegate: VueComponent,
      source: PsiFile,
    ): VueLocallyDefinedComponent<*> =
      VueFileLocallyDefinedComponent(toAsset(FileUtilRt.getNameWithoutExtension(source.name), true), delegate, source)

    private fun create(delegate: VueComponent, source: PsiElement, isCompositionAppComponent: Boolean): VueLocallyDefinedComponent<*>? =
      when (source) {
        is JSPsiNamedElementBase -> create(delegate, source)
        is JSLiteralExpression -> create(delegate, source, isCompositionAppComponent)
        is PsiFile -> create(delegate, source)
        else -> null
      }
  }

  override val source: PsiElement
    get() = sourceElement

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueLocallyDefinedComponent<*>
    && other.javaClass == javaClass
    && other.name == name
    && other.delegate == delegate
    && other.sourceElement == sourceElement
    && other.isCompositionAppComponent == isCompositionAppComponent

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + delegate.hashCode()
    result = 31 * result + sourceElement.hashCode()
    result = 31 * result + isCompositionAppComponent.hashCode()
    return result
  }

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    symbol === this ||
    symbol is VueLocallyDefinedComponent<*>
    && symbol.javaClass == javaClass
    && symbol.name == name
    && symbol.delegate == delegate
    && symbol.sourceElement == sourceElement

  protected fun <C : VueLocallyDefinedComponent<T>> createPointer(clazz: KClass<C>): Pointer<C> {
    val isCompositionAppComponent = this.isCompositionAppComponent
    val delegate = this.delegate.createPointer()
    val sourceElement = this.sourceElement.createSmartPointer()
    return Pointer {
      val newDelegate = delegate.dereference() ?: return@Pointer null
      val newSourceElement = sourceElement.dereference() ?: return@Pointer null
      clazz.safeCast(create(delegate = newDelegate, source = newSourceElement, isCompositionAppComponent = isCompositionAppComponent))
    }
  }

  abstract override fun createPointer(): Pointer<out VueLocallyDefinedComponent<T>>
}

private class VuePsiNamedElementLocallyDefinedComponent(
  name: String,
  delegate: VueComponent,
  sourceElement: JSPsiNamedElementBase,
) : VueLocallyDefinedComponent<JSPsiNamedElementBase>(name, delegate, sourceElement),
    VuePsiSourcedComponent {

  override val elementToImport: PsiElement? by lazy(LazyThreadSafetyMode.PUBLICATION) {
    sourceElement.resolveIfImportSpecifier().takeIf { it !is JSProperty }
    ?: delegate.elementToImport
  }

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    delegate.getNavigationTargets(project)
      .ifEmpty { listOf(VueComponentSourceNavigationTarget(sourceElement)) }

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    super<VuePsiSourcedComponent>.isEquivalentTo(symbol)
    || super<VueLocallyDefinedComponent>.isEquivalentTo(symbol)

  override fun createPointer(): Pointer<VuePsiNamedElementLocallyDefinedComponent> =
    createPointer(VuePsiNamedElementLocallyDefinedComponent::class)
}

private class VueFileLocallyDefinedComponent(
  name: String,
  delegate: VueComponent,
  source: PsiFile,
) : VueLocallyDefinedComponent<PsiFile>(name, delegate, source),
    VueFileComponent {

  override val source: PsiFile
    get() = sourceElement

  override val elementToImport: PsiElement
    get() = source

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    delegate.getNavigationTargets(project)
      .ifEmpty { listOf(VueComponentSourceNavigationTarget(sourceElement)) }

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    super<VueFileComponent>.isEquivalentTo(symbol)
    || super<VueLocallyDefinedComponent>.isEquivalentTo(symbol)

  override fun createPointer(): Pointer<VueFileLocallyDefinedComponent> =
    createPointer(VueFileLocallyDefinedComponent::class)
}

private class VueStringLiteralLocallyDefinedComponent(
  name: String,
  delegate: VueComponent,
  sourceElement: JSLiteralExpression,
  isCompositionAppComponent: Boolean,
) : VueLocallyDefinedComponent<JSLiteralExpression>(name, delegate, sourceElement, isCompositionAppComponent),
    PolySymbolDeclaredInPsi {

  override val textRangeInSourceElement: TextRange
    get() = TextRange(1, sourceElement.textRange.length - 1)

  override val psiContext: PsiElement?
    get() = super<PolySymbolDeclaredInPsi>.psiContext

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    delegate.getNavigationTargets(project)
      .ifEmpty { super.getNavigationTargets(project) }

  override val renameTarget: PolySymbolRenameTarget?
    get() = PolySymbolRenameTarget.create(this)

  override fun createPointer(): Pointer<VueStringLiteralLocallyDefinedComponent> =
    createPointer(VueStringLiteralLocallyDefinedComponent::class)
}

private class VueInitializerTextLiteralLocallyDefinedComponent(
  name: String,
  delegate: VueComponent,
  sourceElement: JSInitializerOwner,
) : VueLocallyDefinedComponent<JSInitializerOwner>(name, delegate, sourceElement) {

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> =
    delegate.getNavigationTargets(project)
      .ifEmpty { listOf(VueComponentSourceNavigationTarget(sourceElement)) }

  override fun createPointer(): Pointer<VueInitializerTextLiteralLocallyDefinedComponent> {
    val delegatePtr = delegate.createPointer()
    val sourcePtr = sourceElement.createSmartPointer()
    return Pointer {
      val delegate = delegatePtr.dereference() ?: return@Pointer null
      val source = sourcePtr.dereference() ?: return@Pointer null
      createFromInitializerTextLiteral(delegate, source) as? VueInitializerTextLiteralLocallyDefinedComponent
    }
  }
}