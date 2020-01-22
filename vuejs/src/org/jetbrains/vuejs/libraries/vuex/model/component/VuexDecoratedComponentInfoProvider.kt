// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.component

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.findDecorator
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueNamedSymbol
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

class VuexDecoratedComponentInfoProvider : VueContainerInfoProvider.VueDecoratedContainerInfoProvider(::VuexDecoratedComponentInfo) {

  private class VuexDecoratedComponentInfo constructor(clazz: JSClass) : VueContainerInfo {
    override val computed: List<VueComputedProperty>
    override val methods: List<VueMethod>

    init {
      val computed = mutableListOf<VueComputedProperty>()
      val methods = mutableListOf<VueMethod>()

      clazz.jsType
        .asRecordType()
        .typeMembers
        .forEach { member ->
          val decorator = findDecorator(member, DECS)
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

    companion object {

      private const val STATE_DEC = "State"
      private const val GETTER_DEC = "Getter"
      private const val ACTION_DEC = "Action"
      private const val MUTATION_DEC = "Mutation"

      private val DECS = setOf(STATE_DEC, GETTER_DEC, ACTION_DEC, MUTATION_DEC)
    }

    private abstract class VuexMappedSymbol(protected val member: JSRecordType.PropertySignature)
      : VueNamedSymbol {
      override val source: PsiElement? get() = member.memberSource.singleElement
      override val name: String = member.memberName
    }

    private class VuexMappedComputedProperty(member: JSRecordType.PropertySignature)
      : VuexMappedSymbol(member), VueComputedProperty

    private class VuexMappedMethod(member: JSRecordType.PropertySignature)
      : VuexMappedSymbol(member), VueMethod

  }
}
