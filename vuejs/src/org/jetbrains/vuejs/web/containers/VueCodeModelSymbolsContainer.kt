// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.containers

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.ecmascript6.psi.*
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import com.intellij.util.containers.MultiMap
import com.intellij.webSymbols.SymbolKind
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.WebSymbolsContainerWithCache
import com.intellij.webSymbols.WebSymbolsRegistryManager
import com.intellij.webSymbols.webTypes.WebTypesSymbol
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.VueWebSymbolsRegistryExtension.Companion.KIND_VUE_COMPONENTS
import org.jetbrains.vuejs.web.VueWebSymbolsRegistryExtension.Companion.KIND_VUE_DIRECTIVES
import org.jetbrains.vuejs.web.asWebSymbol
import org.jetbrains.vuejs.web.symbols.VueDocumentedItemSymbol
import org.jetbrains.vuejs.web.symbols.VueWebTypesMergedSymbol
import java.util.*

class VueCodeModelSymbolsContainer<K> private constructor(private val container: VueEntitiesContainer,
                                                          project: Project,
                                                          dataHolder: UserDataHolder,
                                                          private val proximity: VueModelVisitor.Proximity,
                                                          key: K)
  : WebSymbolsContainerWithCache<UserDataHolder, K>(VueFramework.ID, project, dataHolder, key) {

  companion object {

    fun create(container: VueEntitiesContainer, proximity: VueModelVisitor.Proximity): VueCodeModelSymbolsContainer<*>? {
      container.source
        ?.let {
          return VueCodeModelSymbolsContainer(container, it.project, it, proximity, proximity)
        }
      return if (container is VueGlobal)
        VueCodeModelSymbolsContainer(container, container.project, container.project, proximity, container.packageJsonUrl ?: "")
      else null
    }

  }

  override fun toString(): String {
    return "EntityContainerWrapper($container)"
  }

  override fun createPointer(): Pointer<VueCodeModelSymbolsContainer<K>> {
    val containerPtr = container.createPointer()
    val dataHolderPtr = dataHolder.let { if (it is Project) Pointer.hardPointer(it) else (it as PsiElement).createSmartPointer() }
    val project = this.project
    val proximity = this.proximity
    val key = this.key
    return Pointer {
      val container = containerPtr.dereference() ?: return@Pointer null
      val dataHolder = dataHolderPtr.dereference() ?: return@Pointer null
      VueCodeModelSymbolsContainer(container, project, dataHolder, proximity, key)
    }
  }

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(project).modificationCount +
    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS.modificationCount

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    val webTypesContributions = calculateWebTypesContributions()
    visitContainer(container, proximity, webTypesContributions, consumer)
    if (container is VueGlobal) {
      visitContainer(container.unregistered, VueModelVisitor.Proximity.OUT_OF_SCOPE, webTypesContributions, consumer)
    }
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  private fun visitContainer(container: VueEntitiesContainer,
                             forcedProximity: VueModelVisitor.Proximity,
                             webTypesContributions: MultiMap<WebTypesSymbolLocation, WebSymbol>,
                             consumer: (WebSymbol) -> Unit) {
    container.acceptEntities(object : VueModelVisitor() {

      override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
        component.asWebSymbol(name, forcedProximity)
          ?.tryMergeWithWebTypes(webTypesContributions)
          ?.let(consumer)
        return true
      }

      override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
        directive.asWebSymbol(name, forcedProximity)
          ?.tryMergeWithWebTypes(webTypesContributions)
          ?.let(consumer)
        return true
      }

    }, VueModelVisitor.Proximity.LOCAL)
  }

  private fun calculateWebTypesContributions(): MultiMap<WebTypesSymbolLocation, WebSymbol> {
    val registry = container.source
                     ?.let { WebSymbolsRegistryManager.get(it, false) }
                   ?: WebSymbolsRegistryManager.getInstance(project).get(null, false)
    val result = MultiMap.createLinkedSet<WebTypesSymbolLocation, WebSymbol>()
    registry.runNameMatchQuery(listOf(NAMESPACE_HTML, KIND_VUE_COMPONENTS), virtualSymbols = false)
      .asSequence().plus(registry.runNameMatchQuery(listOf(NAMESPACE_HTML, KIND_VUE_DIRECTIVES), virtualSymbols = false))
      .filterIsInstance<WebTypesSymbol>()
      .forEach { symbol ->
        when (val location = symbol.location) {
          is WebTypesSymbol.FileExport ->
            location.findFile()?.let {
              WebTypesSymbolLocation(
                it.url,
                location.symbolName,
                symbol.kind
              )
            }
          is WebTypesSymbol.ModuleExport ->
            WebTypesSymbolLocation(
              location.moduleName.lowercase(Locale.US),
              location.symbolName,
              symbol.kind)
          is WebTypesSymbol.FileOffset, null -> null
        }?.let {
          result.putValue(it, symbol)
        }
      }
    return result
  }

  private fun WebSymbol.tryMergeWithWebTypes(webTypesContributions: MultiMap<WebTypesSymbolLocation, WebSymbol>): WebSymbol {
    val source =
      (this as? VueDocumentedItemSymbol<*>)?.rawSource
        ?.let { source ->
          if (source is JSProperty)
            JSPsiImplUtils.getInitializerReference(source)?.let { JSStubBasedPsiTreeUtil.resolveLocally(it, source) }
          else source
        }
      ?: return this

    val locations =
      when (source) {
        is ES6ImportSpecifierAlias -> symbolLocationsFromSpecifier(source.findSpecifierElement() as? ES6ImportSpecifier, kind)
        is ES6ImportSpecifier -> symbolLocationsFromSpecifier(source, kind)
        is ES6ImportedBinding -> symbolLocationsForModule(source, source.declaration?.fromClause?.referenceText, "default", kind)
        is TypeScriptPropertySignature -> symbolLocationFromPropertySignature(source, kind)?.let { listOf(it) }
        else -> null
      } ?: emptyList()

    val toMerge = locations.flatMap { webTypesContributions[it] }
    if (toMerge.isNotEmpty())
      return VueWebTypesMergedSymbol(this, toMerge)
    return this
  }

  private fun symbolLocationsFromSpecifier(specifier: ES6ImportSpecifier?, symbolKind: String): List<WebTypesSymbolLocation> {
    if (specifier?.specifierKind == ES6ImportExportSpecifier.ImportExportSpecifierKind.IMPORT) {
      val symbolName = if (specifier.isDefault) "default" else specifier.referenceName
      val moduleName = specifier.declaration?.fromClause?.referenceText
      return symbolLocationsForModule(specifier, moduleName, symbolName, symbolKind)
    }
    return emptyList()
  }

  private fun symbolLocationsForModule(context: PsiElement,
                                       moduleName: String?,
                                       symbolName: String?,
                                       symbolKind: String): List<WebTypesSymbolLocation> =
    if (symbolName != null && moduleName != null) {
      val result = mutableListOf<WebTypesSymbolLocation>()
      val unquotedModule = StringUtil.unquoteString(moduleName)
      if (!unquotedModule.startsWith(".")) {
        result.add(WebTypesSymbolLocation(unquotedModule.lowercase(Locale.US), symbolName, symbolKind))
      }

      if (unquotedModule.contains('/')) {
        val modules = JSFileReferencesUtil.resolveModuleReference(context, unquotedModule)
        modules.mapNotNullTo(result) {
          it.containingFile?.originalFile?.virtualFile?.url?.let { url ->
            WebTypesSymbolLocation(url, symbolName, symbolKind)
          }
        }
        // A workaround to avoid full resolution in case of components in subpackages
        if (symbolName == "default"
            && !unquotedModule.startsWith(".")
            && unquotedModule.count { it == '/' } == 1) {

          modules.mapNotNullTo(result) {
            ES6PsiUtil.findDefaultExport(it)
              ?.asSafely<ES6ExportDefaultAssignment>()
              ?.initializerReference
              ?.let { symbolName ->
                WebTypesSymbolLocation(unquotedModule.takeWhile { it != '/' }, symbolName, symbolKind)
              }
          }
        }
      }
      result
    }
    else emptyList()

  private fun symbolLocationFromPropertySignature(property: TypeScriptPropertySignature, kind: SymbolKind): WebTypesSymbolLocation? {
    // TypeScript GlobalComponents definition
    val symbolName = property.memberName.takeIf { it.isNotEmpty() }
                     ?: return null

    // Locate module
    val packageName = property.containingFile?.originalFile?.virtualFile?.let { PackageJsonUtil.findUpPackageJson(it) }
                        ?.let { PackageJsonData.getOrCreate(it) }
                        ?.name
                      ?: return null

    return WebTypesSymbolLocation(packageName.lowercase(Locale.US), symbolName, kind)
  }

  private data class WebTypesSymbolLocation constructor(
    val moduleName: String,
    val symbolName: String,
    val symbolKind: String,
  )

}