package com.intellij.deno.modules

import com.google.common.hash.Hashing
import com.google.gson.JsonParser
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoUtil.getOwnUrlForFile
import com.intellij.deno.findDenoConfig
import com.intellij.deno.isDenoEnableForContext
import com.intellij.javascript.JSModuleBaseReference
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathConfiguration
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil.isRelative
import com.intellij.lang.javascript.frameworks.modules.JSExactFileReference
import com.intellij.lang.javascript.frameworks.modules.JSImportMapContributorBase
import com.intellij.lang.javascript.frameworks.modules.JSPathMappingsUtil
import com.intellij.lang.javascript.frameworks.modules.JSUrlImportsUtil
import com.intellij.lang.javascript.frameworks.modules.JSUrlImportsUtil.trimSchema
import com.intellij.lang.javascript.modules.JSModuleDescriptorFactory
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.JSSimpleImportDescriptor
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.component1
import com.intellij.openapi.util.component2
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiUtilCore
import java.nio.charset.StandardCharsets


private const val npmPrefix = "npm:"
private const val npmRegistry = "registry.npmjs.org"

class DenoModuleReferenceContributor : JSImportMapContributorBase() {

  override fun isApplicable(host: PsiElement): Boolean = isDenoEnableForContext(host)

  override fun getAllReferences(unquotedEscapedText: String,
                                host: PsiElement,
                                offset: Int,
                                provider: PsiReferenceProvider?): Array<PsiReference> {
    if (JSUrlImportsUtil.startsWithRemoteUrlPrefix(unquotedEscapedText)) {
      return resolveAsUrl(unquotedEscapedText, host, TextRange(offset, offset + unquotedEscapedText.length))
    }

    if (isRelative(unquotedEscapedText)) {
      return resolveAsDenoLibFile(unquotedEscapedText, host, TextRange(offset, offset + unquotedEscapedText.length))
    }

    if (unquotedEscapedText.startsWith(npmPrefix)) {
      return resolveAsNpmPackage(unquotedEscapedText, host, TextRange(offset, offset + unquotedEscapedText.length))
    }

    return super.getAllReferences(unquotedEscapedText, host, offset, provider)
  }

  private fun resolveAsNpmPackage(unquotedEscapedText: String, host: PsiElement, range: TextRange): Array<PsiReference> {
    val path = unquotedEscapedText.removePrefix(npmPrefix)
    val parts = path.split("@")
    val packagePath = parts[0]
    val version = parts.getOrNull(1)
    val denoNpm = DenoSettings.getService(host.project).getDenoNpm()

    val url = "$denoNpm/$npmRegistry/$packagePath"
    val virtualFile = PsiUtilCore.getVirtualFile(host) ?: return emptyArray()
    val packageDirectory = virtualFile.fileSystem.findFileByPath(url) ?: return emptyArray()
    val versionDirectory = packageDirectory.children.firstOrNull {
      it.isDirectory && (it.name == version || version != null && it.name.startsWith(version))
    } ?: return emptyArray()

    return arrayOf(JSExactFileReference(host, range, listOf("$url/${versionDirectory.name}"), null))
  }

  override fun getAdditionalDescriptors(configuration: JSImportPathConfiguration,
                                        baseDescriptor: JSImportDescriptor): List<JSImportDescriptor> {
    val moduleDescriptor = baseDescriptor.moduleDescriptor
    if (moduleDescriptor !is JSModuleNameInfo) return emptyList()
    val resolvedModuleFile = moduleDescriptor.resolvedFile
    if (isCacheFile(configuration.place, resolvedModuleFile)) {
      val descriptors = buildForCachedFile(configuration, moduleDescriptor, baseDescriptor)
      val importMapDescriptors = getImportMapDescriptorsForCachedFiles(configuration, descriptors)
      return importMapDescriptors + descriptors
    }

    return super.getAdditionalDescriptors(configuration, baseDescriptor)
  }

  private fun getImportMapDescriptorsForCachedFiles(configuration: JSImportPathConfiguration,
                                                    descriptors: List<JSImportDescriptor>): List<JSImportDescriptor> {
    if (descriptors.isEmpty()) return emptyList()

    val (pathSubstitutions) = readImportMap(configuration.place) ?: return emptyList()
    if (pathSubstitutions.isEmpty()) return emptyList()
    val result = mutableListOf<JSImportDescriptor>()

    for (descriptor in descriptors) {
      val moduleDescriptor = descriptor.moduleDescriptor
      val moduleName = moduleDescriptor.moduleName
      for (pathSubstitution in pathSubstitutions.substitutions) {
        for (mapping in pathSubstitution.mappings) {
          if (moduleName.startsWith(mapping)) {
            val suffix = moduleName.substring(mapping.length)
            if (suffix.isNotEmpty() && !pathSubstitution.canStartWith()) continue
            val newModuleDescriptor = JSModuleDescriptorFactory.copyWithModuleName(configuration.place, moduleDescriptor,
                                                                                   pathSubstitution.mappedName + suffix)

            result.add(JSSimpleImportDescriptor(newModuleDescriptor, descriptor))
          }
        }
      }
    }

    return result
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

  override fun getDefaultWeight(): Int {
    return JSModuleBaseReference.ModuleTypes.PATH_MAPPING.weight()
  }

  override fun getLookupElements(unquotedEscapedText: String, host: PsiElement): Collection<LookupElement> {
    val denoLandElement = PrioritizedLookupElement.withPriority(
      LookupElementBuilder.create("https://deno.land").withIcon(JSUrlImportsUtil.getIcon()), -1.0)
    val npmElement = PrioritizedLookupElement.withPriority(LookupElementBuilder.create("npm:").withIcon(JSUrlImportsUtil.getIcon()), -1.0)
    return listOf(denoLandElement, npmElement)
  }

  override fun findImportMapFile(host: PsiElement): VirtualFile? {
    return extractImportMapPathFromDenoConfig(host) ?: extractImportMapPathFromInitObject(host)
  }
}

private fun extractImportMapPathFromDenoConfig(host: PsiElement): VirtualFile? {
  val config = findDenoConfig(host.project, PsiUtilCore.getVirtualFile(host)) ?: return null
  val findFile = host.manager.findFile(config) as? JsonFile ?: return null

  val literal = (findFile.topLevelValue as? JsonObject)?.findProperty("import_map") as? JsonStringLiteral ?: return null
  return JSPathMappingsUtil.getPathRelativeBaseUrlOrSelfIfAbsolute(config.parent, literal.value)
}

private fun extractImportMapPathFromInitObject(host: PsiElement): VirtualFile? {
  val pathMacroManager = PathMacroManager.getInstance(host.project)
  val initString = pathMacroManager.expandPath(DenoSettings.getService(host.project).getDenoInit())
  val parsed = JsonParser.parseString(initString)
  if (!parsed.isJsonObject) return null
  val importMapProperty = parsed.asJsonObject.get("importMap")
  if (importMapProperty == null || !importMapProperty.isJsonPrimitive) return null
  val value = importMapProperty.asString ?: return null
  val guessProjectDir = host.project.guessProjectDir() ?: return null

  return JSPathMappingsUtil.getPathRelativeBaseUrlOrSelfIfAbsolute(guessProjectDir, value)
}

fun resolveAsUrl(urlText: String,
                 host: PsiElement,
                 range: TextRange): Array<PsiReference> {
  val (withoutSchema, schema) = trimSchema(urlText) ?: return emptyArray()
  val denoDeps = DenoSettings.getService(host.project).getDenoCacheDeps()
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


fun resolveAsDenoLibFile(url: String,
                         host: PsiElement,
                         range: TextRange): Array<PsiReference> {
  if (!url.startsWith(".")) return emptyArray()
  val virtualFile = PsiUtilCore.getVirtualFile(host) ?: return emptyArray()
  val path = virtualFile.path

  val denoDeps = DenoSettings.getService(host.project).getDenoCacheDeps()
  if (!path.startsWith(denoDeps)) return emptyArray()

  val ownUrl = getOwnUrlForFile(host, virtualFile) ?: return emptyArray()
  val (ownPath, schema) = trimSchema(ownUrl) ?: return emptyArray()
  val indexOfSuffix = ownPath.lastIndexOf("/")
  if (indexOfSuffix <= 0) return emptyArray()
  val toCanonicalPath = FileUtil.toCanonicalPath("${ownPath.substring(0, indexOfSuffix)}/$url", false)
  return resolveAsUrl("$schema://$toCanonicalPath", host, range)
}


