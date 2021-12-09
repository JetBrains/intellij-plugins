// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.lang.javascript.psi.JSElement
import org.jetbrains.vuejs.libraries.vuex.VuexUtils
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexContainerInfoProvider.VuexContainerInfo
import org.jetbrains.vuejs.model.source.EntityContainerInfoProvider.InitializedContainerInfoProvider

class VuexContainerInfoProvider : InitializedContainerInfoProvider<VuexContainerInfo>(
  VuexContainerInfoProvider::VuexContainerInfoImpl) {

  interface VuexContainerInfo {
    val state: Map<String, VuexStateProperty>
    val actions: Map<String, VuexAction>
    val mutations: Map<String, VuexMutation>
    val getters: Map<String, VuexGetter>

    val modules: Map<String, VuexModule>

    val isNamespaced: Boolean
  }

  companion object {
    val INSTANCE: VuexContainerInfoProvider = VuexContainerInfoProvider()

    private val ContainerMembers = object {
      val State: MemberReader = MemberReader(VuexUtils.STATE, canBeFunctionResult = true)
      val Actions: MemberReader = MemberReader(VuexUtils.ACTIONS)
      val Getters: MemberReader = MemberReader(VuexUtils.GETTERS)
      val Mutations: MemberReader = MemberReader(VuexUtils.MUTATIONS)
      val Modules: MemberReader = MemberReader(VuexUtils.MODULES)
    }

    private val STATE = SimpleMemberMapAccessor(ContainerMembers.State, ::VuexStatePropertyImpl)
    private val ACTIONS = SimpleMemberMapAccessor(ContainerMembers.Actions, ::VuexActionImpl)
    private val GETTERS = SimpleMemberMapAccessor(ContainerMembers.Getters, ::VuexGetterImpl)
    private val MUTATIONS = SimpleMemberMapAccessor(ContainerMembers.Mutations, ::VuexMutationImpl)
    private val MODULES = SimpleMemberMapAccessor(ContainerMembers.Modules, ::VuexModuleImpl)
    private val IS_NAMESPACED = BooleanValueAccessor(VuexUtils.PROP_NAMESPACED)
  }

  private class VuexContainerInfoImpl(declaration: JSElement)
    : InitializedContainerInfo(declaration), VuexContainerInfo {

    override val state: Map<String, VuexStateProperty> get() = get(STATE)
    override val actions: Map<String, VuexAction> get() = get(ACTIONS)
    override val mutations: Map<String, VuexMutation> get() = get(MUTATIONS)
    override val getters: Map<String, VuexGetter> get() = get(GETTERS)

    override val modules: Map<String, VuexModule> get() = get(MODULES)
    override val isNamespaced: Boolean get() = get(IS_NAMESPACED)
  }

}
