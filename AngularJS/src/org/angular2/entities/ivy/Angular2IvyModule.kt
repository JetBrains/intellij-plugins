// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import com.intellij.javascript.nodejs.library.node_modules.NodeModulesDirectoryManager
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.types.TypeScriptTypeOfJSTypeImpl.getTypeOfResultElements
import com.intellij.model.Pointer
import com.intellij.psi.util.CachedValueProvider.Result
import org.angular2.entities.*
import org.angular2.entities.Angular2ModuleResolver.ResolvedEntitiesList

class Angular2IvyModule(entityDef: Angular2IvySymbolDef.Module)
  : Angular2IvyEntity<Angular2IvySymbolDef.Module>(entityDef), Angular2Module {

  private val myModuleResolver = Angular2ModuleResolver({ field }, symbolCollector)

  override val declarations: Set<Angular2Declaration>
    get() = myModuleResolver.declarations

  override val imports: Set<Angular2Entity>
    get() = myModuleResolver.imports

  override val exports: Set<Angular2Entity>
    get() = myModuleResolver.exports

  override val allExportedDeclarations: Set<Angular2Declaration>
    get() = myModuleResolver.allExportedDeclarations

  override val isScopeFullyResolved: Boolean
    get() = myModuleResolver.isScopeFullyResolved

  override val isPublic: Boolean
    get() = !getName().startsWith("ɵ")

  override fun areExportsFullyResolved(): Boolean {
    return myModuleResolver.areExportsFullyResolved()
  }

  override fun createPointer(): Pointer<Angular2IvyModule> {
    val source = myEntityDef.createPointer()
    return Pointer {
      source.dereference()?.let { Angular2IvyModule(it) }
    }
  }

  override fun areDeclarationsFullyResolved(): Boolean {
    return myModuleResolver.areDeclarationsFullyResolved()
  }

  companion object {

    private val symbolCollector = object : Angular2ModuleResolver.SymbolCollector<TypeScriptField> {
      override fun <U : Angular2Entity> collect(source: TypeScriptField,
                                                propertyName: String,
                                                symbolClazz: Class<U>): Result<ResolvedEntitiesList<U>> =
        collectSymbols(source, propertyName, symbolClazz)
    }

    private fun <T : Angular2Entity> collectSymbols(fieldDef: TypeScriptField,
                                                    propertyName: String,
                                                    symbolClazz: Class<T>): Result<ResolvedEntitiesList<T>> {
      val moduleDef = Angular2IvySymbolDef.get(fieldDef, false) as? Angular2IvySymbolDef.Module
      val types = moduleDef?.getTypesList(propertyName) ?: emptyList()
      if (types.isEmpty()) {
        return ResolvedEntitiesList.createResult(emptySet(), true, fieldDef)
      }
      val entities = HashSet<T>()
      var fullyResolved = true
      // Dependencies for the cache are calculated heuristically.
      // As dependencies, we use source and target files, however we miss any files in between.
      // To compensate, we depend on any changes in node_modules.
      // This approach is correct in 95% of cases, but gives huge boost to performance.
      val dependencies = HashSet<Any>()
      dependencies.add(fieldDef.containingFile)
      dependencies.add(NodeModulesDirectoryManager.getInstance(fieldDef.project).nodeModulesDirChangeTracker)
      for (typeOfType in types) {
        val reference = typeOfType.referenceText
        if (reference == null) {
          fullyResolved = false
          continue
        }
        val resolvedTypes = getTypeOfResultElements(typeOfType, reference)
        resolvedTypes.forEach { type -> dependencies.add(type.containingFile) }
        val entity = resolvedTypes
          .map { el -> Angular2EntitiesProvider.getEntity(el) }
          .filterIsInstance(symbolClazz)
          .firstOrNull()
        if (entity == null) {
          fullyResolved = false
        }
        else {
          entities.add(entity)
        }
      }
      return ResolvedEntitiesList.createResult(entities, fullyResolved, dependencies)
    }
  }
}
