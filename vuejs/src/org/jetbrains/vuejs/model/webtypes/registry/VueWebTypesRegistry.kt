// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.registry

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo
import com.intellij.javascript.nodejs.packageJson.NpmRegistryService
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.library.JSLibraryUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.ParameterizedCachedValue
import com.intellij.psi.util.ParameterizedCachedValueProvider
import com.intellij.util.text.SemVer
import one.util.streamex.StreamEx
import org.jdom.Element
import org.jetbrains.vuejs.model.VuePlugin
import org.jetbrains.vuejs.model.source.VueSourcePlugin
import org.jetbrains.vuejs.model.webtypes.VueWebTypesPlugin
import org.jetbrains.vuejs.model.webtypes.json.WebTypes
import java.util.*
import java.util.Collections.emptySortedMap
import java.util.concurrent.*
import kotlin.collections.HashMap
import kotlin.math.max

@State(name = "VueWebTypesRegistry", storages = [Storage("web-types-registry.xml")])
class VueWebTypesRegistry : PersistentStateComponent<Element> {

  companion object {
    private val LOG = Logger.getInstance(VueWebTypesRegistry::class.java)
    const val PACKAGE_PREFIX = "@web-types"

    internal val STATE_UPDATE_INTERVAL = TimeUnit.MINUTES.toNanos(10)

    internal val EDT_TIMEOUT = TimeUnit.MILLISECONDS.toNanos(5)
    internal val EDT_RETRY_INTERVAL = TimeUnit.SECONDS.toNanos(1)

    internal val NON_EDT_TIMEOUT = TimeUnit.SECONDS.toNanos(10)
    internal val NON_EDT_RETRY_INTERVAL = TimeUnit.MINUTES.toNanos(1)

    private val PLUGINS_CACHE_KEY: Key<ParameterizedCachedValue<List<VuePlugin>, Pair<State, ModificationTracker>>> = Key(
      "vue.web-types.plugins")

    fun getInstance(): VueWebTypesRegistry {
      return ServiceManager.getService(VueWebTypesRegistry::class.java)
    }

  }

  private var myStateLock = Object()
  private var myState = State(emptySortedMap())
  @Volatile
  private var myStateVersion = 0
  private var myStateTimestamp = 0L
  private var myStateUpdate: FutureResultProvider<Boolean>? = null

  private var myPluginLoadMap = HashMap<String, FutureResultProvider<VuePlugin>>()
  private var myPluginCache = CacheBuilder.newBuilder()
    .maximumSize(20)
    .expireAfterAccess(30, TimeUnit.MINUTES)
    .build(CacheLoader.from(this::buildPlugin))

  override fun getState(): Element {
    synchronized(myState) {
      return myState.toElement()
    }
  }

  override fun loadState(stateElement: Element) {
    synchronized(myStateLock) {
      myState = State(stateElement)
      myStateVersion++
    }
  }

  fun getVuePlugins(module: Module): List<VuePlugin> {
    return processState { state, tracker ->
      CachedValuesManager.getManager(module.project)
        .getParameterizedCachedValue(module, PLUGINS_CACHE_KEY, ParameterizedCachedValueProvider { params ->
          @Suppress("UNCHECKED_CAST")
          val result: List<VuePlugin> =
            StreamEx.of(
              FilenameIndex.getVirtualFilesByName(module.project, PackageJsonUtil.FILE_NAME,
                                                  GlobalSearchScope.allScope(module.project)))
              .filter { JSLibraryUtil.isProbableLibraryFile(it) }
              .map { getWebTypesPlugin(params.first, it) ?: VueSourcePlugin.create(module, it) }
              .nonNull()
              .toList() as List<VuePlugin>

          CachedValueProvider.Result.create(result, params.second,
                                            NodeModulesDirectoryManager.getInstance(module.project).nodeModulesDirChangeTracker)
        }, false, Pair(state, tracker))
    }
  }

  private fun getWebTypesPlugin(state: State, packageJsonFile: VirtualFile): VuePlugin? {
    val packageJson = PackageJsonUtil.getOrCreateData(packageJsonFile)

    packageJson.name ?: return null

    // Check if there is plugin local web-types definition
    packageJson.webTypes?.let {
      packageJsonFile.parent?.findFileByRelativePath(it)
    }?.inputStream?.let {
      return VueWebTypesPlugin(ObjectMapper().readValue(it, WebTypes::class.java), null)
    }

    val versions = state.availableVersions["@web-types/" + packageJson.name!!]
    if (versions == null || versions.isEmpty()) return null

    val pkgVersion = packageJson.version

    val webTypesVersionEntry = versions.entries.find {
      pkgVersion == null || it.key <= pkgVersion
    } ?: return null

    return loadWebTypesPlugin(webTypesVersionEntry.value)
  }

  private fun loadWebTypesPlugin(tarballUrl: String): VuePlugin? {
    synchronized(myPluginLoadMap) {
      myPluginCache.getIfPresent(tarballUrl)?.let { return it }
      myPluginLoadMap.computeIfAbsent(tarballUrl) {
        FutureResultProvider(Callable {
          myPluginCache.get(tarballUrl)
        })
      }
    }.result
      ?.let {
        synchronized(myPluginLoadMap) {
          myPluginLoadMap.remove(tarballUrl)
        }
        return it
      }
    ?: return null
  }

  private fun buildPlugin(tarballUrl: String?): VuePlugin? {
    tarballUrl ?: return null
    val webTypesJson = ObjectMapper().readValue(VueWebTypesJsonsCache.getWebTypesJson(tarballUrl), WebTypes::class.java)
    synchronized(myStateLock) {
      myStateVersion++
    }
    return VueWebTypesPlugin(webTypesJson, null)
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
    val stateUpdate: FutureResultProvider<Boolean>
    synchronized(myStateLock) {
      if (myStateTimestamp + STATE_UPDATE_INTERVAL >= System.nanoTime()
          // Use #loadState() in test mode
          || ApplicationManager.getApplication().isUnitTestMode) {
        return
      }
      if (myStateUpdate == null) {
        myStateUpdate = FutureResultProvider(Callable {
          val state = createNewState()
          synchronized(myStateLock) {
            myState = state
            myStateVersion++
            myStateTimestamp = System.nanoTime()
          }
          true
        })
      }
      stateUpdate = myStateUpdate!!
    }

    stateUpdate.result?.let {
      synchronized(myStateLock) {
        if (myStateUpdate == stateUpdate) {
          myStateUpdate = null
        }
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

    val availableVersions: SortedMap<String, SortedMap<SemVer, String>> = StreamEx.of(packageInfo)
      .parallel()
      .map { it.name }
      .mapToEntry {
        NpmRegistryService.getInstance()
          .getCachedOrFetchPackageVersionsFuture(it)
          .get()
      }
      .nonNullValues()
      .mapValues { it!!.versionUrlMap }
      .toSortedMap()
    return State(availableVersions)
  }

  private class State {

    val availableVersions: SortedMap<String, SortedMap<SemVer, String>>

    constructor(availableVersions: SortedMap<String, SortedMap<SemVer, String>>) {
      this.availableVersions = availableVersions
    }

    constructor(root: Element) {
      availableVersions = TreeMap()

      for (versions in root.getChildren(PACKAGE_ELEMENT)) {
        val name = versions.getAttributeValue(NAME_ATTR) ?: continue
        val map = availableVersions.computeIfAbsent(name, { TreeMap(Comparator.reverseOrder<SemVer>()) })
        for (version in versions.getChildren(VERSION_ELEMENT)) {
          val ver = version.getAttributeValue(VALUE_ATTR)?.let { SemVer.parseFromText(it) } ?: continue
          val url = version.getAttributeValue(URL_ATTR) ?: continue
          map[ver] = url
        }
      }
    }

    fun toElement(): Element {
      val root = Element(WEB_TYPES_ELEMENT)
      for (entry in availableVersions) {
        val versionsEl = Element(PACKAGE_ELEMENT)
        versionsEl.setAttribute(NAME_ATTR, entry.key)
        for (versionEntry in entry.value) {
          val versionEl = Element(VERSION_ELEMENT)
          versionEl.setAttribute(VALUE_ATTR, versionEntry.key.rawVersion)
          versionEl.setAttribute(URL_ATTR, versionEntry.value)
          versionsEl.addContent(versionEl)
        }
        root.addContent(versionsEl)
      }
      return root
    }

    companion object {
      const val WEB_TYPES_ELEMENT = "web-types"
      const val PACKAGE_ELEMENT = "package"
      const val VERSION_ELEMENT = "version"

      const val NAME_ATTR = "name"
      const val VALUE_ATTR = "value"
      const val URL_ATTR = "url"
    }
  }

  inner class StateModificationTracker(private val stateVersion: Int) : ModificationTracker {
    override fun getModificationCount(): Long {
      return if (stateVersion != myStateVersion) 1 else 0
    }
  }

  class FutureResultProvider<T>(private val operation: Callable<T>) {
    private var myLock = Object()
    private var myFuture: Future<T>? = null
    private var myRetryTime = 0L

    val result: T?
      get() {
        val future: Future<T>
        synchronized(myLock) {
          if (myRetryTime > System.nanoTime()) {
            return null
          }
          if (myFuture == null) {
            myFuture = ApplicationManager.getApplication().executeOnPooledThread(operation)
          }
          future = myFuture!!
        }
        val app = ApplicationManager.getApplication()
        val edt = app.isReadAccessAllowed && !app.isUnitTestMode
        try {
          ProgressManager.checkCanceled()
          return future.get(if (edt) EDT_TIMEOUT else NON_EDT_TIMEOUT, TimeUnit.NANOSECONDS)
        }
        catch (e: TimeoutException) {
          synchronized(myLock) {
            myRetryTime = max(myRetryTime, System.nanoTime() + (if (edt) EDT_RETRY_INTERVAL else NON_EDT_RETRY_INTERVAL))
          }
        }
        catch (e: ExecutionException) {
          LOG.warn(e)
          synchronized(myLock) {
            myRetryTime = max(myRetryTime, System.nanoTime() + NON_EDT_RETRY_INTERVAL)
            myFuture = null
          }
        }
        return null
      }
  }

}
