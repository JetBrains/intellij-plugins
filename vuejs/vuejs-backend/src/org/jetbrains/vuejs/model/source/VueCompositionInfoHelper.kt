// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeOwner
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSAliasTypeImpl
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSContextualUnionType
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutionContextImpl
import com.intellij.lang.javascript.psi.util.stubSafeCallArguments
import com.intellij.model.Pointer
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier
import org.jetbrains.vuejs.index.getFunctionNameFromVueIndex
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueDataProperty
import org.jetbrains.vuejs.model.VueImplicitElement
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueProperty
import org.jetbrains.vuejs.model.VueSymbol
import org.jetbrains.vuejs.types.VueUnwrapRefType
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

object VueCompositionInfoHelper {

  private const val READ_ONLY_TYPE = "ReadOnly"

  fun <T : PsiElement> createRawBindings(context: PsiElement, typeSource: T, setupTypeProvider: (T) -> JSType?): List<VueSymbol> =
    setupTypeProvider(typeSource)
      ?.asRecordType()
      ?.typeMembers
      ?.mapNotNull {
        mapSignatureToRawBinding(it as? JSRecordType.PropertySignature ?: return@mapNotNull null,
                                 context, typeSource, setupTypeProvider)
      }
    ?: emptyList()

  fun getUnwrappedRefElement(element: PsiElement?, context: PsiElement): VueImplicitElement? {
    val resolved = (element as? JSPsiNamedElementBase)?.resolveIfImportSpecifier()
    val jsType = (resolved as? JSTypeOwner)?.jsType
    val name = resolved?.name
    return if (jsType != null && name != null) {
      VueImplicitElement(name, VueUnwrapRefType(jsType, context), resolved, JSImplicitElement.Type.Property, true)
    }
    else null
  }

  internal fun substituteRefType(type: JSType, context: JSTypeSubstitutionContextImpl? = null): JSType {
    var result = if (context != null) type.substitute(context) else type.substitute()
    if (result is JSAliasTypeImpl)
      result = result.alias.let { if (context != null) it.substitute(context) else it.substitute() }
    if (result is JSContextualUnionType) {
      // Find first Ref, which is not Ref<any>
      result = result.types.find {
        it is JSGenericTypeImpl
        && (it.type as? JSTypeImpl)?.typeText == "Ref"
        && it.arguments.getOrNull(0) !is JSAnyType
      } ?: result
    }
    return result ?: type
  }

  private fun <T : PsiElement> mapSignatureToRawBinding(
    signature: JSRecordType.PropertySignature,
    psiContext: PsiElement,
    setupTypeSource: T,
    setupTypeProvider: (T) -> JSType?,
  ): VueSymbol? {
    val name = signature.memberName
    val source = signature.memberSource.singleElement
    if (source !is PsiNamedElement || source.name != name)
      return null
    val context = JSTypeSubstitutionContextImpl(psiContext)
    var signatureType = signature.jsType?.let { substituteRefType(it, context) }
    var isReadOnly = false
    if (signatureType is JSAliasTypeImpl) {
      signatureType = signatureType.alias
    }
    when (signatureType) {
      is JSGenericTypeImpl -> {
        when ((signatureType.type as? JSTypeImpl)?.typeText) {
          READ_ONLY_TYPE -> isReadOnly = true
        }
      }
      is JSFunctionType -> {
        return VueComposedMethod(name = name,
                                 source = source,
                                 type = signature.jsType,
                                 psiContext = psiContext,
                                 setupTypeSource = setupTypeSource,
                                 setupTypeProvider = setupTypeProvider)
      }
    }

    val unwrapRefTypeSource =
      psiContext.asSafely<JSCallExpression>()
        ?.takeIf { getFunctionNameFromVueIndex(it) == DEFINE_EXPOSE_FUN }
        ?.stubSafeCallArguments?.getOrNull(0)
        ?.asSafely<JSObjectLiteralExpression>()
      ?: psiContext
    val type = signatureType?.let { VueUnwrapRefType(it, unwrapRefTypeSource) }
    return if (isReadOnly) {
      VueComposedComputedProperty(name, source, type, psiContext, setupTypeSource, setupTypeProvider)
    }
    else {
      VueComposedDataProperty(name, source, type, psiContext, setupTypeSource, setupTypeProvider)
    }
  }


  private abstract class VueComposedProperty<T : PsiElement>(
    override val name: String,
    override val source: PsiNamedElement,
    override val type: JSType?,
    override val psiContext: PsiElement,
    private val setupTypeSource: T,
    private val setupTypeProvider: (T) -> JSType?,
  ) : VueProperty, PsiSourcedPolySymbol {

    abstract override fun createPointer(): Pointer<out VueComposedProperty<T>>

    override fun equals(other: Any?): Boolean =
      other === this
      || other is VueComposedProperty<*>
      && other.javaClass == javaClass
      && other.name == name
      && other.psiContext == psiContext
      && other.setupTypeSource == setupTypeSource

    override fun hashCode(): Int {
      var result = name.hashCode()
      result = 31 * result + psiContext.hashCode()
      result = 31 * result + setupTypeSource.hashCode()
      return result
    }

    protected fun <T : PsiElement, Prop : VueComposedProperty<T>, K : KClass<out Prop>> createPointer(cls: K): Pointer<Prop> {
      val name = name
      val setupTypeSourcePtr = setupTypeSource.createSmartPointer()
      val setupTypeProvider = setupTypeProvider
      val psiContextPtr = psiContext.createSmartPointer()
      return Pointer<Prop> {
        val setupTypeSource = setupTypeSourcePtr.dereference() ?: return@Pointer null
        val psiContext = psiContextPtr.dereference() ?: return@Pointer null
        return@Pointer setupTypeProvider(setupTypeSource)
          ?.asRecordType()
          ?.findPropertySignature(name)
          ?.let { mapSignatureToRawBinding(it, psiContext, setupTypeSource, setupTypeProvider) }
          ?.let { cls.safeCast(it) }
      }
    }
  }

  private class VueComposedDataProperty<T : PsiElement>(
    name: String,
    source: PsiNamedElement,
    type: JSType?,
    psiContext: PsiElement,
    setupTypeSource: T,
    setupTypeProvider: (T) -> JSType?,
  ) : VueComposedProperty<T>(name, source, type, psiContext, setupTypeSource, setupTypeProvider), VueDataProperty {

    override fun createPointer(): Pointer<VueComposedDataProperty<T>> =
      createPointer(this::class)
  }

  private class VueComposedComputedProperty<T : PsiElement>(
    name: String,
    source: PsiNamedElement,
    type: JSType?,
    psiContext: PsiElement,
    setupTypeSource: T,
    setupTypeProvider: (T) -> JSType?,
  ) : VueComposedProperty<T>(name, source, type, psiContext, setupTypeSource, setupTypeProvider), VueComputedProperty {

    override fun createPointer(): Pointer<VueComposedComputedProperty<T>> =
      createPointer(this::class)
  }

  private class VueComposedMethod<T : PsiElement>(
    name: String,
    source: PsiNamedElement,
    type: JSType?,
    psiContext: PsiElement,
    setupTypeSource: T,
    setupTypeProvider: (T) -> JSType?,
  ) : VueComposedProperty<T>(name, source, type, psiContext, setupTypeSource, setupTypeProvider), VueMethod {

    override fun createPointer(): Pointer<VueComposedMethod<T>> =
      createPointer(this::class)
  }

}