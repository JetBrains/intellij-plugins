// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.component

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.model.Pointer
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
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
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

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
          if (member !is JSRecordType.PropertySignature) return@forEach
          when (val property = buildVuexMappedProperty(clazz, member)) {
            is VuexMappedComputedProperty -> {
              computed.add(property)
            }
            is VuexMappedMethod -> {
              methods.add(property)
            }
          }
        }

      this.computed = computed
      this.methods = methods
    }

    companion object {
      private fun buildVuexMappedProperty(clazz: JSClass, member: JSRecordType.PropertySignature): VueProperty? {
        val decorator = findDecorator(member, VUEX_DEC_MAPPERS)
        return when (decorator?.decoratorName) {
          STATE_DEC, GETTER_DEC -> {
            VuexMappedComputedProperty(clazz, member)
          }
          ACTION_DEC, MUTATION_DEC -> {
            VuexMappedMethod(clazz, member)
          }
          else -> null
        }
      }
    }

    private abstract class VuexMappedProperty(
      protected val clazz: JSClass,
      protected val member: JSRecordType.PropertySignature,
    ) : VueProperty, PsiSourcedPolySymbol {
      override val source: PsiElement? get() = member.memberSource.singleElement

      override val type: JSType? get() = member.jsType

      override val name: String = member.memberName

      abstract override fun createPointer(): Pointer<out VuexMappedProperty>

      protected fun <Prop : VuexMappedProperty, K : KClass<out Prop>> createPointer(cls: K): Pointer<Prop> {
        val clazzPtr = clazz.createSmartPointer()
        val name = member.memberName
        return Pointer {
          clazzPtr.dereference()
            ?.jsType
            ?.asRecordType()
            ?.findPropertySignature(name)
            ?.let { buildVuexMappedProperty(clazz, it) }
            ?.let { cls.safeCast(it) }
        }
      }

      override fun equals(other: Any?): Boolean =
        other === this
        || other is VuexMappedProperty
        && other.javaClass == javaClass
        && other.member.memberName == member.memberName
        && other.member.memberSource.singleElement == member.memberSource.singleElement

      override fun hashCode(): Int {
        var result = member.memberName.hashCode()
        result = 31 * result + member.memberSource.singleElement.hashCode()
        return result
      }
    }

    private class VuexMappedComputedProperty(
      clazz: JSClass,
      member: JSRecordType.PropertySignature,
    ) : VuexMappedProperty(clazz, member),
        VueComputedProperty {
      override fun createPointer(): Pointer<VuexMappedComputedProperty> =
        createPointer(this::class)
    }

    private class VuexMappedMethod(
      clazz: JSClass,
      member: JSRecordType.PropertySignature,
    ) : VuexMappedProperty(clazz, member),
        VueMethod {
      override fun createPointer(): Pointer<VuexMappedMethod> =
        createPointer(this::class)
    }
  }
}
