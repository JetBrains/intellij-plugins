// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.component

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.findDecorator
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.ACTION_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTER_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MUTATION_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STATE_DEC
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.VUEX_DEC_MAPPERS
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueProperty
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

class VuexDecoratedComponentInfoProvider : VueContainerInfoProvider.VueDecoratedContainerInfoProvider(::VuexDecoratedComponentInfo) {

  private class VuexDecoratedComponentInfo(clazz: JSClass) : VueContainerInfo {
    override val computed: List<VueComputedProperty>
    override val methods: List<VueMethod>

    init {
      val computed = mutableListOf<VueComputedProperty>()
      val methods = mutableListOf<VueMethod>()

      clazz.jsType
        .asRecordType()
        .typeMembers
        .forEach { member ->
          val decorator = findDecorator(member, VUEX_DEC_MAPPERS)
          when (decorator?.decoratorName) {
            STATE_DEC,
            GETTER_DEC -> if (member is JSRecordType.PropertySignature) {
              computed.add(VuexMappedComputedProperty(member))
            }
            ACTION_DEC,
            MUTATION_DEC -> if (member is JSRecordType.PropertySignature) {
              methods.add(VuexMappedMethod(member))
            }
          }
        }

      this.computed = computed
      this.methods = methods
    }

    private abstract class VuexMappedProperty(protected val member: JSRecordType.PropertySignature)
      : VueProperty {
      override val source: PsiElement? get() = member.memberSource.singleElement
      override val jsType: JSType? get() = member.jsType
      override val name: String = member.memberName
    }

    private class VuexMappedComputedProperty(member: JSRecordType.PropertySignature)
      : VuexMappedProperty(member), VueComputedProperty

    private class VuexMappedMethod(member: JSRecordType.PropertySignature)
      : VuexMappedProperty(member), VueMethod

  }
}
