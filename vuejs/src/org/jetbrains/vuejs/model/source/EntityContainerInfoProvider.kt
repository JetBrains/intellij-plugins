// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclarationPart
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSKeywordElementType
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.collectPropertiesRecursively
import org.jetbrains.vuejs.codeInsight.getStringLiteralsFromInitializerArray
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

interface EntityContainerInfoProvider<T> {

  @JvmDefault
  fun getInfo(descriptor: VueSourceEntityDescriptor): T? = null

  abstract class DecoratedContainerInfoProvider<T>(val createInfo: (clazz: JSClass) -> T) : EntityContainerInfoProvider<T> {

    final override fun getInfo(descriptor: VueSourceEntityDescriptor): T? =
      descriptor.clazz?.let {
        val manager = CachedValuesManager.getManager(it.project)
        manager.getCachedValue(it, manager.getKeyForClass<T>(this::class.java), {
          val dependencies = mutableListOf<Any>()
          JSClassUtils.processClassesInHierarchy(it, true) { aClass, _, _ ->
            dependencies.add(aClass)
            dependencies.add(aClass.containingFile)
            true
          }
          CachedValueProvider.Result.create(createInfo(it), dependencies)
        }, false)
      }
  }

  abstract class InitializedContainerInfoProvider<T>(val createInfo: (initializer: JSObjectLiteralExpression) -> T) : EntityContainerInfoProvider<T> {

    final override fun getInfo(descriptor: VueSourceEntityDescriptor): T? =
      descriptor.initializer?.let {
        val manager = CachedValuesManager.getManager(it.project)
        manager.getCachedValue(it, manager.getKeyForClass<T>(this::class.java), {
          CachedValueProvider.Result.create(createInfo(it), PsiModificationTracker.MODIFICATION_COUNT)
        }, false)
      }

    protected abstract class InitializedContainerInfo(val declaration: JSObjectLiteralExpression) {
      private val values: MutableMap<MemberAccessor<*>, Any?> = ConcurrentHashMap()

      protected fun <T> get(accessor: MemberAccessor<T>): T {
        @Suppress("UNCHECKED_CAST")
        return values.computeIfAbsent(accessor, Function { it.build(declaration) }) as T
      }
    }

    abstract class MemberAccessor<T> {
      abstract fun build(declaration: JSObjectLiteralExpression): T
    }

    abstract class ListAccessor<T> : MemberAccessor<List<T>>()

    abstract class MapAccessor<T> : MemberAccessor<Map<String, T>>()

    class SimpleMemberAccessor<T>(private val memberReader: MemberReader,
                                  private val provider: (String, JSElement) -> T)
      : ListAccessor<T>() {

      override fun build(declaration: JSObjectLiteralExpression): List<T> {
        return memberReader.readMembers(declaration).map { (name, element) -> provider(name, element) }
      }
    }


    class SimpleMemberMapAccessor<T>(private val memberReader: MemberReader,
                                     private val provider: (String, JSElement) -> T) : MapAccessor<T>() {

      override fun build(declaration: JSObjectLiteralExpression): Map<String, T> {
        return memberReader.readMembers(declaration)
          .asSequence()
          .map {
            Pair(it.first, provider(it.first, it.second))
          }
          .distinctBy { it.first }
          .toMap(TreeMap())
      }

    }

    class BooleanValueAccessor(private val propertyName: String) : MemberAccessor<Boolean>() {
      override fun build(declaration: JSObjectLiteralExpression): Boolean {
        return declaration.findProperty(propertyName)
          ?.initializerOrStub?.castSafelyTo<JSLiteralExpression>()
          ?.significantValue == (JSTokenTypes.TRUE_KEYWORD as JSKeywordElementType).keyword
      }
    }

    open class MemberReader(private val propertyName: String,
                            private val canBeArray: Boolean = false,
                            private val canBeObject: Boolean = true) {
      fun readMembers(descriptor: JSObjectLiteralExpression): List<Pair<String, JSElement>> {
        val property = descriptor.findProperty(propertyName) ?: return emptyList()

        var propsObject = property.objectLiteralExpressionInitializer ?: getObjectLiteral(property)
        val initializerReference = JSPsiImplUtils.getInitializerReference(property)
        if (propsObject == null && initializerReference != null) {
          var resolved = JSStubBasedPsiTreeUtil.resolveLocally(initializerReference, property)
          if (resolved is ES6ImportedBinding && resolved.isNamespaceImport) {
            return processJSTypeMembers(JSResolveUtil.getElementJSType(resolved))
          }
          else if (resolved is ES6ImportExportDeclarationPart) {
            resolved = VueComponents.meaningfulExpression(resolved)
          }
          if (resolved is JSObjectLiteralExpression) {
            propsObject = resolved
          }
          else if (resolved != null) {
            propsObject = JSStubBasedPsiTreeUtil.findDescendants(resolved, JSStubElementTypes.OBJECT_LITERAL_EXPRESSION)
                            .find { it.context == resolved } ?: getObjectLiteralFromResolved(resolved)
            if ((propsObject == null && canBeArray) || !canBeObject) {
              return readPropsFromArray(resolved)
            }
          }
        }
        if (propsObject != null && canBeObject) {
          return collectPropertiesRecursively(propsObject)
        }
        return if (canBeArray) readPropsFromArray(property) else return emptyList()
      }

      private fun processJSTypeMembers(type: JSType?): List<Pair<String, JSElement>> {
        return type?.asRecordType()
                 ?.properties
                 ?.mapNotNull { prop ->
                   prop.takeIf { it.hasValidName() }
                     ?.memberSource
                     ?.singleElement
                     ?.castSafelyTo<JSElement>()
                     ?.let { Pair(prop.memberName, it) }
                 }
               ?: emptyList()
      }

      protected open fun getObjectLiteral(property: JSProperty): JSObjectLiteralExpression? = null
      protected open fun getObjectLiteralFromResolved(resolved: PsiElement): JSObjectLiteralExpression? = null

      private fun readPropsFromArray(holder: PsiElement): List<Pair<String, JSElement>> =
        getStringLiteralsFromInitializerArray(holder)
          .map { Pair(getTextIfLiteral(it) ?: "", it) }
    }

  }

}
