// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:JvmName("FlexModuleSdkSync")

package com.intellij.lang.javascript.flex.projectStructure.model.impl

import com.intellij.lang.javascript.flex.FlexModuleType
import com.intellij.lang.javascript.flex.projectStructure.entities.FlexBuildConfigurationSdksEntity
import com.intellij.lang.javascript.flex.projectStructure.entities.FlexBuildConfigurationSdksEntitySource
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.SdkId
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.concurrency.annotations.RequiresWriteLock

/**
 * The module SDK of a Flex module: the SDK of the [active] build configuration, or else the SDK of the first build configuration that
 * has one.
 *
 * Code insight needs the active build configuration only, see
 * [com.intellij.lang.javascript.flex.projectStructure.FlexOrderEnumerationHandler]. The fallback keeps a JDK entry while some build
 * configuration has an SDK, because the handler replaces the roots of that entry with the roots of a forced build configuration.
 */
internal fun expectedModuleSdk(configurations: List<FlexBuildConfiguration>, active: FlexBuildConfiguration?): Sdk? =
  active?.sdk ?: configurations.firstNotNullOfOrNull { it.sdk }

/** Sets the module SDK of a Flex [module] to [expectedModuleSdk] if it differs. */
@RequiresWriteLock
internal fun syncModuleSdk(module: Module) {
  if (!isFlexModule(module)) return
  val expected = expectedModuleSdk(module)
  if (ModuleRootManager.getInstance(module).sdk == expected) return
  ModuleRootModificationUtil.setModuleSdk(module, expected)
}

/** Runs [syncModuleSdk] for every module of the [project], then [syncBuildConfigurationSdks]. */
@RequiresWriteLock
internal fun syncAllModuleSdks(project: Project) {
  for (module in ModuleManager.getInstance(project).modules) {
    syncModuleSdk(module)
  }
  syncBuildConfigurationSdks(project)
}

/** Tells if [syncAllModuleSdks] changes the [project]. A caller uses it to skip the write action. */
@RequiresReadLock
internal fun isModuleSdkSyncNeeded(project: Project): Boolean {
  val modules = ModuleManager.getInstance(project).modules
  return modules.any { isFlexModule(it) && ModuleRootManager.getInstance(it).sdk != expectedModuleSdk(it) } ||
         !isBuildConfigurationSdksEntityUpToDate(project, buildConfigurationSdkIds(project))
}

/**
 * Makes the [FlexBuildConfigurationSdksEntity] list the SDKs of all build configurations of the Flex modules in the [project]. The entity
 * refers to the SDKs that only an inactive build configuration uses, so the file index registers their roots too.
 */
@RequiresWriteLock
internal fun syncBuildConfigurationSdks(project: Project) {
  val sdks = buildConfigurationSdkIds(project)
  if (isBuildConfigurationSdksEntityUpToDate(project, sdks)) return

  val builder = MutableEntityStorage.create()
  if (sdks.isNotEmpty()) {
    builder.addEntity(FlexBuildConfigurationSdksEntity(sdks, FlexBuildConfigurationSdksEntitySource))
  }
  WorkspaceModel.getInstance(project).updateProjectModel("Update Flex build configuration SDKs") { storage ->
    storage.replaceBySource({ it is FlexBuildConfigurationSdksEntitySource }, builder)
  }
}

private fun isFlexModule(module: Module): Boolean =
  !module.isDisposed && ModuleType.get(module) == FlexModuleType.getInstance()

private fun expectedModuleSdk(module: Module): Sdk? {
  val manager = FlexBuildConfigurationManager.getInstance(module)
  return expectedModuleSdk(manager.buildConfigurations.asList(), manager.activeConfiguration)
}

/** The IDs of the SDKs of all build configurations of the Flex modules in the [project]. */
private fun buildConfigurationSdkIds(project: Project): List<SdkId> =
  ModuleManager.getInstance(project).modules
    .filter { isFlexModule(it) }
    .flatMap { FlexBuildConfigurationManager.getInstance(it).buildConfigurations.asList() }
    .mapNotNull { it.sdk }
    .map { SdkId(it.name, it.sdkType.name) }
    .distinct()
    // A stable order, so an unchanged set of SDKs gives an equal entity and no update.
    .sortedWith(compareBy({ it.type }, { it.name }))

private fun isBuildConfigurationSdksEntityUpToDate(project: Project, sdks: List<SdkId>): Boolean {
  val current = WorkspaceModel.getInstance(project).currentSnapshot
    .entities(FlexBuildConfigurationSdksEntity::class.java).map { it.sdks }.toList()
  return current == listOfNotNull(sdks.ifEmpty { null })
}
