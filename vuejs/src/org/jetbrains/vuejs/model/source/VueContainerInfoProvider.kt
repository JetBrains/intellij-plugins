// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.model.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

interface VueContainerInfoProvider {

  fun getInfo(initializer: JSObjectLiteralExpression?, clazz: JSClass?): VueContainerInfo?

  interface VueContainerInfo {
    val components: Map<String, VueComponent> get() = emptyMap()
    val directives: Map<String, VueDirective> get() = emptyMap()
    val filters: Map<String, VueFilter> get() = emptyMap()
    val mixins: List<VueMixin> get() = emptyList()
    val extends: List<VueMixin> get() = emptyList()

    val data: List<VueDataProperty> get() = emptyList()
    val props: List<VueInputProperty> get() = emptyList()
    val computed: List<VueComputedProperty> get() = emptyList()
    val methods: List<VueMethod> get() = emptyList()
    val emits: List<VueEmitCall> get() = emptyList()
    val slots: List<VueSlot> get() = emptyList()

    val model: VueModelDirectiveProperties? get() = null
    val delimiters: Pair<String, String>? get() = null
  }

  companion object {
    private val EP_NAME = ExtensionPointName.create<VueContainerInfoProvider>("com.intellij.vuejs.containerInfoProvider")

    fun getProviders(): List<VueContainerInfoProvider> = EP_NAME.extensionList
  }


  abstract class VueDecoratedContainerInfoProvider(val createInfo: (clazz: JSClass) -> VueContainerInfo) : VueContainerInfoProvider {
    final override fun getInfo(initializer: JSObjectLiteralExpression?, clazz: JSClass?): VueContainerInfo? =
      clazz?.let {
        val manager = CachedValuesManager.getManager(it.project)
        manager.getCachedValue(it, manager.getKeyForClass<VueContainerInfo>(this::class.java), {
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

  abstract class VueInitializedContainerInfoProvider(val createInfo: (initializer: JSObjectLiteralExpression) -> VueContainerInfo) : VueContainerInfoProvider {

    final override fun getInfo(initializer: JSObjectLiteralExpression?, clazz: JSClass?): VueContainerInfo? =
      initializer?.let {
        val manager = CachedValuesManager.getManager(it.project)
        manager.getCachedValue(it, manager.getKeyForClass<VueContainerInfo>(this::class.java), {
          CachedValueProvider.Result.create(createInfo(it), PsiModificationTracker.MODIFICATION_COUNT)
        }, false)
      }

    protected abstract class VueInitializedContainerInfo(val declaration: JSObjectLiteralExpression) : VueContainerInfo {
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

  }


}
