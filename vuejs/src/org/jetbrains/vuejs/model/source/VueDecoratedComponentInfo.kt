// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.JSRecordType.TypeMember
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceListMember
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.getJSTypeFromPropOptions
import org.jetbrains.vuejs.codeInsight.getRequiredFromPropOptions
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.model.*

class VueDecoratedComponentInfo private constructor(clazz: JSClass<*>) {
  val mixins: List<VueMixin>
  val extends: List<VueMixin>
  val data: List<VueDataProperty>
  val computed: List<VueComputedProperty>
  val methods: List<VueMethod>
  val emits: List<VueEmitCall>
  val props: List<VueInputProperty>
  val model: VueModelDirectiveProperties?

  init {
    val mixins = mutableListOf<VueMixin>()
    val extends = mutableListOf<VueMixin>()
    val data = mutableListOf<VueDataProperty>()
    val computed = mutableListOf<VueComputedProperty>()
    val methods = mutableListOf<VueMethod>()
    val emits = mutableListOf<VueEmitCall>()
    val props = mutableListOf<VueInputProperty>()
    var model: VueModelDirectiveProperties? = null

    clazz.jsType
      .asRecordType()
      .typeMembers
      .forEach { member ->
        val decorator = findDecorator(member)
        when (decorator?.decoratorName) {
          PROP_DEC -> if (member is PropertySignature) {
            props.add(VueDecoratedInputProperty(member.memberName, member, getPropOptionsFromDec(decorator, 0)))
          }
          PROP_SYNC_DEC -> if (member is PropertySignature) {
            computed.add(VueDecoratedComputedProperty(member.memberName, member))
            getNameFromDecorator(decorator)?.let { name ->
              props.add(VueDecoratedInputProperty(name, member, getPropOptionsFromDec(decorator, 1)))
              emits.add(VueDecoratedPropertyEmitCall("update:$name", member))
            }
          }
          MODEL_DEC -> if (member is PropertySignature && model === null) {
            val name = getNameFromDecorator(decorator)
            model = VueModelDirectiveProperties(member.memberName,
                                                name ?: VueModelDirectiveProperties.DEFAULT_EVENT)
            props.add(VueDecoratedInputProperty(member.memberName, member, getPropOptionsFromDec(decorator, 1)))
          }
          EMIT_DEC -> if (member is PropertySignature) {
            if (member.memberSource.singleElement is JSFunction) {
              emits.add(VueDecoratedPropertyEmitCall(getNameFromDecorator(decorator)
                                                     ?: fromAsset(member.memberName),
                                                     member))
              methods.add(VueDecoratedPropertyMethod(member.memberName, member))
            }
          }
          else -> when (member) {
            is PropertySignature -> {
              val source = member.memberSource.singleElement
              if (source is JSFunction) {
                if (source.isGetProperty || source.isSetProperty) {
                  computed.add(VueDecoratedComputedProperty(member.memberName, member))
                }
                else {
                  methods.add(VueDecoratedPropertyMethod(member.memberName, member))
                }
              }
              else if (source is JSAttributeListOwner) {
                data.add(VueDecoratedDataProperty(member))
              }
            }
          }
        }
      }

    clazz.extendsList?.children?.forEach { extendItem ->
      val call = ((extendItem as? JSReferenceListMember)?.expression as? JSCallExpression)
      if ((call?.methodExpression as? JSReferenceExpression)?.referenceName == "mixins") {
        call.arguments.mapNotNullTo(mixins) { arg ->
          (arg as? JSReferenceExpression)?.resolve()
            ?.let { VueModelManager.getMixin(it) }
        }
      }
    }

    this.mixins = mixins
    this.extends = extends
    this.data = data
    this.computed = computed
    this.methods = methods
    this.emits = emits
    this.props = props
    this.model = model
  }

  companion object {
    fun get(clazz: JSClass<*>?): VueDecoratedComponentInfo? {
      return CachedValuesManager.getCachedValue(clazz ?: return null) {
        val dependencies = mutableListOf<Any>()
        JSClassUtils.processClassesInHierarchy(clazz, true) { aClass, _, _ ->
          dependencies.add(aClass.containingFile)
          true
        }
        CachedValueProvider.Result.create(VueDecoratedComponentInfo(clazz), dependencies)
      }
    }

    private const val PROP_DEC = "Prop"
    private const val PROP_SYNC_DEC = "PropSync"
    private const val MODEL_DEC = "Model"
    private const val EMIT_DEC = "Emit"

    private val DECS = setOf(PROP_DEC, PROP_SYNC_DEC, MODEL_DEC, EMIT_DEC)

    private fun findDecorator(member: TypeMember): ES6Decorator? {
      return (member.memberSource.singleElement as? JSAttributeListOwner)
        ?.attributeList
        ?.decorators
        ?.find { DECS.contains(it.decoratorName) }
    }

    private fun getNameFromDecorator(decorator: ES6Decorator): String? {
      return (decorator.expression as? JSCallExpression)
        ?.arguments
        ?.firstOrNull()
        ?.let { getTextIfLiteral(it) }
    }

    private fun getPropOptionsFromDec(decorator: ES6Decorator, index: Int): JSExpression? {
      return (decorator.expression as? JSCallExpression)
        ?.arguments
        ?.getOrNull(index)
    }
  }

  private abstract class VueDecoratedNamedSymbol<T : TypeMember>(override val name: String, protected val member: T)
    : VueNamedSymbol {
    override val source: PsiElement? get() = member.memberSource.singleElement
  }

  private abstract class VueDecoratedProperty(name: String, member: PropertySignature)
    : VueDecoratedNamedSymbol<PropertySignature>(name, member), VueProperty {
    override val jsType: JSType? get() = member.jsType
  }

  private class VueDecoratedInputProperty(name: String, member: PropertySignature, propOptions: JSExpression?)
    : VueDecoratedProperty(name, member), VueInputProperty {
    val typeFromProps = getJSTypeFromPropOptions(propOptions)
    override val jsType: JSType? get() = typeFromProps ?: member.jsType
    override val required: Boolean = getRequiredFromPropOptions(propOptions)
  }

  private class VueDecoratedComputedProperty(name: String, member: PropertySignature)
    : VueDecoratedProperty(name, member), VueComputedProperty

  private class VueDecoratedDataProperty(member: PropertySignature)
    : VueDecoratedProperty(member.memberName, member), VueDataProperty

  private class VueDecoratedPropertyEmitCall(name: String, member: PropertySignature)
    : VueDecoratedNamedSymbol<PropertySignature>(name, member), VueEmitCall {
    override val eventJSType: JSType? get() = member.jsType
  }

  private class VueDecoratedPropertyMethod(name: String, member: PropertySignature)
    : VueDecoratedNamedSymbol<PropertySignature>(name, member), VueMethod

}

