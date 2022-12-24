package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.externalSystem.importing.ProjectResolverPolicy
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioProjectResolvePolicyCleanCache
import com.jetbrains.cidr.cpp.embedded.platformio.project.ID
import com.jetbrains.cidr.external.system.actions.AbstractCidrExternalRefreshProjectAction

class PlatformioRefreshAction : AbstractCidrExternalRefreshProjectAction() {
  override fun getSystemId(e: AnActionEvent): ProjectSystemId = ID
  override fun getResolverPolicy(): ProjectResolverPolicy = PlatformioProjectResolvePolicyCleanCache
}

class PlatformioProjectResolvePolicy(val cleanCache: Boolean) : ProjectResolverPolicy {
  override fun isPartialDataResolveAllowed(): Boolean = false
}

