// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.componentDecorator

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.JSRecordType.TypeMember
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.codeInsight.*
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo
import java.util.*

class VueDecoratedComponentInfoProvider : VueContainerInfoProvider.VueDecoratedContainerInfoProvider(::VueDecoratedComponentInfo) {

  private class VueDecoratedComponentInfo constructor(clazz: JSClass) : VueContainerInfo {
    override val mixins: List<VueMixin>
    override val extends: List<VueMixin>
    override val data: List<VueDataProperty>
    override val computed: List<VueComputedProperty>
    override val methods: List<VueMethod>
    override val emits: List<VueEmitCall>
    override val props: List<VueInputProperty>
    override val model: VueModelDirectiveProperties?

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
          val decorator = findDecorator(member, DECS)
          when (decorator?.decoratorName) {
            PROP_DEC -> if (member is PropertySignature) {
              props.add(VueDecoratedInputProperty(member.memberName, member, decorator, 0))
            }
            PROP_SYNC_DEC -> if (member is PropertySignature) {
              computed.add(VueDecoratedComputedProperty(member.memberName, member, decorator, 1))
              getNameFromDecorator(decorator)?.let { name ->
                props.add(VueDecoratedInputProperty(name, member, decorator, 1))
                emits.add(VueDecoratedPropertyEmitCall("update:$name", member))
              }
            }
            MODEL_DEC -> if (member is PropertySignature && model === null) {
              val name = getNameFromDecorator(decorator)
              model = VueModelDirectiveProperties(member.memberName,
                                                  name ?: VueModelDirectiveProperties.DEFAULT_EVENT)
              props.add(VueDecoratedInputProperty(member.memberName, member, decorator, 1))
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
                    computed.add(VueDecoratedComputedProperty(member.memberName, member, null, 0))
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

      clazz.extendsList?.members?.forEach { extendItem ->
        if (extendItem.referenceText == null) {
          val call = extendItem.expression as? JSCallExpression
          if ((call?.methodExpression as? JSReferenceExpression)?.referenceName?.toLowerCase(Locale.US) == "mixins") {
            call.arguments.mapNotNullTo(mixins) { arg ->
              (arg as? JSReferenceExpression)?.resolve()
                ?.let { VueModelManager.getMixin(it) }
            }
          }
        }
        else {
          extendItem.classes.mapNotNullTo(mixins) {
            VueModelManager.getMixin(it)
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

      private const val PROP_DEC = "Prop"
      private const val PROP_SYNC_DEC = "PropSync"
      private const val MODEL_DEC = "Model"
      private const val EMIT_DEC = "Emit"

      private val DECS = setOf(PROP_DEC, PROP_SYNC_DEC, MODEL_DEC, EMIT_DEC)

      private fun getNameFromDecorator(decorator: ES6Decorator): String? {
        return getDecoratorArgument(decorator, 0)
          ?.let { getTextIfLiteral(it) }
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

    private class VueDecoratedInputProperty(name: String, member: PropertySignature, decorator: ES6Decorator?, decoratorArgumentIndex: Int)
      : VueDecoratedProperty(name, member), VueInputProperty {

      override val jsType: JSType = VueDecoratedComponentPropType(member, decorator, decoratorArgumentIndex)
      override val required: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        getRequiredFromPropOptions(getDecoratorArgument(decorator, decoratorArgumentIndex))
      }
    }

    private class VueDecoratedComputedProperty(name: String,
                                               member: PropertySignature,
                                               decorator: ES6Decorator?,
                                               decoratorArgumentIndex: Int)
      : VueDecoratedProperty(name, member), VueComputedProperty {

      override val jsType: JSType = VueDecoratedComponentPropType(member, decorator, decoratorArgumentIndex)
      override val source: PsiElement = VueImplicitElement(name, jsType,
                                                           member.memberSource.singleElement!!,
                                                           JSImplicitElement.Type.Property, false)
    }

    private class VueDecoratedDataProperty(member: PropertySignature)
      : VueDecoratedProperty(member.memberName, member), VueDataProperty

    private class VueDecoratedPropertyEmitCall(name: String, member: PropertySignature)
      : VueDecoratedNamedSymbol<PropertySignature>(name, member), VueEmitCall {
      override val eventJSType: JSType? get() = member.jsType
    }

    private class VueDecoratedPropertyMethod(name: String, member: PropertySignature)
      : VueDecoratedProperty(name, member), VueMethod

  }


}
