// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.util.concurrent.Striped
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.applyIf
import com.intellij.util.io.encodeUrlQueryParameter
import org.intellij.terraform.config.model.version.MalformedConstraintException
import org.intellij.terraform.config.model.version.Version
import org.intellij.terraform.config.model.version.VersionConstraint
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.nullize
import java.util.*

object ModuleDetectionUtil {
  private val LOG = Logger.getInstance(ModuleDetectionUtil::class.java)

  data class ModulesManifest(val context: VirtualFile, val modules: List<ModuleManifest>) {
    companion object {
      // {
      //   "Key": "",
      //   "Source": "",
      //   "Dir": "."
      // },
      private val Terraform012RootModule = ModuleManifest("", "", "", ".", "")
    }

    fun isTerraform012OrNewer(): Boolean {
      return modules.contains(Terraform012RootModule)
    }
  }

  /**
   * @param[source] value of Module's `source` property
   * @param[dir] path relative to project root, usually starts with '.terraform/modules/'
   * @param[root] path inside `dir` directory
   */
  data class ModuleManifest(val source: String, val key: String, val version: String, val dir: String, val root: String) {
    val full
      get() = dir + if (root.isNotEmpty()) "/$root" else ""
  }

  fun getAsModuleBlock(moduleBlock: HCLBlock): Module? {
    return CachedValuesManager.getCachedValue(moduleBlock, ModuleCachedValueProvider(moduleBlock))?.first
  }

  fun getAsModuleBlockOrError(moduleBlock: HCLBlock): Pair<Module?, String?> {
    return CachedValuesManager.getCachedValue(moduleBlock, ModuleCachedValueProvider(moduleBlock))
  }

  class ModuleCachedValueProvider(private val block: HCLBlock) : CachedValueProvider<Pair<Module?, String?>> {
    override fun compute(): CachedValueProvider.Result<Pair<Module?, String?>> {
      return doGetAsModuleBlock(block)
    }
  }

  private data class ModuleUri(val scheme: String?, val authority: String, val repo: String, val path: String, val params: String) {
    fun couldBeReferencedBy(refUri: ModuleUri): Boolean {
      fun normalizeRepoString(repo: String) = repo.removePrefix("/").removeSuffix(".git")
      return authority == refUri.authority &&
             normalizeRepoString(repo) == normalizeRepoString(refUri.repo) &&
             path.removePrefix("/") == refUri.path.removePrefix("/") &&
             parseParams(params, false).let { it == parseParams(refUri.params, true) || it == parseParams(refUri.params, false) }

    }

    companion object {

      val URL_REGEX = Regex("""((?:\w+::?)+)(?://)?([^/?#]*)([^?#;]*)(.*)""")
      val REF_REGEX = Regex("""((?:\w+::?)+)?(?://)?([^:/?#]*):?([^?#;]*)(.*)""")

      private fun splitAtDirPrefix(repoAndPath: String): Pair<String, String> {
        val ind = repoAndPath.indexOf("//")
        if (ind == -1) return repoAndPath to ""
        return repoAndPath.substring(0, ind) to repoAndPath.substring(ind + 1)
      }

      fun fromModulesJsonSource(modulesJsonSource: String): ModuleUri? {
        val (scheme, authority, repoAndPath, paramsStr) = URL_REGEX.matchEntire(modulesJsonSource)?.destructured ?: return null
        val (repo, dir) = splitAtDirPrefix(repoAndPath)
        val (params, dirFromParams) = splitAtDirPrefix(paramsStr)
        return ModuleUri(scheme, authority, repo, dir.ifEmpty { dirFromParams }, params)
      }

      fun fromReference(moduleReference: String): ModuleUri? {
        val (scheme, authority, repoAndPath, paramsStr) = REF_REGEX.matchEntire(moduleReference)?.destructured ?: return null
        val (repo, dir) = splitAtDirPrefix(repoAndPath)
        val (params, dirFromParams) = splitAtDirPrefix(paramsStr)
        return ModuleUri(scheme.nullize(), authority, repo, dir.ifEmpty { dirFromParams }, params)
      }

      private fun parseParams(paramString: String, encode: Boolean): Map<String, String> {
        if (paramString.isBlank()) return emptyMap()
        val result = sortedMapOf<String, String>()

        var remaining = if (paramString.startsWith("?")) paramString.subSequence(1, paramString.length) else paramString
        while (!remaining.isEmpty()) {
          val p = remaining.indexOf("=")
          if (p == -1) break
          val key = remaining.substring(0, p)
          remaining = remaining.subSequence(p + 1, remaining.length)

          val p1 = remaining.indexOf("&")
          val value: CharSequence
          if (p1 == -1) {
            value = remaining
            remaining = remaining.subSequence(remaining.length, remaining.length)
          }
          else {
            value = remaining.subSequence(0, p1)
            remaining = remaining.subSequence(p1 + 1, remaining.length)
          }

          result[key] = value.toString().applyIf(encode, String::encodeUrlQueryParameter)
        }

        return result
      }

    }
  }

  fun sourceMatch(modulesJsonSource: String, moduleReference: String): Boolean {
    if (modulesJsonSource == moduleReference) return true

    if (modulesJsonSource.startsWith("registry.terraform.io")) {
      return modulesJsonSource.removePrefix("registry.terraform.io").removePrefix("/") == moduleReference.removePrefix("/")
    }

    val modUri = ModuleUri.fromModulesJsonSource(modulesJsonSource) ?: return false
    val refUri = ModuleUri.fromReference(moduleReference) ?: return false
    return modUri.couldBeReferencedBy(refUri)
  }

  private fun doGetAsModuleBlock(moduleBlock: HCLBlock): CachedValueProvider.Result<Pair<Module?, String?>> {
    val name = moduleBlock.getNameElementUnquoted(1) ?: return CachedValueProvider.Result(null to null, moduleBlock)
    val sourceVal = moduleBlock.`object`?.findProperty("source")?.value as? HCLStringLiteral
                    ?: return CachedValueProvider.Result(null to "No 'source' property", moduleBlock)

    val file = moduleBlock.containingFile.originalFile
    val directory = file.containingDirectory ?: return CachedValueProvider.Result(null to null, moduleBlock, file)
    var err: String? = null

    val source = sourceVal.value
    val project = moduleBlock.project

    val dotTerraform = getTerraformDirSomewhere(directory)
    if (dotTerraform != null) {
      LOG.debug("Found .terraform directory: $dotTerraform")
      val manifestFile = getTerraformModulesManifestFile(project, dotTerraform)
      if (manifestFile != null) {
        LOG.debug("Found manifest.json: $manifestFile")
        val manifest = CachedValuesManager.getManager(project).getCachedValue(file, ManifestCachedValueProvider(manifestFile.url))
        if (manifest != null) {
          LOG.debug("All modules from modules.json: ${manifest.modules}")
          val module: ModuleManifest?
          if (isRegistrySource(source, sourceVal)) {
            val version = (moduleBlock.`object`?.findProperty("version")?.value as? HCLStringLiteral)?.value
            val constraint = getVersionConstraint(version) ?: VersionConstraint.AnyVersion
            module = newestModuleManifest(constraint, manifest.modules
              .filter { sourceMatch(it.source, source) })
          }
          else {
            val keyPrefix: String
            val pair = getKeyPrefix(directory, dotTerraform, manifest, name, source)
            if (pair.first == null) {
              val relativeModule = findRelativeModule(directory, moduleBlock, source)
              return CachedValueProvider.Result(relativeModule to (pair.second
                                                                   ?: "Can't determine key prefix"), moduleBlock, dotTerraform,
                                                manifestFile, *getModuleFiles(relativeModule))
            }
            keyPrefix = pair.first!!

            LOG.debug("Searching for module with source '$source' and keyPrefix '$keyPrefix'")
            module = manifest.modules.find {
              sourceMatch(it.source, source) && it.key.startsWith(keyPrefix) && !it.key.removePrefix(keyPrefix).contains('|')
            }
          }

          if (module != null) {
            LOG.debug("Found module $module")
            val path = FileUtil.toSystemIndependentName(module.full)
            var relative = manifest.context.findFileByRelativePath(path)
            if (relative != null) {
              LOG.debug("Absolute module dir: $relative")
              if (isRelativeSource(source)) {
                findRelativeModule(directory, moduleBlock, source)?.let {
                  LOG.debug("Shortcutting to relative module")
                  return CachedValueProvider.Result(it to null, moduleBlock, directory, dotTerraform, manifestFile, relative,
                                                    getModuleFiles(it))
                }
              }
              val canonical = relative.canonicalFile
              // TODO: Symlink resolving probably would not work on Windows
              if (canonical != null && canonical != relative) {
                if (getRoots(project).any { VfsUtilCore.isAncestor(it, canonical, true) }) {
                  LOG.debug("Replacing module relative path ('${relative.name}') with canonical: '$canonical'")
                  relative = canonical
                }
              }
              val dir = PsiManager.getInstance(project).findDirectory(relative)
              if (dir != null) {
                LOG.debug("Module search succeed, directory is $dir")
                val mod = Module(dir)
                return CachedValueProvider.Result(mod to null, moduleBlock, directory, dotTerraform, manifestFile, relative,
                                                  getModuleFiles(mod))
              }
              else {
                err = "Can't find PsiDirectory for $relative"
                LOG.debug(err)
              }
            }
            else {
              err = "Can't find relative dir '$path' in '${manifest.context}'"
              LOG.debug(err)
            }
          }
        }
        else {
          err = "Failed to parse .terraform/modules/modules.json, please rerun `terraform get`"
          LOG.warn(err)
        }
      }
      else {
        err = "No modules/modules.json found in .terraform directory, please run `terraform get` in appropriate place"
        LOG.warn(err)
      }
    }
    else {
      err = "No .terraform found under project directory, please run `terraform get` in appropriate place"
      LOG.warn(err)
    }

    if (err != null) {
      err = "Terraform Module '$name' with source '$source' directory not found locally, use `terraform get` to fetch modules."
      LOG.warn(err)
    }
    val relativeModule = findRelativeModule(directory, moduleBlock, source)
    return CachedValueProvider.Result(relativeModule to err, moduleBlock, directory, *getVFSChainOrVFS(directory, project),
                                      *getModuleFiles(relativeModule))
  }

  private fun newestModuleManifest(constraint: VersionConstraint, modules: List<ModuleManifest>): ModuleManifest? {
    if (modules.isEmpty()) return null
    if (modules.size == 1) {
      val candidate = modules.first()
      val version = Version.parseOrNull(candidate.version) ?: return null
      if (constraint.check(version)) return candidate
      return null
    }

    val versions = TreeMap<Version, ModuleManifest>()
    for (module in modules) {
      val version = Version.parseOrNull(module.version)
      if (version == null) {
        // ignore modules with incorrect versions
      }
      else {
        versions[version] = module
      }
    }
    return versions.entries.findLast { constraint.check(it.key) }?.value
  }

  fun getVersionConstraint(constraint: String?, report: Boolean = true): VersionConstraint? {
    if (constraint.isNullOrBlank()) return VersionConstraint.AnyVersion
    try {
      return VersionConstraint.parse(constraint.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "0"))
    }
    catch (e: MalformedConstraintException) {
      if (constraint.contains(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)) return null
      if (report) {
        logErrorAndFailInInternalMode(ApplicationManager.getApplication(), "Cannot parse version constraint '$constraint'", e)
      }
      return null
    }
  }

  // Based on configs/configload/source_addr.go:isLocalSourceAddr
  private fun isRelativeSource(source: String): Boolean {
    if (source.startsWith("./")) return true
    if (source.startsWith("../")) return true
    if (source.startsWith(".\\")) return true
    if (source.startsWith("..\\")) return true
    return false
  }

  private val IsRegistrySourceKey = Key<Boolean>("TF.IsRegistrySource")
  private val IsRegistrySourceLock = Striped.lock(16)

  private fun isRegistrySource(source: String, element: PsiElement): Boolean {
    var result: Boolean? = element.getUserData(IsRegistrySourceKey)
    if (result != null) return result
    synchronized(IsRegistrySourceLock.get(source)) {
      result = element.getUserData(IsRegistrySourceKey)
      if (result != null) return result!!
      result = RegistryModuleUtil.parseRegistryModule(source) != null
      element.putUserData(IsRegistrySourceKey, result)
    }
    return result!!
  }

  private fun getModuleFiles(module: Module?): Array<out PsiElement> {
    if (module == null) return emptyArray()
    return when (val item = module.item) {
      is PsiDirectory -> arrayOf(item, *item.files)
      else -> arrayOf(item)
    }
  }

  private fun getVFSChainOrVFS(directory: PsiDirectory, project: Project): Array<out ModificationTracker> {
    return getVFSChain(directory, project) ?: arrayOf(VirtualFileManager.getInstance())
  }

  private fun getVFSChain(file: PsiFileSystemItem, project: Project): Array<out VirtualFile>? {
    val roots = getRoots(project).toSet()
    val start = file.virtualFile
    if (!roots.any { VfsUtilCore.isAncestor(it, start, false) }) {
      LOG.warn("'$file' is not under project root nor any module content roots")
      return null
    }
    val chain = ArrayList<VirtualFile>()

    var parent: VirtualFile? = start
    while (true) {
      if (parent == null) return null
      chain.add(parent)
      if (roots.contains(parent)) break
      parent = parent.parent
    }
    return chain.toTypedArray()
  }


  private fun getTerraformModulesManifestFile(project: Project, dotTerraform: VirtualFile): VirtualFile? {
    if (!getRoots(project).any { VfsUtilCore.isAncestor(it, dotTerraform, false) }) {
      LOG.warn("Dir $dotTerraform is not under project root nor any module content roots")
      return null
    }
    if (!dotTerraform.isValid || !dotTerraform.exists()) return null

    val file = dotTerraform.findFileByRelativePath("modules/modules.json")
    if (file == null || !file.exists() || file.isDirectory || !file.isValid) {
      return null
    }
    return file
  }

  class ManifestCachedValueProvider(private val fileUrl: String) : CachedValueProvider<ModulesManifest> {
    override fun compute(): CachedValueProvider.Result<ModulesManifest>? {
      val file = VirtualFileManager.getInstance().findFileByUrl(fileUrl) ?: return null
      if (!file.isValid || !file.exists() || file.isDirectory) {
        return CachedValueProvider.Result(null, file)
      }
      val parsed = parseManifest(file)
      return CachedValueProvider.Result(parsed, file)
    }
  }

  private fun parseManifest(file: VirtualFile): ModulesManifest? {
    LOG.debug("Parsing manifest file $file")
    val stream = file.inputStream ?: return null
    val context = file.parent.parent.parent ?: return null
    val application = ApplicationManager.getApplication()
    try {
      val json: ObjectNode? = stream.use {
        ObjectMapper().readTree(it) as ObjectNode?
      }
      if (json == null) {
        logErrorAndFailInInternalMode(application, "In file '$file' no JSON found")
        return null
      }
      return ModulesManifest(context, json.array("Modules")?.filterIsInstance(ObjectNode::class.java)?.map {
        ModuleManifest(
          source = it.string("Source") ?: "",
          key = it.string("Key") ?: "",
          version = it.string("Version") ?: "",
          dir = it.string("Dir") ?: "",
          root = it.string("Root") ?: ""
        )
      } ?: emptyList())
    }
    catch (e: Throwable) {
      logErrorAndFailInInternalMode(application, "Failed to parse file '$file'", e)
    }
    return null
  }

  private fun logErrorAndFailInInternalMode(application: Application, msg: String, e: Throwable? = null) {
    if (e is ProcessCanceledException) throw e

    val msg2 = if (e == null) msg else "$msg: ${e.message}"
    if (e == null) LOG.error(msg2) else LOG.error(msg2, e)
    if (application.isInternal) {
      throw AssertionError(msg2, e)
    }
  }

  private fun findRelativeModule(directory: PsiDirectory, moduleBlock: HCLBlock, source: String): Module? {
    // Prefer local file paths over loaded modules.
    // TODO: Consider removing that
    // Used in tests

    val relative = directory.virtualFile.findFileByRelativePath(source) ?: return null
    if (!relative.exists() || !relative.isDirectory) return null
    return PsiManager.getInstance(moduleBlock.project).findDirectory(relative)?.let { Module(it) }
  }

  private fun getKeyPrefix(directory: PsiDirectory,
                           dotTerraform: VirtualFile,
                           manifest: ModulesManifest,
                           name: String,
                           source: String): Pair<String?, String?> {
    // Check whether current dir is a module itself
    val relativeToDotTerraform = VfsUtilCore.getRelativePath(directory.virtualFile, dotTerraform)
    val relativeToRoot = VfsUtilCore.getRelativePath(directory.virtualFile, dotTerraform.parent)
    if (relativeToDotTerraform != null) {
      val currentModule = manifest.modules.find { it.full == ".terraform/$relativeToDotTerraform" }
      if (currentModule != null) {
        return if (manifest.isTerraform012OrNewer()) {
          currentModule.key + '.' to null
        }
        else {
          currentModule.key + '|' to null
        }
      }
      else {
        val err = "Path '.terraform/$relativeToDotTerraform' not found among modules, either `terraform get` should be run or we're in non-referenced module, e.g. subdir of some module"
        LOG.info(err)
        return null to err
      }
    }
    else if (!relativeToRoot.isNullOrEmpty() && manifest.isTerraform012OrNewer()) {
      // Relative submodule
      val currentModule = manifest.modules.find { it.full == relativeToRoot }
      return if (currentModule != null) {
        currentModule.key + '.' to null
      }
      else {
        val err = "Path '$relativeToRoot' not found among modules, either `terraform get` should be run or we're in non-referenced module, e.g. subdir of some module"
        LOG.info(err)
        null to err
      }
    }
    else {
      return if (manifest.isTerraform012OrNewer()) {
        name to null
      }
      else {
        // Module referenced from root key would be '1.$NAME;$SOURCE' or '1.$NAME;$SOURCE.$VERSION'
        "1.$name;$source" to null
      }
    }
  }

  private fun getTerraformDirSomewhere(file: PsiDirectory): VirtualFile? {
    val roots = getRoots(file.project).toSet()
    val start = file.virtualFile
    if (!roots.any { VfsUtilCore.isAncestor(it, start, false) }) {
      LOG.warn("File $file is not under project root nor any module content roots")
      return null
    }
    var parent: VirtualFile? = start
    while (true) {
      if (parent == null) return null

      val child = parent.findChild(".terraform")
      if (child != null && child.isDirectory) {
        return child
      }
      parent = parent.parent
      if (roots.contains(parent)) break
    }
    return null
  }

  private fun getRoots(project: Project): Array<out VirtualFile> {
    return ProjectRootManager.getInstance(project).contentRootsFromAllModules
  }
}