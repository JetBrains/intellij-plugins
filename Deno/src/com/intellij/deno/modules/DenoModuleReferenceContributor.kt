package com.intellij.deno.modules

import com.google.common.hash.Hashing
import com.intellij.deno.DenoSettings
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathConfiguration
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.javascript.frameworks.modules.JSBaseModuleReferenceContributor
import com.intellij.lang.javascript.frameworks.modules.JSExactFileReference
import com.intellij.lang.javascript.modules.JSModuleDescriptorFactory
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.lang.javascript.modules.JSModuleNameInfo.ExtensionSettings
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.JSSimpleImportDescriptor
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiUtilCore
import java.io.File
import java.nio.charset.StandardCharsets


class DenoModuleReferenceContributor : JSBaseModuleReferenceContributor() {
  override fun isApplicable(host: PsiElement): Boolean {
    return DenoSettings.getService(host.project).isUseDeno()
  }

  override fun getReferences(unquotedRefText: String,
                             host: PsiElement,
                             offset: Int,
                             provider: PsiReferenceProvider?,
                             isCommonJS: Boolean): Array<PsiReference> {
    val denoDeps = DenoSettings.getService(host.project).getDenoCacheDeps()
    return getReferencesForUrl(unquotedRefText, denoDeps, host, TextRange(offset, offset + unquotedRefText.length))
  }

  private fun getReferencesForUrl(unquotedRefText: String,
                                  denoDeps: String,
                                  host: PsiElement,
                                  range: TextRange): Array<PsiReference> {
    val (withoutSchema, schema) = trimSchema(unquotedRefText) ?: return resolveAsDenoLibFile(unquotedRefText, host, range, denoDeps)

    val firstPart = withoutSchema.indexOf("/")
    if (firstPart <= 0) return emptyArray()
    val directory = withoutSchema.substring(0, firstPart)
    val otherPart = withoutSchema.substring(firstPart)
    val sha256hex = Hashing.sha256()
      .hashString(otherPart, StandardCharsets.UTF_8)
      .toString()

    val url = "$denoDeps/$schema/$directory/$sha256hex"
    return arrayOf(JSExactFileReference(host, range, listOf(url), null))
  }

  private fun resolveAsDenoLibFile(unquotedRefText: String,
                                   host: PsiElement,
                                   range: TextRange,
                                   denoDeps: String): Array<PsiReference> {
    if (!unquotedRefText.startsWith(".")) return emptyArray()
    val virtualFile = PsiUtilCore.getVirtualFile(host) ?: return emptyArray()
    val path = virtualFile.path
    if (!path.startsWith(denoDeps)) return emptyArray()

    val ownUrl = getOwnUrlForFile(host, virtualFile) ?: return emptyArray()
    val (ownPath, schema) = trimSchema(ownUrl) ?: return emptyArray()
    val indexOfSuffix = ownPath.lastIndexOf("/")
    if (indexOfSuffix <= 0) return emptyArray()
    val toCanonicalPath = FileUtil.toCanonicalPath("${ownPath.substring(0, indexOfSuffix)}/$unquotedRefText", false)
    return getReferencesForUrl("$schema://$toCanonicalPath", denoDeps, host, range)
  }

  private fun getOwnUrlForFile(place: PsiElement, virtualFile: VirtualFile): String? {
    //todo cache to virtual file gist?
    val metadata = virtualFile.parent.findChild(virtualFile.name + ".metadata.json")
    if (metadata == null) return null
    val metaDataPsi = place.manager.findFile(metadata)
    if (metaDataPsi !is JsonFile) return null
    val values = metaDataPsi.allTopLevelValues
    for (topLevelValue in values) {
      val property = (topLevelValue as? JsonObject)?.findProperty("url") ?: continue
      val url = (property.value as? JsonStringLiteral)?.value
      if (url != null) return url
    }

    return null
  }

  @Suppress("HttpUrlsUsage")
  private fun trimSchema(unquotedRefText: String): Pair<String, String>? {
    if (unquotedRefText.startsWith("http://")) return Pair(StringUtil.trimStart(unquotedRefText, "http://"), "http")
    if (unquotedRefText.startsWith("https://")) return Pair(StringUtil.trimStart(unquotedRefText, "https://"), "https")
    return null
  }


  override fun getImportDescriptors(configuration: JSImportPathConfiguration,
                                    baseDescriptor: JSImportDescriptor): List<JSImportDescriptor> {
    val moduleDescriptor = baseDescriptor.moduleDescriptor
    if (moduleDescriptor !is JSModuleNameInfo) return emptyList()
    val resolvedModuleFile = moduleDescriptor.resolvedFile
    if (isCacheFile(configuration.place, resolvedModuleFile)) {
      return buildForCachedFile(configuration, moduleDescriptor, baseDescriptor)
    }

    return emptyList()
  }

  private fun isCacheFile(place: PsiElement, file: VirtualFile): Boolean {
    val denoDeps = DenoSettings.getService(place.project).getDenoCacheDeps()
    return file.path.startsWith(denoDeps)
  }

  private fun buildForCachedFile(configuration: JSImportPathConfiguration,
                                 moduleDescriptor: JSModuleNameInfo,
                                 baseDescriptor: JSImportDescriptor): List<JSImportDescriptor> {
    val resolvedModuleFile = moduleDescriptor.resolvedFile
    val moduleFileOrDirectory = moduleDescriptor.moduleFileOrDirectory
    val ownUrlForFile = getOwnUrlForFile(configuration.place, resolvedModuleFile) ?: return emptyList()
    val newInfo = JSModuleDescriptorFactory.createExactModuleDescriptor(ownUrlForFile, moduleFileOrDirectory, resolvedModuleFile,
      configuration.place)
    return listOf(JSSimpleImportDescriptor(newInfo, baseDescriptor))
  }
}