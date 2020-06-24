// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes.registry

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.intellij.javaee.ExternalResourceManager
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.npm.registry.NpmRegistryService
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.util.io.HttpRequests
import com.intellij.util.text.SemVer
import one.util.streamex.EntryStream
import one.util.streamex.StreamEx
import org.jdom.Element
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueGlobal
import org.jetbrains.vuejs.model.VuePlugin
import org.jetbrains.vuejs.model.webtypes.VueWebTypesGlobal
import org.jetbrains.vuejs.model.webtypes.VueWebTypesPlugin
import org.jetbrains.vuejs.model.webtypes.json.WebTypes
import java.io.IOException
import java.util.*
import java.util.Collections.emptySortedMap
import java.util.concurrent.*
import kotlin.collections.HashMap
import kotlin.math.max

@State(name = "VueWebTypesRegistry", storages = [Storage("web-types-registry.xml")])
class VueWebTypesRegistry : PersistentStateComponent<Element> {

  companion object {
    private val LOG = Logger.getInstance(VueWebTypesRegistry::class.java)
    private const val WEB_TYPES_ENABLED_PACKAGES_URL = "https://raw.githubusercontent.com/JetBrains/web-types/master/packages/registry.json"
    private val LETTERS_PATTERN = Regex("[a-zA-Z]")
    private val NON_LETTERS_PATTERN = Regex("^[^a-zA-Z]+\$")
    private val WEB_TYPES_PKG_NAME_REPLACE_PATTERN = Regex("^@(.*)/(.*)$")

    const val PACKAGE_PREFIX = "@web-types"
    const val WEB_TYPES_FILE_SUFFIX = ".web-types.json"

    internal val STATE_UPDATE_INTERVAL = TimeUnit.MINUTES.toNanos(10)

    internal val CHECK_TIMEOUT = TimeUnit.MILLISECONDS.toNanos(1)

    internal val EDT_TIMEOUT = TimeUnit.MILLISECONDS.toNanos(5)
    internal val EDT_RETRY_INTERVAL = TimeUnit.SECONDS.toNanos(1)

    internal val NON_EDT_TIMEOUT = TimeUnit.SECONDS.toNanos(10)
    internal val NON_EDT_RETRY_INTERVAL = TimeUnit.MINUTES.toNanos(1)

    val MODIFICATION_TRACKER = ModificationTracker { instance.myStateTimestamp }

    val instance: VueWebTypesRegistry get() = ServiceManager.getService(VueWebTypesRegistry::class.java)

    fun createWebTypesGlobal(project: Project, packageJsonFile: VirtualFile, owner: VueGlobal): Result<VueGlobal>? =
      loadWebTypes(packageJsonFile)?.let { (webTypes, file) ->
        Result.create(VueWebTypesGlobal(project, packageJsonFile, webTypes, owner), packageJsonFile, file)
      }

    fun createWebTypesPlugin(project: Project, packageJsonFile: VirtualFile, owner: VuePlugin): Result<VuePlugin?> {
      val data = PackageJsonUtil.getOrCreateData(packageJsonFile)
      loadWebTypes(packageJsonFile, data)?.let { (webTypes, file) ->
        return Result.create(VueWebTypesPlugin(project, packageJsonFile, webTypes, owner),
                             packageJsonFile, file, MODIFICATION_TRACKER)
      }
      return instance.getWebTypesPlugin(project, packageJsonFile, data, owner)
    }

    fun createWebTypesPlugin(project: Project,
                             packageName: String,
                             packageVersion: String?,
                             owner: VueEntitiesContainer): Result<VuePlugin?> {
      return instance.getWebTypesPlugin(project, packageName, SemVer.parseFromText(packageVersion), null, owner)
    }

    private fun loadWebTypes(packageJsonFile: VirtualFile,
                             packageJson: PackageJsonData = PackageJsonUtil.getOrCreateData(packageJsonFile))
      : Pair<WebTypes, VirtualFile>? {
      val webTypesFile = packageJson.webTypes?.let {
        packageJsonFile.parent?.findFileByRelativePath(it)
      }
      return webTypesFile?.inputStream?.let {
        createObjectMapper().readValue(it, WebTypes::class.java)
      }
        ?.takeIf { it.framework == WebTypes.Framework.VUE }
        ?.let { Pair(it, webTypesFile) }
    }

    private fun createObjectMapper() = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  }

  private var myStateLock = Object()
  private var myState = State(emptySortedMap(), emptySet())

  @Volatile
  private var myStateVersion = 0
  private var myStateTimestamp = 0L
  private var myStateUpdate: FutureResultProvider<Boolean>? = null

  private var myPluginLoadMap = HashMap<String, FutureResultProvider<WebTypes>>()
  private var myPluginCache = CacheBuilder.newBuilder()
    .maximumSize(20)
    .expireAfterAccess(30, TimeUnit.MINUTES)
    .build(CacheLoader.from(this::buildPackageWebTypes))

  private val bundledWebTypes: Map<String, SortedMap<SemVer, String>> by lazy {
    val result: MutableMap<String, SortedMap<SemVer, String>> = mutableMapOf()
    JSLanguageServiceUtil.getPluginDirectory(VueWebTypesRegistry::class.java, "web-types")
      ?.listFiles { _, name -> name.endsWith(".json") }
      ?.asSequence()
      ?.filter { it.name.contains('@') }
      ?.forEach { file ->
        val packageName = file.name.takeWhile { it != '@' }
        val versionStr = file.name.substringAfter('@')
                           .removeSuffix(WEB_TYPES_FILE_SUFFIX) + "-1"
        SemVer.parseFromText(versionStr)
          ?.let { version ->
            result.computeIfAbsent(packageName) { TreeMap() }[version] = file.toURI().toString()
          }
      }
    result
  }

  override fun getState(): Element {
    synchronized(myState) {
      return myState.toElement()
    }
  }

  override fun loadState(stateElement: Element) {
    synchronized(myStateLock) {
      myState = State(stateElement)
      incStateVersion()
    }
  }

  val webTypesEnabledPackages: Result<Set<String>>
    get() = processState { state, tracker ->
      Result.create(state.enabledPackages, tracker)
    }

  private fun getWebTypesPlugin(project: Project,
                                packageJsonFile: VirtualFile,
                                packageJson: PackageJsonData,
                                owner: VuePlugin): Result<VuePlugin?> {
    return packageJson.name?.let { getWebTypesPlugin(project, it, packageJson.version, packageJsonFile, owner) }
           ?: Result.create(null as VuePlugin?, packageJsonFile)
  }

  private fun getWebTypesPlugin(project: Project,
                                packageName: String,
                                packageVersion: SemVer?,
                                packageJsonFile: VirtualFile?,
                                owner: VueEntitiesContainer): Result<VuePlugin?> {
    return processState { state, tracker ->
      val webTypesPackageName = normalizePackageName(packageName)
      val webTypes = getWebTypesUrl(state.availableVersions["@web-types/$webTypesPackageName"], packageVersion)
                       ?.let { loadPackageWebTypes(it) }
                     ?: getWebTypesUrl(bundledWebTypes[webTypesPackageName], packageVersion)
                       ?.let { loadPackageWebTypes(it) }
      return@processState Result.create(webTypes?.let { VueWebTypesPlugin(project, packageJsonFile, it, owner) }, tracker)
    }
  }

  private fun normalizePackageName(packageName: String?): String? =
    packageName?.replace(WEB_TYPES_PKG_NAME_REPLACE_PATTERN, "at-$1-$2")

  private fun getWebTypesUrl(versions: SortedMap<SemVer, String>?,
                             pkgVersion: SemVer?): String? {
    if (versions == null || versions.isEmpty()) {
      return null
    }
    var webTypesVersionEntry = (if (pkgVersion == null)
      versions.entries.findLast { it.key.preRelease == null }
    else
      versions.entries.find { it.key <= pkgVersion })
                               ?: return null

    if (webTypesVersionEntry.key.preRelease?.contains(LETTERS_PATTERN) == true) {
      // `2.0.0-beta.1` version is higher than `2.0.0-1`, so we need to manually find if there
      // is a non-alpha/beta/rc version available in such a case.
      versions.entries.find {
        it.key.major == webTypesVersionEntry.key.major
        && it.key.minor == webTypesVersionEntry.key.minor
        && it.key.patch == webTypesVersionEntry.key.patch
        && it.key.preRelease?.contains(NON_LETTERS_PATTERN) == true
      }
        ?.let { webTypesVersionEntry = it }
    }
    return webTypesVersionEntry.value
  }

  private fun loadPackageWebTypes(fileUrl: String): WebTypes? {
    synchronized(myPluginLoadMap) {
      myPluginCache.getIfPresent(fileUrl)?.let { return it }
      myPluginLoadMap.computeIfAbsent(fileUrl) {
        FutureResultProvider(Callable {
          myPluginCache.get(fileUrl)
        })
      }
    }.result
      ?.let {
        synchronized(myPluginLoadMap) {
          myPluginLoadMap.remove(fileUrl)
        }
        return it
      }
    ?: return null
  }

  private fun buildPackageWebTypes(fileUrl: String?): WebTypes? {
    val webTypesJson = createObjectMapper().readValue(
      VueWebTypesJsonsCache.getWebTypesJson(fileUrl ?: return null), WebTypes::class.java)
    if (webTypesJson.framework != WebTypes.Framework.VUE) {
      return null
    }
    incStateVersion()
    return webTypesJson
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
            incStateVersion()
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
    val enabledPackagesFuture = ApplicationManager.getApplication().executeOnPooledThread(Callable {
      try {
        HttpRequests.request(WEB_TYPES_ENABLED_PACKAGES_URL)
          .productNameAsUserAgent()
          .gzip(true)
          .readString(null)
      }
      catch (e: Exception) {
        e
      }
    })
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
      .mapValues {
        EntryStream.of(it!!.versionsInfo)
          .filter { (_, info) -> !info.isDeprecated && info.url != null }
          .mapValues { info -> info.url }
          .into(TreeMap<SemVer, String>(Comparator.reverseOrder()) as SortedMap<SemVer, String>)
      }
      .toSortedMap()
    val enabledPackages = enabledPackagesFuture.get()
      ?.let {
        when (it) {
          is Exception -> throw it
          is String -> ObjectMapper().readTree(it) as? ObjectNode
          else -> null
        }
      }
      ?.get("vue")
      ?.let { it as? ArrayNode }
      ?.elements()
      ?.asSequence()
      ?.mapNotNull { (it as? TextNode)?.asText() }
      ?.toSet()
    return State(availableVersions, enabledPackages ?: emptySet())
  }

  private fun incStateVersion() {
    synchronized(myStateLock) {
      myStateVersion++
      // Inform that external resource has changed to reload XmlElement/AttributeDescriptors
      ExternalResourceManager.getInstance().incModificationCount()
    }
  }

  private class State {

    val availableVersions: SortedMap<String, SortedMap<SemVer, String>>
    val enabledPackages: Set<String>

    constructor(availableVersions: SortedMap<String, SortedMap<SemVer, String>>, enabledPackages: Set<String>) {
      this.availableVersions = availableVersions
      this.enabledPackages = enabledPackages
    }

    constructor(root: Element) {
      availableVersions = TreeMap()
      enabledPackages = mutableSetOf()

      for (versions in root.getChildren(PACKAGE_ELEMENT)) {
        val name = versions.getAttributeValue(NAME_ATTR) ?: continue
        val map = availableVersions.computeIfAbsent(name) { TreeMap(Comparator.reverseOrder<SemVer>()) }
        for (version in versions.getChildren(VERSION_ELEMENT)) {
          val ver = version.getAttributeValue(VALUE_ATTR)?.let { SemVer.parseFromText(it) } ?: continue
          val url = version.getAttributeValue(URL_ATTR) ?: continue
          map[ver] = url
        }
      }
      for (enabled in root.getChild("enabled")?.getChildren(PACKAGE_ELEMENT) ?: emptyList()) {
        enabled.getAttributeValue(NAME_ATTR)?.let { enabledPackages.add(it) }
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

  private inner class StateModificationTracker(private val stateVersion: Int) : ModificationTracker {
    override fun getModificationCount(): Long {
      return if (stateVersion != myStateVersion) -1 else 0
    }
  }

  private class FutureResultProvider<T>(private val operation: Callable<T>) {
    private val myLock = Object()
    private var myFuture: Future<*>? = null
    private var myRetryTime = 0L

    val result: T?
      get() {
        val future: Future<*>
        synchronized(myLock) {
          if (myRetryTime > System.nanoTime()) {
            return null
          }
          if (myFuture == null) {
            myFuture = ApplicationManager.getApplication().executeOnPooledThread(Callable {
              try {
                operation.call()
              }
              catch (e: Exception) {
                // we need to catch the exception and process on the main thread
                e
              }
            })
          }
          future = myFuture!!
        }
        val app = ApplicationManager.getApplication()
        val edt = app.isDispatchThread && !app.isUnitTestMode
        var timeout = if (edt) EDT_TIMEOUT else NON_EDT_TIMEOUT
        try {
          do {
            ProgressManager.checkCanceled()
            try {
              when (val result = future.get(CHECK_TIMEOUT, TimeUnit.NANOSECONDS)) {
                is ProcessCanceledException -> {
                  // retry at the next occasion without waiting
                  synchronized(myLock) {
                    myFuture = null
                  }
                  return null
                }
                is ExecutionException -> throw result
                // wrap it and pass to the exception catch block below
                is Exception -> throw ExecutionException(result)
                else -> {
                  // future returns either T or Exception, so result must be T at this point
                  @Suppress("UNCHECKED_CAST")
                  return result as T
                }
              }
            }
            catch (e: TimeoutException) {
              timeout -= CHECK_TIMEOUT
            }
          }
          while (timeout > 0)
          synchronized(myLock) {
            myRetryTime = max(myRetryTime, System.nanoTime() + (if (edt) EDT_RETRY_INTERVAL else NON_EDT_RETRY_INTERVAL))
          }
        }
        catch (e: ExecutionException) {
          // Do not log IOExceptions as errors, since they can appear because of HTTP communication
          if (e.cause is IOException) {
            LOG.warn(e.message)
          }
          else {
            LOG.error(e)
          }
          synchronized(myLock) {
            myRetryTime = max(myRetryTime, System.nanoTime() + NON_EDT_RETRY_INTERVAL)
            myFuture = null
          }
        }
        return null
      }
  }
}
