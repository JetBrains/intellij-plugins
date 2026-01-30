// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuelidate

import com.intellij.javascript.web.js.WebJSResolveUtil.resolveSymbolFromNodeModule
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.resolve.generic.JSTypeSubstitutorImpl
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.js.JS_PROPERTIES
import com.intellij.polySymbols.js.types.JSSymbolScopeType
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.index.VUE_INSTANCE_MODULE
import org.jetbrains.vuejs.model.VueInstancePropertySymbol
import org.jetbrains.vuejs.model.VueInstanceOwner
import org.jetbrains.vuejs.model.VueTypeProvider
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider
import org.jetbrains.vuejs.types.asCompleteType

class VuelidateContainerInfoProvider : VueContainerInfoProvider {

  override fun getThisTypePropertySymbols(
    instanceOwner: VueInstanceOwner,
    standardProperties: Map<String, PolySymbol>,
  ): Collection<PolySymbol> =
    listOfNotNull(
      CompositeVuelidateTypeProvider(instanceOwner.source!!, standardProperties.values.toList())
        .takeIf { it.canCreateType }
        ?.let { VueInstancePropertySymbol(name = "\$v", typeProvider = it, isReadOnly = true) }
    )

  private data class CompositeVuelidateTypeProvider(
    val source: PsiElement,
    val standardProperties: List<PolySymbol>,
  ) : VueTypeProvider {

    val canCreateType: Boolean
      get() =
        validationProps != null && validationGroups != null && validation != null

    private val validationProps
      get() = resolveSymbolFromNodeModule(
        source, VUE_INSTANCE_MODULE, "ValidationProperties", TypeScriptTypeAlias::class.java)

    private val validationGroups
      get() = resolveSymbolFromNodeModule(
        source, VUE_INSTANCE_MODULE, "ValidationGroups", TypeScriptInterface::class.java)

    private val validation
      get() = resolveSymbolFromNodeModule(
        source, "@types/vuelidate", "Validation", TypeScriptInterface::class.java)

    override fun getType(): JSType? {
      val validationProps = validationProps ?: return null
      val validationGroups = validationGroups ?: return null
      val validation = validation ?: return null

      val vueInstanceType = JSSymbolScopeType(StandardPropertiesScope(standardProperties.filter { it.name != "\$v" }), source)

      val validationPropsType = validationProps.jsType
      val substitutor = JSTypeSubstitutorImpl()
      substitutor.put(validationProps.typeParameters[0].genericId, vueInstanceType)
      val parametrizedValidationProps = JSTypeUtils.applyGenericArguments(validationProps.parsedTypeDeclaration, substitutor)
                                        ?: return null

      return JSCompositeTypeFactory.createIntersectionType(
        listOf(parametrizedValidationProps.copyWithStrict(true),
               validationGroups.jsType.copyWithStrict(true),
               validation.jsType.copyWithStrict(true)),
        validationPropsType.source.copyWithStrict(true))
        .asCompleteType()
    }

    override fun createPointer(): Pointer<out VueTypeProvider> {
      val sourcePtr = source.createSmartPointer()
      val standardPropertiesPtrs = standardProperties.map { it.createPointer() }
      return Pointer {
        val source = sourcePtr.element ?: return@Pointer null
        val standardProperties = standardPropertiesPtrs
          .map { it.dereference() ?: return@Pointer null }

        CompositeVuelidateTypeProvider(source, standardProperties)
      }

    }

  }

  private class StandardPropertiesScope(val standardProperties: List<PolySymbol>) : PolySymbolScope {

    override fun getSymbols(kind: PolySymbolKind, params: PolySymbolListSymbolsQueryParams, stack: PolySymbolQueryStack): List<PolySymbol> =
      if (kind == JS_PROPERTIES) standardProperties else emptyList()

    override fun createPointer(): Pointer<out PolySymbolScope> =
      Pointer { null }

    override fun getModificationCount(): Long = -1

  }

}