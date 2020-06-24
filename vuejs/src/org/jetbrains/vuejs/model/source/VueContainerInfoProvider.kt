// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.EntityContainerInfoProvider.DecoratedContainerInfoProvider
import org.jetbrains.vuejs.model.source.EntityContainerInfoProvider.InitializedContainerInfoProvider
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo

interface VueContainerInfoProvider : EntityContainerInfoProvider<VueContainerInfo> {

  @JvmDefault
  override fun getInfo(descriptor: VueSourceEntityDescriptor): VueContainerInfo?

  @JvmDefault
  fun getThisTypeProperties(instanceOwner: VueInstanceOwner,
                            standardProperties: MutableMap<String, PropertySignature>)
    : Collection<PropertySignature> = emptyList()

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

  abstract class VueInitializedContainerInfoProvider(createInfo: (initializer: JSObjectLiteralExpression) -> VueContainerInfo)
    : InitializedContainerInfoProvider<VueContainerInfo>(createInfo), VueContainerInfoProvider {

    protected abstract class VueInitializedContainerInfo(declaration: JSObjectLiteralExpression)
      : InitializedContainerInfo(declaration), VueContainerInfo

  }
}
