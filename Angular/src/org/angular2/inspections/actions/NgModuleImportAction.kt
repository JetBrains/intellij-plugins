// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions

import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.PsiElement
import com.intellij.util.containers.MultiMap
import one.util.streamex.IntStreamEx
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.imports.Angular2ImportsHandler
import org.angular2.codeInsight.imports.Angular2ModuleImportCandidate
import org.angular2.editor.scheduleDelayedAutoPopupIfNeeded
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2Module
import org.angular2.entities.metadata.psi.Angular2MetadataModule
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.ApiStatus.Internal

class NgModuleImportAction internal constructor(editor: Editor?,
                                                element: PsiElement,
                                                @NlsContexts.Command actionName: String,
                                                codeCompletion: Boolean) //NON-NLS
  : Angular2NgModuleSelectAction(editor, element, "NgModule", actionName, codeCompletion) {

  override fun getModuleSelectionPopupTitle(): String {
    return Angular2Bundle.message("angular.quickfix.ngmodule.import.select.module")
  }

  override fun getRawCandidates(): List<JSImportCandidate> {

    val candidates = Angular2FixesFactory.getCandidatesForResolution(context, myCodeCompletion)
    if (!candidates.get(Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE).isEmpty()) {
      return emptyList()
    }

    val importableDeclarations = candidates.get(Angular2DeclarationsScope.DeclarationProximity.IMPORTABLE)
    val scope = Angular2DeclarationsScope(context)
    return declarationsToModuleImports(context, importableDeclarations, scope)
  }

  override fun runAction(editor: Editor?,
                         candidate: JSImportCandidateWithExecutor,
                         place: PsiElement) {
    val scope = Angular2DeclarationsScope(context)
    val importsOwner = scope.importsOwner
    if (importsOwner == null || !scope.isInSource(importsOwner)) {
      return
    }
    Angular2ImportsHandler.getFor(importsOwner)
      .insertImport(editor, candidate, importsOwner)
    if (myCodeCompletion) scheduleDelayedAutoPopupIfNeeded(editor, myPlaceInfo.place)
  }

  companion object {

    @Internal
    fun declarationsToModuleImports(context: PsiElement,
                                    declarations: Collection<Angular2Declaration>,
                                    scope: Angular2DeclarationsScope): List<Angular2ModuleImportCandidate> {
      val distanceCalculator = DistanceCalculator()
      val moduleToDeclarationDistances = MultiMap<Angular2Module, Int>()
      declarations.forEach { declaration ->
        scope.getPublicModulesExporting(declaration)
          .distinct()
          .forEach { module ->
            moduleToDeclarationDistances.putValue(module, distanceCalculator.get(module, declaration))
          }
      }
      val averageDistances = HashMap<Angular2Entity, Double>()
      for ((key, value) in moduleToDeclarationDistances.entrySet()) {
        averageDistances[key] = IntStreamEx.of(value).average().orElse(0.0)
      }

      for (declaration in declarations) {
        if (declaration.isStandalone) {
          averageDistances[declaration] = -0.5
        }
      }

      return averageDistances.keys
        .asSequence()
        .sortedBy { averageDistances[it] }
        .mapNotNull {
          val entitySource = it.entitySource ?: return@mapNotNull null
          val name = detectName(entitySource) ?: return@mapNotNull null
          Angular2ModuleImportCandidate(name, entitySource, context)
        }
        .toList()
    }

    private fun detectName(element: PsiElement?): String? {
      if (element == null) return null
      val entityToImport = Angular2EntitiesProvider.getEntity(element) ?: return null

      return if (entityToImport is Angular2MetadataModule)  // metadata does not support standalone declarations
        entityToImport.stub.memberName ?: entityToImport.name
      else
        entityToImport.entitySourceName
    }

  }


  private class DistanceCalculator {

    fun get(module: Angular2Module, declaration: Angular2Declaration): Int {
      // For now very simple condition, if that's not enough we can
      // improve algorithm by providing proper distance calculations.
      return if (module.exports.contains(declaration)) 0 else 1
    }
  }
}
