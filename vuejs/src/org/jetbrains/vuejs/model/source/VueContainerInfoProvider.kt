// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.containers.MultiMap
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.EntityContainerInfoProvider.DecoratedContainerInfoProvider
import org.jetbrains.vuejs.model.source.EntityContainerInfoProvider.InitializedContainerInfoProvider
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

interface VueContainerInfoProvider : EntityContainerInfoProvider<VueContainerInfo> {

  fun getAdditionalComponents(scope: GlobalSearchScope, sourceComponents: ComponentsInfo): ComponentsInfo? = null

  fun getThisTypeProperties(instanceOwner: VueInstanceOwner,
                            standardProperties: MutableMap<String, PropertySignature>)
    : Collection<PropertySignature> = emptyList()

  data class ComponentsInfo(val local: MultiMap<String, VueComponent>, val global: MultiMap<String, VueComponent>) {
    fun get(local: Boolean): MultiMap<String, VueComponent> = if (local) this.local else global
  }

  @JvmDefaultWithCompatibility
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

    val model: VueModelDirectiveProperties? get() = null
    val template: VueTemplate<*>? get() = null
    val delimiters: Pair<String, String>? get() = null
  }

  companion object {
    private val EP_NAME = ExtensionPointName.create<VueContainerInfoProvider>("com.intellij.vuejs.containerInfoProvider")

    fun getProviders(): List<VueContainerInfoProvider> = EP_NAME.extensionList
  }


  abstract class VueDecoratedContainerInfoProvider(createInfo: (clazz: JSClass) -> VueContainerInfo)
    : DecoratedContainerInfoProvider<VueContainerInfo>(createInfo), VueContainerInfoProvider

  abstract class VueInitializedContainerInfoProvider(createInfo: (initializer: JSElement) -> VueContainerInfo)
    : InitializedContainerInfoProvider<VueContainerInfo>(createInfo), VueContainerInfoProvider {

    protected abstract class VueInitializedContainerInfo(declaration: JSElement)
      : InitializedContainerInfo(declaration), VueContainerInfo

  }
}
