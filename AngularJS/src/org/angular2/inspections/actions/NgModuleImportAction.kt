// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions

import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.modules.imports.ES6ImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.PsiElement
import com.intellij.util.containers.MultiMap
import one.util.streamex.IntStreamEx
import org.angular2.Angular2DecoratorUtil.IMPORTS_PROP
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2Entity
import org.angular2.entities.Angular2Module
import org.angular2.entities.metadata.psi.Angular2MetadataModule
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil
import org.angular2.lang.Angular2Bundle

class NgModuleImportAction internal constructor(editor: Editor?,
                                                element: PsiElement,
                                                @NlsContexts.Command actionName: String,
                                                codeCompletion: Boolean) //NON-NLS
  : Angular2NgModuleSelectAction(editor, element, "NgModule", actionName, codeCompletion) {

  override fun getModuleSelectionPopupTitle(): String {
    return Angular2Bundle.message("angular.quickfix.ngmodule.import.select.module")
  }

  override fun getRawCandidates(): List<JSImportCandidate> {
    val distanceCalculator = DistanceCalculator()
    val scope = Angular2DeclarationsScope(context)

    val candidates = Angular2FixesFactory.getCandidatesForResolution(context, myCodeCompletion)
    if (!candidates.get(Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE).isEmpty()) {
      return emptyList()
    }

    val moduleToDeclarationDistances = MultiMap<Angular2Module, Int>()
    val importableDeclarations = candidates.get(Angular2DeclarationsScope.DeclarationProximity.IMPORTABLE)
    importableDeclarations.forEach { declaration ->
      if (!declaration.isStandalone)
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

    for (declaration in importableDeclarations) {
      if (declaration.isStandalone) {
        averageDistances[declaration] = 0.0
      }
    }

    return averageDistances.keys
      .asSequence()
      .sortedBy { averageDistances[it] }
      .mapNotNull {
        val cls = it.typeScriptClass ?: return@mapNotNull null
        val name = detectName(cls) ?: return@mapNotNull null
        ES6ImportCandidate(name, cls, context)
      }
      .toList()
  }

  private fun detectName(element: PsiElement?): String? {
    if (element == null) return null
    val entityToImport = Angular2EntitiesProvider.getEntity(element) ?: return null

    return if (entityToImport is Angular2MetadataModule) { // metadata does not support standalone declarations
      entityToImport.stub.memberName ?: entityToImport.name
    }
    else entityToImport.className
  }

  override fun runAction(editor: Editor?,
                         candidate: JSImportCandidateWithExecutor,
                         place: PsiElement) {
    val element = candidate.element ?: return
    val scope = Angular2DeclarationsScope(context)
    val importsOwner = scope.importsOwner
    if (importsOwner == null || !scope.isInSource(importsOwner)) {
      return
    }
    val destinationModuleClass = importsOwner.typeScriptClass

    if (destinationModuleClass == null || importsOwner.decorator == null) {
      return
    }

    val name = candidate.name
    WriteAction.run<RuntimeException> {
      ES6ImportPsiUtil.insertJSImport(destinationModuleClass, name, element, editor)
      Angular2FixesPsiUtil.insertEntityDecoratorMember(importsOwner, IMPORTS_PROP, name)
      // TODO support NgModuleWithProviders static methods
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
