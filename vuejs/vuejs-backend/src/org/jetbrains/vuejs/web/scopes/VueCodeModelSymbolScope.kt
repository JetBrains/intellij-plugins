// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.scopes

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
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolModifier
import com.intellij.polySymbols.query.PolySymbolQueryExecutorFactory
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.polySymbols.webTypes.WebTypesSymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.contextOfType
import com.intellij.util.asSafely
import com.intellij.util.containers.MultiMap
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.source.VueSourceEntity
import org.jetbrains.vuejs.web.*
import org.jetbrains.vuejs.web.symbols.VueSourceElementSymbol
import org.jetbrains.vuejs.web.symbols.VueWebTypesMergedSymbol
import java.util.*

class VueCodeModelSymbolScope<K>
private constructor(
  private val container: VueEntitiesContainer,
  project: Project,
  dataHolder: UserDataHolder,
  private val proximity: VueModelVisitor.Proximity,
  key: K,
) : PolySymbolScopeWithCache<UserDataHolder, K>(project, dataHolder, key) {

  companion object {

    fun create(container: VueEntitiesContainer, proximity: VueModelVisitor.Proximity): VueCodeModelSymbolScope<*>? {
      container.source
        ?.let {
          return VueCodeModelSymbolScope(container, it.project, it, proximity, proximity)
        }
      return if (container is VueGlobal)
        VueCodeModelSymbolScope(container, container.project, container.project, proximity, container.packageJsonUrl ?: "")
      else null
    }

  }

  override fun toString(): String {
    return "EntityContainerWrapper($container)"
  }

  override fun createPointer(): Pointer<VueCodeModelSymbolScope<K>> {
    val containerPtr = container.createPointer()
    val dataHolderPtr = dataHolder.let { if (it is Project) Pointer.hardPointer(it) else (it as PsiElement).createSmartPointer() }
    val project = this.project
    val proximity = this.proximity
    val key = this.key
    return Pointer {
      val container = containerPtr.dereference() ?: return@Pointer null
      val dataHolder = dataHolderPtr.dereference() ?: return@Pointer null
      VueCodeModelSymbolScope(container, project, dataHolder, proximity, key)
    }
  }

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(project).modificationCount +
    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS.modificationCount

  override fun provides(kind: PolySymbolKind): Boolean =
    kind == VUE_COMPONENTS
    || kind == VUE_GLOBAL_DIRECTIVES
    || kind == VUE_SCRIPT_SETUP_LOCAL_DIRECTIVES
    || kind == VUE_DIRECTIVES

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    val webTypesContributions = calculateWebTypesContributions()
    visitContainer(container, proximity, webTypesContributions, consumer)
    if (container is VueGlobal) {
      visitContainer(container.unregistered, VueModelVisitor.Proximity.OUT_OF_SCOPE, webTypesContributions, consumer)
    }
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
  }

  private fun visitContainer(
    container: VueEntitiesContainer,
    forcedProximity: VueModelVisitor.Proximity,
    webTypesContributions: MultiMap<WebTypesSymbolLocation, PolySymbol>,
    consumer: (PolySymbol) -> Unit,
  ) {
    container.acceptEntities(object : VueModelVisitor() {

      override fun visitComponent(name: String, component: VueComponent, proximity: Proximity): Boolean {
        component.asPolySymbol(name, forcedProximity)
          ?.tryMergeWithWebTypes(webTypesContributions)
          ?.forEach(consumer)
        return true
      }

      override fun visitDirective(name: String, directive: VueDirective, proximity: Proximity): Boolean {
        directive.asPolySymbol(name, forcedProximity)
          ?.tryMergeWithWebTypes(webTypesContributions)
          ?.forEach(consumer)
        return true
      }

    }, VueModelVisitor.Proximity.LOCAL)
  }

  private fun calculateWebTypesContributions(): MultiMap<WebTypesSymbolLocation, PolySymbol> {
    val registry = container.source
                     ?.let { PolySymbolQueryExecutorFactory.create(it, false) }
                   ?: PolySymbolQueryExecutorFactory.getInstance(project).create(null, false)
    val result = MultiMap.createLinkedSet<WebTypesSymbolLocation, PolySymbol>()
    registry.listSymbolsQuery(VUE_COMPONENTS, false)
      .exclude(PolySymbolModifier.ABSTRACT, PolySymbolModifier.VIRTUAL)
      .run()
      .asSequence().plus(
        registry.listSymbolsQuery(VUE_DIRECTIVES, false)
          .exclude(PolySymbolModifier.ABSTRACT, PolySymbolModifier.VIRTUAL)
          .run()
      )
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

  private fun PolySymbol.tryMergeWithWebTypes(webTypesContributions: MultiMap<WebTypesSymbolLocation, PolySymbol>): List<PolySymbol> {
    if (this !is VueSourceElementSymbol || this !is PsiSourcedPolySymbol) return listOf(this)

    val source =
      (this as? VueSourceEntity)
        ?.descriptor
        ?.source
        ?.let {
          it.contextOfType(ES6ExportDefaultAssignment::class)
          ?: it as? HtmlFileImpl
        }
      ?: ((this as? VueDirective)?.rawSource ?: (this as? VueComponent)?.rawSource ?: this.source)
        ?.let { source ->
          if (source is JSProperty)
            JSPsiImplUtils.getInitializerReference(source)?.let { JSStubBasedPsiTreeUtil.resolveLocally(it, source) }
          else source
        }
      ?: return listOf(this)


    val locations =
      when (source) {
        is ES6ImportSpecifierAlias -> symbolLocationsFromSpecifier(source.findSpecifierElement() as? ES6ImportSpecifier, kind)
        is ES6ImportSpecifier -> symbolLocationsFromSpecifier(source, kind)
        is ES6ImportedBinding -> symbolLocationsForModule(source, source.declaration?.fromClause?.referenceText, "default", kind)
        is TypeScriptPropertySignature -> symbolLocationFromPropertySignature(source, kind)?.let { listOf(it) }
        is ES6ExportDefaultAssignment, is HtmlFileImpl -> source.containingFile.virtualFile?.url?.let {
          listOf(WebTypesSymbolLocation(it, "default", kind))
        }
        else -> null
      } ?: emptyList()

    val toMerge = locations.flatMap { webTypesContributions[it] }
    return if (toMerge.isNotEmpty())
      if (source is ES6ExportDefaultAssignment || source is HtmlFileImpl) {
        // Merge with the source component - we need to merge both ways
        val names = toMerge.asSequence().map { it.name }.plus(this.name).toSet()
        names.map { VueWebTypesMergedSymbol(it, this, toMerge) }
      }
      else
        listOf(VueWebTypesMergedSymbol(this.name, this, toMerge))
    else
      listOf(this)
  }

  private fun symbolLocationsFromSpecifier(specifier: ES6ImportSpecifier?, symbolKind: PolySymbolKind): List<WebTypesSymbolLocation> {
    if (specifier?.specifierKind == ES6ImportExportSpecifier.ImportExportSpecifierKind.IMPORT) {
      val symbolName = if (specifier.isDefault) "default" else specifier.referenceName
      val moduleName = specifier.declaration?.fromClause?.referenceText
      return symbolLocationsForModule(specifier, moduleName, symbolName, symbolKind)
    }
    return emptyList()
  }

  private fun symbolLocationsForModule(
    context: PsiElement,
    moduleName: String?,
    symbolName: String?,
    symbolKind: PolySymbolKind,
  ): List<WebTypesSymbolLocation> =
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

  private fun symbolLocationFromPropertySignature(
    property: TypeScriptPropertySignature,
    kind: PolySymbolKind,
  ): WebTypesSymbolLocation? {
    if (!property.isValid) return null

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

  private data class WebTypesSymbolLocation(
    val moduleName: String,
    val symbolName: String,
    val symbolKind: PolySymbolKind,
  )

}