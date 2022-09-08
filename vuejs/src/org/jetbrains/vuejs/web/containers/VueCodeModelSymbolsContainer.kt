// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.containers

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.web.symbols.SymbolKind
import com.intellij.javascript.web.symbols.WebSymbol
import com.intellij.javascript.web.symbols.WebSymbolsContainer.Companion.NAMESPACE_HTML
import com.intellij.javascript.web.symbols.WebSymbolsContainerWithCache
import com.intellij.javascript.web.symbols.WebSymbolsRegistryManager
import com.intellij.javascript.web.webTypes.WebTypesSymbol
import com.intellij.javascript.web.webTypes.json.SourceSymbol
import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifier
import com.intellij.lang.ecmascript6.psi.ES6ImportSpecifierAlias
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.MultiMap
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.web.VueFramework
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider.Companion.KIND_VUE_COMPONENTS
import org.jetbrains.vuejs.web.VueWebSymbolsAdditionalContextProvider.Companion.KIND_VUE_DIRECTIVES
import org.jetbrains.vuejs.web.asWebSymbol
import org.jetbrains.vuejs.web.symbols.VueDocumentedItemSymbol
import org.jetbrains.vuejs.web.symbols.VueWebTypesMergedSymbol

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
        (symbol.rawSource as? SourceSymbol)
          ?.let {
            WebTypesSymbolLocation(
              it.module ?: symbol.origin.packageName ?: return@let null,
              it.symbol ?: "default",
              symbol.kind)
          }?.let {
            result.putValue(it, symbol)
          }
      }
    return result
  }

  private fun WebSymbol.tryMergeWithWebTypes(webTypesContributions: MultiMap<WebTypesSymbolLocation, WebSymbol>): WebSymbol {
    val source = (this as? VueDocumentedItemSymbol<*>)?.rawSource ?: return this

    val location = when (source) {
      is ES6ImportSpecifierAlias -> symbolLocationFromSpecifier(source.findSpecifierElement() as? ES6ImportSpecifier, kind)
      is ES6ImportSpecifier -> symbolLocationFromSpecifier(source, kind)
      is TypeScriptPropertySignature -> symbolLocationFromPropertySignature(source, kind)
      else -> null
    }

    if (location != null) {
      val toMerge = webTypesContributions[location]
      if (toMerge.isNotEmpty())
        return VueWebTypesMergedSymbol(this, toMerge)
    }
    return this
  }

  private fun symbolLocationFromSpecifier(specifier: ES6ImportSpecifier?, kind: String): WebTypesSymbolLocation? {
    if (specifier?.specifierKind == ES6ImportExportSpecifier.ImportExportSpecifierKind.IMPORT) {
      val symbolName = if (specifier.isDefault) "default" else specifier.referenceName
      val moduleName = specifier.declaration?.fromClause?.referenceText
        ?.let { StringUtil.unquoteString(it) }
      if (symbolName != null && moduleName != null) {
        return WebTypesSymbolLocation(moduleName, symbolName, kind)
      }
    }
    return null
  }

  private fun symbolLocationFromPropertySignature(property: TypeScriptPropertySignature, kind: SymbolKind): WebTypesSymbolLocation? {
    // TypeScript GlobalComponents definition
    val symbolName = property.memberName.takeIf { it.isNotEmpty() }
                     ?: return null

    // Locate module
    val packageName = property.containingFile?.originalFile?.virtualFile?.let { PackageJsonUtil.findUpPackageJson(it) }
                        ?.let { PackageJsonData.getOrCreate(it) }
                        ?.name
                      ?: return null

    return WebTypesSymbolLocation(packageName, symbolName, kind)
  }

  private data class WebTypesSymbolLocation(
    val moduleName: String,
    val symbolName: String,
    val symbolKind: String,
  )

}