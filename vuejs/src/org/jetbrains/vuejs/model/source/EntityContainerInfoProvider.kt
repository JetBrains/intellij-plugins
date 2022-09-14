// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.ecmascript6.psi.ES6ImportExportDeclarationPart
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSBooleanLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSModuleTypeImpl
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.codeInsight.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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

  abstract class InitializedContainerInfoProvider<T>(val createInfo: (initializer: JSElement) -> T) : EntityContainerInfoProvider<T> {

    final override fun getInfo(descriptor: VueSourceEntityDescriptor): T? =
      descriptor.initializer?.let {
        val manager = CachedValuesManager.getManager(it.project)
        manager.getCachedValue(it, manager.getKeyForClass(this::class.java), {
          CachedValueProvider.Result.create(createInfo(it), PsiModificationTracker.MODIFICATION_COUNT)
        }, false)
      }

    protected abstract class InitializedContainerInfo(val declaration: JSElement) {
      private val values: ConcurrentHashMap<MemberAccessor<*>, Any?> = ConcurrentHashMap()

      protected fun <T> get(accessor: MemberAccessor<T>): T {
        @Suppress("UNCHECKED_CAST")
        return values.getOrPut(accessor) { accessor.build(declaration) } as T
      }
    }

    abstract class MemberAccessor<T> {
      abstract fun build(declaration: JSElement): T
    }

    abstract class ListAccessor<T> : MemberAccessor<List<T>>()

    abstract class MapAccessor<T> : MemberAccessor<Map<String, T>>()

    class SimpleMemberAccessor<T>(private val memberReader: MemberReader,
                                  private val provider: (String, JSElement) -> T)
      : ListAccessor<T>() {

      override fun build(declaration: JSElement): List<T> {
        return memberReader.readMembers(declaration).map { (name, element) -> provider(name, element) }
      }
    }


    class SimpleMemberMapAccessor<T>(private val memberReader: MemberReader,
                                     private val provider: (String, JSElement) -> T) : MapAccessor<T>() {

      override fun build(declaration: JSElement): Map<String, T> {
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
      override fun build(declaration: JSElement): Boolean =
        when (declaration) {
          is JSObjectLiteralExpression -> declaration
            .findProperty(propertyName)
            .let { getBooleanValue(it) }
          is JSFile -> JSModuleTypeImpl(declaration, true)
            .asRecordType()
            .findPropertySignature(propertyName)
            .let { getBooleanValue(it) }
          else -> false
        }

      private fun getBooleanValue(element: Any?): Boolean =
        element?.let { it as? JSTypeOwner }
          ?.jsType?.substitute()
          ?.let { it as? JSBooleanLiteralTypeImpl }
          ?.literal == true
    }

    open class MemberReader(private val propertyName: String,
                            private val canBeArray: Boolean = false,
                            private val canBeObject: Boolean = true,
                            private val canBeFunctionResult: Boolean = false) {
      fun readMembers(descriptor: JSElement): List<Pair<String, JSElement>> =
        when (descriptor) {
          is JSObjectLiteralExpression -> readObjectLiteral(descriptor)
          is JSFile -> readFileExports(descriptor)
          else -> emptyList()
        }

      private fun readObjectLiteral(descriptor: JSObjectLiteralExpression): List<Pair<String, JSElement>> {
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
                            .find { it.context == resolved } ?: getObjectLiteral(resolved)
            if ((propsObject == null && canBeArray) || !canBeObject) {
              return readPropsFromArray(resolved)
            }
          }
        }
        if (propsObject != null && canBeObject) {
          return collectMembers(propsObject)
        }
        return if (canBeArray) readPropsFromArray(property) else return emptyList()
      }

      private fun readFileExports(file: JSFile): List<Pair<String, JSElement>> =
        JSModuleTypeImpl(file, true).asRecordType()
          .findPropertySignature(propertyName)
          ?.memberSource
          ?.singleElement
          ?.let { exportedMember ->
            val objectLiteral = objectLiteralFor(exportedMember)
            if (canBeObject && objectLiteral != null)
              collectMembers(objectLiteral)
            else if (canBeArray)
              readPropsFromArray(exportedMember)
            else null
          } ?: emptyList()

      protected open fun getObjectLiteral(element: PsiElement): JSObjectLiteralExpression? =
        if (canBeFunctionResult) objectLiteralFor(element) else null

      private fun readPropsFromArray(holder: PsiElement): List<Pair<String, JSElement>> =
        getStringLiteralsFromInitializerArray(holder)
          .map { Pair(getTextIfLiteral(it) ?: "", it) }
    }

  }

}
