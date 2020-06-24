// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuelidate

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterface
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeAlias
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor
import org.jetbrains.vuejs.codeInsight.resolveSymbolFromNodeModule
import org.jetbrains.vuejs.index.VUE_INSTANCE_MODULE
import org.jetbrains.vuejs.model.VueInstanceOwner
import org.jetbrains.vuejs.model.createImplicitPropertySignature
import org.jetbrains.vuejs.model.source.VueSourceEntityDescriptor
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo
import org.jetbrains.vuejs.types.VueComponentInstanceType

class VuelidateContainerInfoProvider : VueContainerInfoProvider {

  override fun getInfo(descriptor: VueSourceEntityDescriptor): VueContainerInfo? = null

  override fun getThisTypeProperties(instanceOwner: VueInstanceOwner,
                                     standardProperties: MutableMap<String, JSRecordType.PropertySignature>): Collection<JSRecordType.PropertySignature> {
    val source = instanceOwner.source!!
    val validationProps = resolveSymbolFromNodeModule(
      source, VUE_INSTANCE_MODULE, "ValidationProperties", TypeScriptTypeAlias::class.java) ?: return emptyList()
    val validationGroups = resolveSymbolFromNodeModule(
      source, VUE_INSTANCE_MODULE, "ValidationGroups", TypeScriptInterface::class.java) ?: return emptyList()
    val validation = resolveSymbolFromNodeModule(
      source, "@types/vuelidate", "Validation", TypeScriptInterface::class.java) ?: return emptyList()

    val vueInstanceType = VueComponentInstanceType(JSTypeSourceFactory.createTypeSource(source, true),
                                                   instanceOwner, standardProperties.values.filter { it.memberName != "\$v" })

    val validationPropsType = validationProps.jsType
    val substitutor = JSTypeSubstitutor()
    substitutor.put(validationProps.typeParameters[0].genericId, vueInstanceType)
    val parametrizedValidationProps = JSTypeUtils.applyGenericArguments(validationProps.parsedTypeDeclaration, substitutor)
                                      ?: return emptyList()

    val compositeVuelidateType = JSCompositeTypeFactory.createIntersectionType(
      listOf(parametrizedValidationProps, validationGroups.jsType, validation.jsType),
      validationPropsType.source.copyWithStrict(false))

    return listOf(createImplicitPropertySignature("\$v", compositeVuelidateType,
                                                  standardProperties["\$v"]?.memberSource?.singleElement ?: source,
                                                  isReadOnly = true))
  }

}