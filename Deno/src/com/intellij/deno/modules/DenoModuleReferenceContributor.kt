package com.intellij.deno.modules

import com.google.common.hash.Hashing
import com.google.gson.JsonParser
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoUtil.getOwnUrlForFile
import com.intellij.javascript.JSModuleBaseReference
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathBuilder
import com.intellij.lang.ecmascript6.psi.impl.JSImportPathConfiguration
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil.isRelative
import com.intellij.lang.javascript.frameworks.modules.*
import com.intellij.lang.javascript.frameworks.modules.JSUrlImportsUtil.trimSchema
import com.intellij.lang.javascript.modules.JSModuleDescriptorFactory
import com.intellij.lang.javascript.modules.JSModuleNameInfo
import com.intellij.lang.javascript.modules.imports.JSImportDescriptor
import com.intellij.lang.javascript.modules.imports.JSSimpleImportDescriptor
import com.intellij.lang.javascript.psi.resolve.JSModuleReferenceContributor
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
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiUtilCore
import java.nio.charset.StandardCharsets
import java.nio.file.Paths


class DenoModuleReferenceContributor : JSModuleReferenceContributor {

  override fun isApplicable(host: PsiElement): Boolean = DenoSettings.getService(host.project).isUseDeno()

  override fun getAllReferences(unquotedRefText: String,
                                host: PsiElement,
                                offset: Int,
                                provider: PsiReferenceProvider?): Array<PsiReference> {
    if (JSUrlImportsUtil.startsWithRemoteUrlPrefix(unquotedRefText)) {
      return resolveAsUrl(unquotedRefText, host, TextRange(offset, offset + unquotedRefText.length))
    }

    if (isRelative(unquotedRefText)) {
      return resolveAsDenoLibFile(unquotedRefText, host, TextRange(offset, offset + unquotedRefText.length))
    }

    return resolveAsImportMap(unquotedRefText, host, offset)
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

    return getImportMapDescriptors(configuration)
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
      for (pathSubstitution in pathSubstitutions) {
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

  private fun getImportMapDescriptors(configuration: JSImportPathConfiguration): List<JSImportDescriptor> {
    val (pathSubstitutions, importMapFile) = readImportMap(configuration.place) ?: return emptyList()
    if (pathSubstitutions.isEmpty()) return emptyList()

    val builder = JSImportPathBuilder.createBuilder(configuration)
    return builder.getMappingsForBaseUrl(importMapFile.parent, pathSubstitutions)
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
    val builder = LookupElementBuilder.create("https://deno.land").withIcon(JSUrlImportsUtil.getIcon())
    return listOf(PrioritizedLookupElement.withPriority(builder, -1.0))
  }
}

private fun resolveAsImportMap(unquotedRefText: String, host: PsiElement, offset: Int): Array<PsiReference> {
  val (substitutions, importMapFile) = readImportMap(host) ?: return emptyArray()
  if (substitutions.isEmpty()) return emptyArray()

  val exactPath = JSPathMappingsUtil.substituteMappings(unquotedRefText, substitutions).firstOrNull() ?: return emptyArray()

  if (exactPath != unquotedRefText) {
    if (JSUrlImportsUtil.startsWithRemoteUrlPrefix(exactPath)) {
      return resolveAsUrl(exactPath, host, TextRange(offset, offset + unquotedRefText.length))
    }
    else {
      val file = JSPathMappingsUtil.getStringPathRelativeBaseUrlOrSelfIfAbsolute(importMapFile.parent, exactPath)
                 ?: return PsiReference.EMPTY_ARRAY
      val range = TextRange.create(offset, offset + unquotedRefText.length)
      return arrayOf(object : JSExactFileReference(host, range, listOf(file), null) {
        override fun getWeight(): Int = JSModuleBaseReference.ModuleTypes.PATH_MAPPING.weight()
      })
    }
  }

  return emptyArray()
}

private fun readImportMap(host: PsiElement): Pair<List<JSModulePathSubstitution>, VirtualFile>? {
  val importMapPath = extractImportMapPath(host) ?: return null
  val virtualFile = PsiUtilCore.getVirtualFile(host) ?: return null
  val importMapFile = virtualFile.fileSystem.findFileByPath(importMapPath) ?: return null
  val jsonMap = host.manager.findFile(importMapFile) as? JsonFile ?: return null
  return Pair(CachedValuesManager.getCachedValue(jsonMap, CachedValueProvider {
    CachedValueProvider.Result(parseMap(jsonMap), jsonMap)
  }), virtualFile)
}

private fun parseMap(file: JsonFile): List<JSModulePathSubstitution> {
  val topLevelValue = file.topLevelValue
  if (topLevelValue !is JsonObject) return emptyList()
  val importsValue = topLevelValue.findProperty("imports")?.value ?: return emptyList()
  if (importsValue !is JsonObject) return emptyList()

  val result = mutableListOf<JSModulePathSubstitution>()
  for (jsonProperty in importsValue.propertyList) {
    val name = jsonProperty.name
    val value = (jsonProperty.value as? JsonStringLiteral)?.value ?: continue
    result.add(object : JSModulePathSubstitutionImpl(name, value) {
      override fun canStartWith(): Boolean {
        return mappedName.endsWith("/")
      }
    })
  }

  return result
}

private fun extractImportMapPath(host: PsiElement): String? {
  val pathMacroManager = PathMacroManager.getInstance(host.project)
  val initString = pathMacroManager.expandPath(DenoSettings.getService(host.project).getDenoInit())
  val parsed = JsonParser.parseString(initString)
  if (!parsed.isJsonObject) return null
  val importMapProperty = parsed.asJsonObject.get("importMap")
  if (importMapProperty == null || !importMapProperty.isJsonPrimitive) return null
  val value = importMapProperty.asString ?: return null
  if (Paths.get(value).isAbsolute) return value
  
  val guessProjectDir = host.project.guessProjectDir() ?: return null
  return guessProjectDir.path + "/" + FileUtil.toSystemIndependentName(value)
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


