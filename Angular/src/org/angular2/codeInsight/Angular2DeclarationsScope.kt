// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.MultiMap
import org.angular2.entities.*

/**
 * Objects of this class should not be cached or stored. It is intended for single use.
 */
class Angular2DeclarationsScope {

  constructor(element: PsiElement) {
    this.location = element
    this.scope = createScope(element)
    this.fileIndex = NotNullLazyValue.createValue { ProjectRootManager.getInstance(element.project).fileIndex }
  }

  constructor(component: Angular2Component) {
    this.location = component.templateFile
    this.scope = createScope(location)
    this.fileIndex = NotNullLazyValue.createValue { ProjectRootManager.getInstance(component.sourceElement.project).fileIndex }
  }

  private val location: PsiElement?
  private val scope: NotNullLazyValue<ScopeResult>
  private val export2NgModuleMap: HashMap<Project, MultiMap<Angular2Declaration, Angular2Module>> = HashMap()
  private val fileIndex: NotNullLazyValue<ProjectFileIndex>

  val importsOwner: Angular2ImportsOwner?
    get() = scope.value.owner

  val isFullyResolved: Boolean
    get() = scope.value.fullyResolved

  fun <T : Angular2Declaration> getClosestDeclaration(declarations: Collection<T>): Pair<T, DeclarationProximity>? {
    return declarations.minOfWithOrNull(Comparator.comparing { it.second }) { Pair(it, getDeclarationProximity(it)) }
  }

  operator fun contains(declaration: Angular2Declaration): Boolean {
    val declarations = scope.value.declarations
    return declarations == null || declarations.contains(declaration)
  }

  fun getPublicModulesExporting(declaration: Angular2Declaration): List<Angular2Module> {
    return export2NgModuleMap
      .computeIfAbsent(declaration.sourceElement.project) {
        Angular2EntitiesProvider.getExportedDeclarationToModuleMap(location ?: declaration.sourceElement)
      }
      .get(declaration)
      .filter { it.isPublic && it.entitySource != null && it.isStandalonePseudoModule == declaration.isStandalone }
  }

  fun getDeclarationProximity(declaration: Angular2Declaration): DeclarationProximity {
    if (contains(declaration)) {
      return DeclarationProximity.IN_SCOPE
    }

    if (declaration.isStandalone) {
      return DeclarationProximity.IMPORTABLE
    }

    val modules = export2NgModuleMap.computeIfAbsent(declaration.sourceElement.project) {
      Angular2EntitiesProvider.getExportedDeclarationToModuleMap(location ?: declaration.sourceElement)
    }.get(declaration)
    if (modules.isEmpty()) {
      if (!isInSource(declaration)) {
        return DeclarationProximity.NOT_REACHABLE
      }
      return if (declaration.allDeclaringModules.isEmpty())
        DeclarationProximity.NOT_DECLARED_IN_ANY_MODULE
      else
        DeclarationProximity.NOT_EXPORTED_BY_MODULE
    }
    else if (modules.any { it.isPublic }) {
      return DeclarationProximity.IMPORTABLE
    }
    return DeclarationProximity.NOT_REACHABLE
  }

  fun getDeclarationsProximity(declarations: Iterable<Angular2Declaration>): DeclarationProximity {
    var result = DeclarationProximity.NOT_REACHABLE
    for (declaration in declarations) {
      val current = getDeclarationProximity(declaration)
      if (current == DeclarationProximity.IN_SCOPE) {
        return DeclarationProximity.IN_SCOPE
      }
      if (current.ordinal < result.ordinal) {
        result = current
      }
    }
    return result
  }

  fun isInSource(entity: Angular2Entity): Boolean {
    if (!entity.isModifiable) return false
    val file = entity.sourceElement.containingFile ?: return false
    val vf = file.viewProvider.virtualFile
    return fileIndex.value.isInContent(vf) && !fileIndex.value.isInLibrary(vf)
  }

  enum class DeclarationProximity {
    IN_SCOPE,
    IMPORTABLE, // standalone or exported by public module
    NOT_DECLARED_IN_ANY_MODULE,
    NOT_EXPORTED_BY_MODULE,
    NOT_REACHABLE
  }

  companion object {

    private fun createScope(element: PsiElement?) = NotNullLazyValue.createValue {
      val file = element?.containingFile
                 ?: return@createValue ScopeResult(null, null, false)
      CachedValuesManager.getCachedValue(file) {
        val importsOwner = Angular2EntitiesProvider.findTemplateComponent(file)
          ?.let { selectImportsOwner(it, file) }
        val result = ScopeResult(importsOwner, importsOwner?.declarationsInScope, importsOwner?.isScopeFullyResolved ?: false)
        CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
      }
    }

    private fun selectImportsOwner(component: Angular2Component, context: PsiFile): Angular2ImportsOwner? {
      if (component.isStandalone) return component
      val modules = component.allDeclaringModules
      if (modules.size > 1) {
        for (handler in Angular2FrameworkHandler.EP_NAME.extensionList) {
          val result = handler.selectModuleForDeclarationsScope(modules, component, context)
          if (result != null) {
            return result
          }
        }
        return Angular2EntityUtils.defaultChooseModule(modules)
      }
      return modules.firstOrNull()
    }
  }


  private data class ScopeResult(val owner: Angular2ImportsOwner?, val declarations: Set<Angular2Declaration>?, val fullyResolved: Boolean)
}
