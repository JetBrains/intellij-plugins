package com.intellij.deno.modules

import com.google.gson.JsonParser
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.deno.DenoSettings
import com.intellij.deno.findDenoConfig
import com.intellij.deno.isDenoEnableForContext
import com.intellij.deno.model.*
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
import com.intellij.openapi.components.service
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


class DenoModuleReferenceContributor : JSImportMapContributorBase() {

  override fun isApplicable(host: PsiElement): Boolean = isDenoEnableForContext(host)

  override fun getAllReferences(
    unquotedEscapedText: String,
    host: PsiElement,
    offset: Int,
    provider: PsiReferenceProvider?,
  ): Array<PsiReference> {
    if (JSUrlImportsUtil.startsWithRemoteUrlPrefix(unquotedEscapedText)) {
      return resolveAsUrl(unquotedEscapedText, host, TextRange(offset, offset + unquotedEscapedText.length))
    }

    if (isRelative(unquotedEscapedText)) {
      return resolveAsDenoLibFile(unquotedEscapedText, host, TextRange(offset, offset + unquotedEscapedText.length))
    }

    if (hasJsrImportPrefix(unquotedEscapedText)) {
      return resolveAsJsr(unquotedEscapedText, host, TextRange(offset, offset + unquotedEscapedText.length))
    }

    if (hasNpmImportPrefix(unquotedEscapedText)) {
      return resolveAsNpmPackage(unquotedEscapedText, host, TextRange(offset, offset + unquotedEscapedText.length))
    }

    return super.getAllReferences(unquotedEscapedText, host, offset, provider)
  }

  override fun getAdditionalDescriptors(
    configuration: JSImportPathConfiguration,
    baseDescriptor: JSImportDescriptor,
  ): List<JSImportDescriptor> {
    val moduleDescriptor = baseDescriptor.moduleDescriptor
    if (moduleDescriptor !is JSModuleNameInfo) return emptyList()
    val resolvedModuleFile = moduleDescriptor.resolvedFile
    if (isDepsFile(configuration.project, resolvedModuleFile)) {
      val descriptors = buildForCachedFile(configuration, moduleDescriptor, baseDescriptor)
      val importMapDescriptors = getImportMapDescriptorsForCachedFiles(configuration, descriptors)
      return importMapDescriptors + descriptors
    }
    if (isNpmFile(configuration.project, resolvedModuleFile)) {
      val descriptors = buildForNpm(configuration, moduleDescriptor, baseDescriptor)
      val importMapDescriptors = getImportMapDescriptorsForCachedFiles(configuration, descriptors)
      return importMapDescriptors + descriptors
    }

    return super.getAdditionalDescriptors(configuration, baseDescriptor)
  }

  private fun getImportMapDescriptorsForCachedFiles(
    configuration: JSImportPathConfiguration,
    descriptors: List<JSImportDescriptor>,
  ): List<JSImportDescriptor> {
    if (descriptors.isEmpty()) return emptyList()

    val (pathSubstitutions) = readImportMap(configuration.place) ?: return emptyList()
    if (pathSubstitutions.isEmpty()) return emptyList()
    val result = mutableListOf<JSImportDescriptor>()

    for (descriptor in descriptors) {
      val moduleDescriptor = descriptor.moduleDescriptor
      val moduleName = moduleDescriptor.moduleName
      for (pathSubstitution in pathSubstitutions.substitutions) {
        for (moduleMapping in pathSubstitution.mappings) {
          if (moduleName.startsWith(moduleMapping.mapping)) {
            val suffix = moduleName.substring(moduleMapping.mapping.length)
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

  private fun buildForCachedFile(
    configuration: JSImportPathConfiguration, moduleDescriptor: JSModuleNameInfo, baseDescriptor: JSImportDescriptor,
  ): List<JSImportDescriptor> {
    val resolvedModuleFile = moduleDescriptor.resolvedFile
    val moduleFileOrDirectory = moduleDescriptor.moduleFileOrDirectory
    val project = configuration.project
    val model = project.service<DenoModel>()
    var ownUrlForFile = model.findOwnUrlForFile(resolvedModuleFile) ?: return emptyList()
    if (ownUrlForFile.startsWith(jsrUrlPart)) {
      parseDenoUrl(ownUrlForFile)?.let {
        val isMod = it.subPath == modFileName
        ownUrlForFile = "$jsrImportPrefix${it.packageName}@${it.version}${if (isMod) "" else it.subPathPart()}"
      }
    }

    val newInfo = JSModuleDescriptorFactory.createExactModuleDescriptor(ownUrlForFile, moduleFileOrDirectory, resolvedModuleFile,
                                                                        configuration.place)
    return listOf(JSSimpleImportDescriptor(newInfo, baseDescriptor))
  }

  private fun buildForNpm(
    configuration: JSImportPathConfiguration, moduleDescriptor: JSModuleNameInfo, baseDescriptor: JSImportDescriptor,
  ): List<JSImportDescriptor> {
    val npmModel = buildNpmModel(moduleDescriptor.moduleFileOrDirectory) ?: return emptyList()

    val resolvedModuleFile = moduleDescriptor.resolvedFile
    val moduleFileOrDirectory = moduleDescriptor.moduleFileOrDirectory

    val newInfo = JSModuleDescriptorFactory.createExactModuleDescriptor(npmModel.toImportPath(), moduleFileOrDirectory, resolvedModuleFile,
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

  override fun resolveAsImportMapExactText(importMapFile: VirtualFile, exactPath: String, range: TextRange, host: PsiElement): Array<PsiReference> {
    if (JSUrlImportsUtil.startsWithRemoteUrlPrefix(exactPath)) return resolveAsUrl(exactPath, host, range)
    if (hasNpmImportPrefix(exactPath)) return resolveAsNpmPackage(exactPath, host, range)
    if (hasJsrImportPrefix(exactPath)) return resolveAsJsr(exactPath, host, range)

    return super.resolveAsImportMapExactText(importMapFile, exactPath, range, host)
  }
}

private fun extractImportMapPathFromDenoConfig(host: PsiElement): VirtualFile? {
  val config = findDenoConfig(host.project, PsiUtilCore.getVirtualFile(host)) ?: return null
  val findFile = host.manager.findFile(config) as? JsonFile ?: return null

  val topLevelObject = findFile.topLevelValue as? JsonObject ?: return null
  if (topLevelObject.findProperty("imports") != null) return config

  val literal = topLevelObject.findProperty("import_map") as? JsonStringLiteral ?: return null

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

private fun resolveAsJsr(
  urlText: String,
  host: PsiElement,
  range: TextRange,
): Array<PsiReference> {
  val model = host.project.service<DenoModel>()
  val path = model.findFilePathByJsrImport(urlText) ?: return emptyArray()
  return arrayOf(JSExactFileReference(host, range, listOf(path), null))
}

private fun resolveAsUrl(
  urlText: String,
  host: PsiElement,
  range: TextRange,
): Array<PsiReference> {
  val model = host.project.service<DenoModel>()
  val path = model.findFilePathByUrlImport(urlText) ?: return emptyArray()
  return arrayOf(JSExactFileReference(host, range, listOf(path), null))
}

private fun resolveAsNpmPackage(unquotedEscapedText: String, host: PsiElement, range: TextRange): Array<PsiReference> {
  val model = host.project.service<DenoModel>()
  val path = model.findFilePathByNpmImport(unquotedEscapedText) ?: return emptyArray()
  return arrayOf(JSExactFileReference(host, range, listOf(path), null))
}

private fun resolveAsDenoLibFile(
  url: String,
  host: PsiElement,
  range: TextRange,
): Array<PsiReference> {
  if (!url.startsWith(".")) return emptyArray()
  val virtualFile = PsiUtilCore.getVirtualFile(host) ?: return emptyArray()
  val path = virtualFile.path

  val denoDeps = DenoSettings.getService(host.project).getDenoCacheDeps()
  if (!path.startsWith(denoDeps)) return emptyArray()

  val model = host.project.service<DenoModel>()
  val ownUrl = model.findOwnUrlForFile(virtualFile) ?: return emptyArray()
  val (ownPath, schema) = trimSchema(ownUrl) ?: return emptyArray()
  val indexOfSuffix = ownPath.lastIndexOf("/")
  if (indexOfSuffix <= 0) return emptyArray()
  val toCanonicalPath = FileUtil.toCanonicalPath("${ownPath.substring(0, indexOfSuffix)}/$url", false)

  return resolveAsUrl("$schema://$toCanonicalPath", host, range)
}


