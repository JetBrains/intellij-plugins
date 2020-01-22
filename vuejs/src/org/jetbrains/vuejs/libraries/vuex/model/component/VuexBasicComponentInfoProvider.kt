// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.component

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.PsiTreeUtil
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueNamedSymbol
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider

class VuexBasicComponentInfoProvider : VueContainerInfoProvider.VueInitializedContainerInfoProvider(::VuexComponentInfo) {

  private class VuexComponentInfo(declaration: JSObjectLiteralExpression) : VueInitializedContainerInfo(declaration) {

    override val computed: List<VueComputedProperty> get() = get(COMPUTED)
    override val methods: List<VueMethod> get() = get(METHODS)

    companion object {
      private val COMPUTED = SimpleMemberAccessor(ContainerMember.Computed, ::VuexMappedSourceComputedProperty)
      private val METHODS = SimpleMemberAccessor(ContainerMember.Methods, ::VuexMappedSourceMethod)
    }

    private class SimpleMemberAccessor<T : VueNamedSymbol>(val member: ContainerMember,
                                                           val provider: (String, JSElement) -> T)
      : ListAccessor<T>() {

      override fun build(declaration: JSObjectLiteralExpression): List<T> {
        return member.readMembers(declaration).map { (name, element) -> provider(name, element) }
      }
    }

    private enum class ContainerMember(val propertyName: String,
                                       private vararg val functionNames: String) {
      Computed("computed", "mapState", "mapGetters"),
      Methods("methods", "mapActions", "mapMutations");

      fun readMembers(descriptor: JSObjectLiteralExpression): List<Pair<String, JSElement>> {
        val property = descriptor.findProperty(propertyName) ?: return emptyList()

        PsiTreeUtil.getStubChildOfType(property, JSCallExpression::class.java)
          ?.let {
            return if (functionNames.contains((it.stubSafeMethodExpression as? JSReferenceExpression)?.referenceName)) readArguments(
              it)
            else emptyList()
          }
        return (property.objectLiteralExpressionInitializer ?: return emptyList())
          .let { PsiTreeUtil.getStubChildrenOfTypeAsList(it, JSSpreadExpression::class.java) }
          .let { StreamEx.of(it) }
          .map { PsiTreeUtil.getStubChildOfType(it, JSCallExpression::class.java) }
          .filter { it != null && functionNames.contains((it.stubSafeMethodExpression as? JSReferenceExpression)?.referenceName) }
          .flatCollection { readArguments(it!!) }
          .distinctBy { it.first }
          .toList()
      }

      fun readArguments(call: JSCallExpression): List<Pair<String, JSElement>> {
        (call as? StubBasedPsiElementBase<*>)
          ?.greenStub
          ?.let {
            return readStubbedArguments(it)
          }
        return readPsiArguments(call)
      }

      fun readPsiArguments(call: JSCallExpression): List<Pair<String, JSElement>> {
        val args = call.arguments
        val initializer = if (args.size > 1 && args[0] is JSLiteralExpression) {
          args[1]
        }
        else if (args.isNotEmpty()) {
          args[0]
        }
        else {
          return emptyList()
        }
        if (initializer is JSObjectLiteralExpression) {
          return initializer.properties.asSequence()
            .filter { it.name != null }
            .map { Pair(it.name!!, it) }
            .toList()
        }
        if (initializer is JSArrayLiteralExpression) {
          return initializer.expressions.asSequence()
            .mapNotNull { it as? JSLiteralExpression }
            .filter { it.isQuotedLiteral && it.stringValue != null }
            .map { Pair(it.stringValue!!, it) }
            .toList()
        }
        return emptyList()
      }

      fun readStubbedArguments(call: StubElement<PsiElement>): List<Pair<String, JSElement>> {
        call.findChildStubByType(JSElementTypes.OBJECT_LITERAL_EXPRESSION)
          ?.let { stub ->
            return stub.psi.properties.asSequence()
              .filter { it.name != null }
              .map { Pair(it.name!!, it) }
              .toList()
          }
        return call.getChildrenByType(JSElementTypes.LITERAL_EXPRESSION) { arrayOfNulls<JSLiteralExpression>(it) }
          .asSequence()
          .filter { it?.significantValue != null }
          .map { Pair(it!!.significantValue!!, it) }
          .toList()
      }

    }

    private class VuexMappedSourceComputedProperty(override val name: String,
                                                   override val source: PsiElement?) : VueComputedProperty

    private class VuexMappedSourceMethod(override val name: String,
                                         override val source: PsiElement?) : VueMethod
  }
}
