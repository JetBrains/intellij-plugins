// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.registry

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo
import com.intellij.javascript.nodejs.packageJson.NpmRegistryService
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.text.SemVer
import com.intellij.util.xmlb.annotations.XMap
import one.util.streamex.StreamEx
import org.jetbrains.vuejs.model.VuePlugin
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@State(name = "VueWebTypesRegistry")
class VueWebTypesRegistry : PersistentStateComponent<VueWebTypesRegistry.State> {

  companion object {

    const val PACKAGE_PREFIX = "@web-types"

    fun getInstance(): VueWebTypesRegistry {
      return ServiceManager.getService(VueWebTypesRegistry::class.java)
    }

  }

  private val STATE_UPDATE_INTERVAL = TimeUnit.MINUTES.toNanos(10)

  private var myStateLock = Object()
  private var myState = State()
  @Volatile
  private var myStateVersion = 0
  private var myStateTimestamp = 0L
  private var myStateUpdateFuture: Future<*>? = null

  override fun getState(): State {
    return myState
  }

  override fun loadState(state: State) {
    synchronized(myStateLock) {
      myState = state
      myStateVersion++
    }
  }

  fun getVuePlugins(module: Module): List<VuePlugin> {
    return processState { state, tracker ->
      CachedValuesManager.getManager(module.project).getCachedValue(module) {

        @Suppress("UNCHECKED_CAST")
        val result: List<VuePlugin> =
          StreamEx.of(FilenameIndex.getVirtualFilesByName(module.project, PackageJsonUtil.FILE_NAME,
                                                          GlobalSearchScope.moduleWithLibrariesScope(module)))
            .filter { JSLibraryUtil.isProbableLibraryFile(it) }
            .map { PackageJsonUtil.getOrCreateData(it) }
            .map { getVuePlugin(state, it) }
            .nonNull()
            .toList() as List<VuePlugin>

        CachedValueProvider.Result.create(result, tracker, VFS_STRUCTURE_MODIFICATIONS)
      }
    }
  }

  private fun getVuePlugin(state: State,
                           packageJson: PackageJsonData): VuePlugin? {
    packageJson.name ?: return null
    val versions = state.availableVersions[packageJson.name!!]
    if (versions == null || versions.isEmpty()) return null

    val pkgVersion = packageJson.version
    val webTypesVersion =
      if (pkgVersion == null)
        versions.last()
      else
        versions.findLast { it >= pkgVersion }
        ?: return null

    return loadPlugin(packageJson.name!!, webTypesVersion)
  }

  private fun loadPlugin(name: String, webTypesVersion: SemVer): VuePlugin? {
    return null
  }

  private fun <T> processState(processor: (State, ModificationTracker) -> T): T {
    val state: State
    val tracker: ModificationTracker
    updateStateIfNeeded()
    synchronized(myStateLock) {
      state = myState
      tracker = StateModificationTracker(myStateVersion)
    }
    return processor(state, tracker)
  }

  private fun updateStateIfNeeded() {
    var stateVersion: Int
    synchronized(myStateLock) {
      if (myStateTimestamp + STATE_UPDATE_INTERVAL >= System.nanoTime()) {
        return
      }
      stateVersion = myStateVersion
      if (myStateUpdateFuture == null) {
        myStateUpdateFuture = ApplicationManager.getApplication().executeOnPooledThread(Callable {
          val state = createNewState()
          synchronized(myStateLock) {
            myState = state
            myStateUpdateFuture = null
            myStateVersion++
            myStateTimestamp = System.nanoTime()
            myStateLock.notifyAll()
          }
        })
      }
    }

    synchronized(myStateLock) {
      if (stateVersion == myStateVersion) {
        ProgressManager.checkCanceled()
        myStateLock.wait(if (ApplicationManager.getApplication().isReadAccessAllowed) 50 else 100)
      }
    }
  }

  private fun createNewState(): State {
    val packageInfo: MutableList<NodePackageBasicInfo> = mutableListOf()
    NpmRegistryService.getInstance().findPackages(null,
                                                  NpmRegistryService.fullTextSearch(PACKAGE_PREFIX),
                                                  50,
                                                  { it.name.startsWith("$PACKAGE_PREFIX/") },
                                                  { packageInfo.add(it) })

    val availableVersions: TreeMap<String, List<SemVer>> = StreamEx.of(packageInfo)
      .parallel()
      .map { it.name }
      .mapToEntry {
        NpmRegistryService.getInstance()
          .getCachedOrFetchPackageVersionsFuture(it)
          .get()
      }
      .nonNullValues()
      .mapValues { it!!.versions }
      .toCustomMap { TreeMap() }
    return State(availableVersions)
  }

  class State(
    @XMap @JvmField
    var availableVersions: TreeMap<String, List<SemVer>> = TreeMap()
  )

  inner class StateModificationTracker(private val stateVersion: Int) : ModificationTracker {
    override fun getModificationCount(): Long {
      return if (stateVersion != myStateVersion) 1 else 0
    }
  }

}
