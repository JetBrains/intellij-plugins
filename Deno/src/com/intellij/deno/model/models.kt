package com.intellij.deno.model

import com.intellij.deno.DenoSettings
import com.intellij.deno.lang.getDenoCacheElementsByKey
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.javascript.frameworks.modules.JSUrlImportsUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.util.text.nullize

const val npmImportPrefix = "npm:"
const val jsrImportPrefix = "jsr:"

private const val npmRegistry = "registry.npmjs.org"
private val denoUrlFileRefKey = Key.create<String?>("deno.file.url")


data class UrlModel(
  val schema: String,
  val namespace: String,
  val subNamespace: String,
  val packageName: String,
  val subPath: String?,
  val version: String?,
) {
  override fun toString(): String {
    val jsrFormat = isJsr(namespace)
    val versionPart = version?.let { if (jsrFormat) "/$version" else "@$version" } ?: ""
    return "$schema://$namespace${subNamespacePart()}/$packageName$versionPart${subPathPart()}"
  }

  fun fullPath(): String {
    return "$namespace${subNamespacePart()}/$packageName${subPathPart()}"
  }

  private fun subPathPart(): String = subPath?.let { "/$it" } ?: ""
  private fun subNamespacePart(): String = if (subNamespace.isEmpty()) "" else "/$subNamespace"
}


fun parseDenoUrl(url: String): UrlModel? {
  try {
    val pair = JSUrlImportsUtil.trimSchema(url) ?: return null
    val pathWithNamespace = pair.first
    val schema = pair.second
    val indexOf = pathWithNamespace.indexOf('/')
    if (indexOf <= 0) return null

    val namespace = pathWithNamespace.substring(0, indexOf)
    var path = pathWithNamespace.substring(indexOf + 1)
    var scope = ""

    var subNamespace = ""


    //we know the structure of JSR urls, and it has a different versioning scheme,
    // so subNamespace is possible only in other cases
    if (!isJsr(namespace)) {
      val atIndex = path.indexOf("@")

      var next = atIndex - 1
      while (next >= 0) {
        val prev = path[next]
        if (prev == '/') {
          subNamespace = path.substring(0, next)
          path = path.substring(next + 1)
          break
        }
        next--
      }
    }

    //scoped package
    if (path.startsWith('@')) {
      val scopeEnd = path.indexOf('/')
      if (scopeEnd > 0) {
        scope = path.substring(0, scopeEnd + 1)
        path = path.substring(scopeEnd + 1)
      }
    }
    val versionAtIndex = path.indexOf('@')
    val nextPartIndex = path.indexOf('/')
    val endOfPackage = if (versionAtIndex > 0 && (nextPartIndex < 0 || versionAtIndex < nextPartIndex)) versionAtIndex else nextPartIndex
    var packageName = scope + if (endOfPackage > 0) path.substring(0, endOfPackage) else path

    val restPart = path.substring(endOfPackage + 1)

    val afterVersionIndex = restPart.indexOf('/')
    val hasVersion = isJsr(namespace) && afterVersionIndex > 0 || versionAtIndex > 0
    if (afterVersionIndex < 0 && hasVersion) {
      return UrlModel(schema, namespace, subNamespace, packageName, null, restPart)
    }

    var version = if (hasVersion) restPart.substring(0, afterVersionIndex) else null

    val subPath = if (hasVersion) restPart.substring(afterVersionIndex + 1) else restPart
    return UrlModel(schema, namespace, subNamespace, packageName, subPath.nullize(), version.nullize())
  }
  catch (e: Exception) {
    logger<UrlModel>().error("Deno url parsing error", e)
    return null
  }
}

@Service(Level.PROJECT)
class DenoModel(private val project: Project) {

  fun findFilePathByUrlImport(importPath: String): String? {
    if (!JSUrlImportsUtil.startsWithRemoteUrlPrefix(importPath)) return null

    val url = parseDenoUrl(importPath) ?: return null
    val key = url.fullPath()
    val elements = getDenoCacheElementsByKey(project, key)

    val el = elements.firstOrNull() ?: return null

    val deps = DenoSettings.getService(project).getDenoCacheDeps()
    return "$deps/${url.schema}/${url.namespace}/${el.hash}"
  }

  fun findFilePathByJsrImport(importPath: String): String? {
    if (!hasJsrImportPrefix(importPath)) return null
    val path = importPath.removePrefix(jsrImportPrefix)

    val url = parseDenoUrl("https://jsr.io/$path") ?: return null

    val key = url.fullPath() + getJsrPostfix(url)

    val elements = getDenoCacheElementsByKey(project, key)

    val el = elements.firstOrNull() ?: return null

    val deps = DenoSettings.getService(project).getDenoCacheDeps()
    return "$deps/${url.schema}/${url.namespace}/${el.hash}"
  }

  /**
   * This implementation is wrong: we use just names for the resolution, while deno itself uses "exports" definition
   */
  fun getJsrPostfix(url: UrlModel): String {
    val subPath = url.subPath
    if (subPath == null) return "/mod.ts"

    return subPath + if (subPath.endsWith(".ts")) "" else ".ts"
  }

  fun findFilePathByNpmImport(unquotedEscapedText: String): String? {
    val path = unquotedEscapedText.removePrefix(npmImportPrefix)

    val parts = path.split("@")
    val packagePath = parts[0]
    val version = parts.getOrNull(1)
    val denoNpm = DenoSettings.getService(project).getDenoNpm()

    val url = "$denoNpm/$npmRegistry/$packagePath"
    val packageDirectory = LocalFileSystem.getInstance().findFileByPath(url) ?: return null
    val versionDirectory = packageDirectory.children.firstOrNull {
      it.isDirectory && (it.name == version || version != null && it.name.startsWith(version))
    } ?: return null

    return "$url/${versionDirectory.name}"
  }

  @NlsSafe
  fun findOwnUrlForFile(virtualFile: VirtualFile): String? {
    val psiManager = PsiManager.getInstance(project)

    val userData = virtualFile.getUserData(denoUrlFileRefKey)
    if (userData != null) return userData

    val metadata = virtualFile.parent.findChild(virtualFile.name + ".metadata.json")
    if (metadata == null) return null
    val metaDataPsi = psiManager.findFile(metadata)
    if (metaDataPsi !is JsonFile) return null
    val values = metaDataPsi.allTopLevelValues
    for (topLevelValue in values) {
      val property = (topLevelValue as? JsonObject)?.findProperty("url") ?: continue
      val url = (property.value as? JsonStringLiteral)?.value
      if (url != null) {
        virtualFile.putUserData(denoUrlFileRefKey, url)
        return url
      }
    }

    return null
  }
}

fun isJsr(namespace: String): Boolean = namespace == "jsr.io"
fun hasJsrImportPrefix(importText: String): Boolean = importText.startsWith(jsrImportPrefix)
fun hasNpmImportPrefix(importText: String): Boolean = importText.startsWith(npmImportPrefix)