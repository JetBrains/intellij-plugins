// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.intellij.javascript.nodejs.library.node_modules.NodeModulesDirectoryManager
import com.intellij.javascript.nodejs.npm.registry.NpmRegistryService
import com.intellij.javascript.nodejs.packageJson.InstalledPackageVersion
import com.intellij.javascript.nodejs.packageJson.NodePackageBasicInfo
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.*
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.HttpRequests
import org.jetbrains.annotations.NonNls
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.*
import java.util.function.Supplier
import kotlin.Pair

class AngularCliSchematicsRegistryServiceImpl : AngularCliSchematicsRegistryService() {

  private val myNgAddPackages = CachedValue { fetchPackagesSupportingNgAdd() }
  private val myLocalNgAddPackages = ConcurrentHashMap<String, Pair<Boolean, Long>>()
  private val myNgAddSupportedCache = ConcurrentHashMap<String, CachedValue<Boolean>>()

  override fun getPackagesSupportingNgAdd(timeout: Long): List<NodePackageBasicInfo> {
    return myNgAddPackages.getValue(timeout) ?: emptyList()
  }

  override fun supportsNgAdd(packageName: String, timeout: Long): Boolean {
    return getPackagesSupportingNgAdd(timeout).any { pkg -> packageName == pkg.name }
  }

  override fun supportsNgAdd(packageName: String,
                             versionOrRange: String,
                             timeout: Long): Boolean {
    return supportsNgAdd(packageName, timeout)
           && myNgAddSupportedCache
             .computeIfAbsent(getKey(packageName, versionOrRange)) { _ ->
               CachedValue { checkForNgAddSupport(packageName, versionOrRange) }
             }.getValue(timeout) == true
  }

  override fun supportsNgAdd(version: InstalledPackageVersion): Boolean {
    try {
      val packageJson = version.packageJson
      if (packageJson != null) {
        return myLocalNgAddPackages.compute(packageJson.path) { _, curValue ->
          if (curValue != null && packageJson.modificationStamp == curValue.second) {
            return@compute curValue
          }
          try {
            val schematicsCollection = getSchematicsCollection(File(packageJson.path))
            return@compute Pair(schematicsCollection != null && hasNgAddSchematic(schematicsCollection), packageJson.modificationStamp)
          }
          catch (e: IOException) {
            return@compute Pair(false, packageJson.modificationStamp)
          }
        }!!.first
      }
    }
    catch (e: Exception) {
      LOG.info("Failed to retrieve schematics info for " + version.packageDir.name, e)
    }

    return false
  }

  override fun getSchematics(project: Project,
                             cliFolder: VirtualFile,
                             includeHidden: Boolean,
                             logErrors: Boolean): List<Schematic> =
    AngularCliUtil.findCliJson(cliFolder)
      ?.let { angularJson ->
        ReadAction.compute<PsiFile, RuntimeException> {
          PsiManager.getInstance(project).findFile(angularJson)
        }
      }
      ?.let { angularJson ->
        getCachedSchematics(angularJson, if (includeHidden) SCHEMATICS_ALL else SCHEMATICS_PUBLIC).getUpToDateOrCompute {
          CachedValueProvider.Result.create(
            doLoad(angularJson.project,
                   angularJson.virtualFile.parent, includeHidden, logErrors),
            NodeModulesDirectoryManager.getInstance(angularJson.project).nodeModulesDirChangeTracker,
            SCHEMATICS_CACHE_TRACKER,
            angularJson)
        }
      }
    ?: emptyList()

  override fun clearProjectSchematicsCache() {
    SCHEMATICS_CACHE_TRACKER.incModificationCount()
  }

  private class CachedSchematics {
    private var mySchematics: List<Schematic>? = null
    private var myTrackers: List<Pair<Any, Long>>? = null

    @Synchronized
    fun getUpToDateOrCompute(provider: Supplier<CachedValueProvider.Result<List<Schematic>>>): List<Schematic>? {
      if (mySchematics != null
          && myTrackers?.all { pair -> pair.second >= 0 && getTimestamp(pair.first) == pair.second } == true) {
        return mySchematics
      }
      val schematics = provider.get()
      mySchematics = Collections.unmodifiableList(schematics.value)
      myTrackers = schematics.dependencyItems.map { obj -> Pair(obj, getTimestamp(obj)) }
      return mySchematics
    }

    private fun getTimestamp(dependency: Any): Long {
      if (dependency is ModificationTracker) {
        return dependency.modificationCount
      }
      if (dependency is PsiElement) {
        if (!dependency.isValid) return -1
        val containingFile = dependency.containingFile
        return containingFile?.virtualFile?.modificationStamp ?: -1
      }
      throw UnsupportedOperationException(dependency.javaClass.toString())
    }
  }

  private class CachedValue<T>(private val myValueSupplier: Callable<out T>) {

    private var myUpdateTime: Long = 0
    private var myCacheComputation: Future<out T>? = null
    private var myCachedValue: T? = null

    private val isCacheExpired: Boolean
      @Synchronized get() = myUpdateTime + CACHE_EXPIRY <= System.currentTimeMillis()

    fun getValue(timeout: Long): T? {
      val cacheComputation: Future<out T>
      synchronized(this) {
        if (myCachedValue != null && !isCacheExpired) {
          return myCachedValue
        }
        if (myCacheComputation == null) {
          myCachedValue = null
          myCacheComputation = ourExecutorService.submit(myValueSupplier)
        }
        cacheComputation = myCacheComputation!!
      }
      var result: T? = JSLanguageServiceUtil.awaitFuture(cacheComputation, timeout, 10, null, false, null)
      synchronized(this) {
        if (myCacheComputation != null && myCacheComputation!!.isDone) {
          try {
            myCachedValue = myCacheComputation!!.get()
            result = myCachedValue
          }
          catch (_: InterruptedException) {
          }
          catch (_: CancellationException) {
          }
          catch (e: ExecutionException) {
            LOG.error(e)
          }

          myCacheComputation = null
          myUpdateTime = System.currentTimeMillis()
        }
      }
      return result
    }
  }

  companion object {

    @NonNls
    private val USER_AGENT = "JetBrains IDE"

    @NonNls
    private val NG_PACKAGES_URL = "https://raw.githubusercontent.com/JetBrains/intellij-plugins/master/AngularJS/resources/org/angularjs/cli/ng-packages.json"

    @NonNls
    private val LOG = Logger.getInstance(AngularCliSchematicsRegistryServiceImpl::class.java)
    private const val CACHE_EXPIRY = 25 * 60 * 1000 //25 mins

    @NonNls
    private val ourExecutorService = AppExecutorUtil.createBoundedApplicationPoolExecutor("Angular CLI Schematics Registry Pool", 5)

    @NonNls
    private val SCHEMATICS_PUBLIC = Key<CachedSchematics>("angular.cli.schematics.public")

    @NonNls
    private val SCHEMATICS_ALL = Key<CachedSchematics>("angular.cli.schematics.all")
    private val SCHEMATICS_CACHE_TRACKER = SimpleModificationTracker()

    @NonNls
    private val NG_PACKAGES_JSON_PATH = "../../angularjs/cli/ng-packages.json"

    @NonNls
    private val SCHEMATICS_PROP = "schematics"

    @NonNls
    private val NG_ADD_SCHEMATIC = "ng-add"

    private fun fetchPackagesSupportingNgAdd(): List<NodePackageBasicInfo> {
      try {
        val builder = HttpRequests.request(NG_PACKAGES_URL)
        builder.userAgent(USER_AGENT)
        builder.gzip(true)
        return readNgAddPackages(builder.readString(null))
      }
      catch (e: IOException) {
        LOG.info("Failed to load current list of ng-add compatible packages.", e)
        try {
          AngularCliSchematicsRegistryServiceImpl::class.java.getResourceAsStream(NG_PACKAGES_JSON_PATH)!!.use { `is` ->
            return readNgAddPackages(FileUtil.loadTextAndClose(InputStreamReader(`is`, StandardCharsets.UTF_8)))
          }
        }
        catch (e1: Exception) {
          LOG.error("Failed to load list of ng-add compatible packages from static file.", e1)
        }

      }

      return emptyList()
    }

    private fun readNgAddPackages(content: String): List<NodePackageBasicInfo> {
      val contents = JsonParser.parseString(content) as JsonObject
      return contents.get(NG_ADD_SCHEMATIC).asJsonObject
        .entrySet().map { e -> NodePackageBasicInfo(e.key, e.value.asString) }
    }

    @Throws(IOException::class)
    private fun getSchematicsCollection(packageJson: File): File? {
      JsonReader(InputStreamReader(FileInputStream(packageJson), StandardCharsets.UTF_8))
        .use { reader ->
          reader.beginObject()
          while (reader.hasNext()) {
            val key = reader.nextName()
            if (key == SCHEMATICS_PROP) {
              val path = reader.nextString()
              return Paths.get(packageJson.parent, path).normalize().toAbsolutePath().toFile()
            }
            else {
              reader.skipValue()
            }
          }
          return null
        }
    }

    @Throws(IOException::class)
    private fun hasNgAddSchematic(schematicsCollection: File): Boolean {
      JsonReader(InputStreamReader(FileInputStream(schematicsCollection), StandardCharsets.UTF_8))
        .use { reader -> return hasNgAddSchematic(reader) }
    }

    @Throws(IOException::class)
    @JvmStatic
    fun hasNgAddSchematic(reader: JsonReader): Boolean {
      reader.isLenient = true
      reader.beginObject()
      while (reader.hasNext()) {
        val key = reader.nextName()
        if (SCHEMATICS_PROP == key) {
          reader.beginObject()
          while (reader.hasNext()) {
            val schematicName = reader.nextName()
            if (schematicName == NG_ADD_SCHEMATIC) {
              return true
            }
            reader.skipValue()
          }
          reader.endObject()
        }
        else {
          reader.skipValue()
        }
      }
      reader.endObject()
      return false
    }

    private fun checkForNgAddSupport(packageName: String, versionOrRange: String): Boolean {
      try {
        val indicator = ProgressManager.getInstance().progressIndicator
        val pkgJson = NpmRegistryService.getInstance().fetchPackageJson(packageName, versionOrRange, indicator)
        return pkgJson?.get(SCHEMATICS_PROP) != null
      }
      catch (e: Exception) {
        LOG.info(e)
      }

      return false
    }

    private fun getKey(packageName: String,
                       version: String): String {
      return "$packageName@$version"
    }

    private fun getCachedSchematics(dataHolder: UserDataHolder, key: Key<CachedSchematics>): CachedSchematics {
      var result = dataHolder.getUserData(key)
      if (result != null) {
        return result
      }

      if (dataHolder is UserDataHolderEx) {
        return dataHolder.putUserDataIfAbsent(key, CachedSchematics())
      }
      result = CachedSchematics()
      dataHolder.putUserData(key, result)
      return result
    }
  }
}
