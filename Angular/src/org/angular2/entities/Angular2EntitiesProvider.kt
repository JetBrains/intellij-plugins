// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecma6.TypeScriptInterfaceClass
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.contextOfType
import com.intellij.util.SmartList
import com.intellij.util.containers.MultiMap
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.entities.Angular2EntityUtils.anyElementDirectiveIndexName
import org.angular2.entities.Angular2EntityUtils.getAttributeDirectiveIndexName
import org.angular2.entities.Angular2EntityUtils.getElementDirectiveIndexName
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector
import java.util.concurrent.ConcurrentHashMap

/**
 * @see Angular2ApplicableDirectivesProvider
 */
object Angular2EntitiesProvider {

  private val EP_NAME = ExtensionPointName<Angular2EntitiesSource>("org.angular2.entitiesSource")
  private val entitySources get() = EP_NAME.extensionList.asSequence()

  const val TRANSFORM_METHOD: String = "transform"

  @JvmStatic
  fun getEntity(element: PsiElement?): Angular2Entity? =
    if (element == null || !isSupportedEntityClass(element::class.java))
      null
    else
      entitySources.firstNotNullOfOrNull { it.getEntity(element) }

  @JvmStatic
  fun getDeclaration(element: PsiElement?): Angular2Declaration? =
    getEntity(element) as? Angular2Declaration

  @JvmStatic
  fun getComponent(element: PsiElement?): Angular2Component? =
    getEntity(element) as? Angular2Component

  @JvmStatic
  fun getDirective(element: PsiElement?): Angular2Directive? =
    getEntity(element) as? Angular2Directive

  @JvmStatic
  fun getPipe(element: PsiElement?): Angular2Pipe? {
    val pipeClass = if (element is TypeScriptFunction && TRANSFORM_METHOD == element.name) {
      element.context as? TypeScriptClass
    }
    else if (element is TypeScriptField && TRANSFORM_METHOD == element.name) {
      element.contextOfType(TypeScriptInterfaceClass::class)
    }
    else
      element
    return getEntity(pipeClass) as? Angular2Pipe
  }

  @JvmStatic
  fun getModule(element: PsiElement?): Angular2Module? =
    getEntity(element) as? Angular2Module

  @JvmStatic
  fun findElementDirectivesCandidates(project: Project, elementName: String): List<Angular2Directive> =
    findDirectivesCandidates(project, getElementDirectiveIndexName(elementName))

  @JvmStatic
  fun findAttributeDirectivesCandidates(
    project: Project,
    attributeName: String,
  ): List<Angular2Directive> =
    findDirectivesCandidates(project, getAttributeDirectiveIndexName(attributeName))

  @JvmStatic
  fun findTemplateComponent(templateContext: PsiElement): Angular2Component? =
    entitySources.firstNotNullOfOrNull { it.findTemplateComponent(templateContext) }

  @JvmStatic
  fun findPipes(project: Project, name: String): List<Angular2Pipe> =
    entitySources.flatMap { it.findPipes(project, name) }.toList()

  @JvmStatic
  fun getAllElementDirectives(project: Project): Map<String, List<Angular2Directive>> =
    CachedValuesManager.getManager(project).getCachedValue(project) {
      create(
        findDirectivesCandidates(project, anyElementDirectiveIndexName)
          .distinct()
          .flatMap { directive ->
            val result = SmartList<Pair<String, Angular2Directive>>()
            val selectorProcessor = { sel: Angular2DirectiveSimpleSelector ->
              val elementName = sel.elementName
              if (!StringUtil.isEmpty(elementName) && "*" != elementName) {
                result.add(Pair.pair(elementName, directive))
              }
            }
            for (sel in directive.selector.simpleSelectors) {
              selectorProcessor(sel)
              sel.notSelectors.forEach(selectorProcessor)
            }
            result
          }
          .groupBy({ it.first }, { it.second }),
        PsiModificationTracker.MODIFICATION_COUNT)
    }

  @JvmStatic
  fun getAllPipes(project: Project): Map<String, List<Angular2Pipe>> =
    CachedValuesManager.getManager(project).getCachedValue(project) {
      create(
        entitySources
          .flatMap { it.getAllPipeNames(project) }
          .distinct()
          .associateBy({ it }, { findPipes(project, it) }),
        PsiModificationTracker.MODIFICATION_COUNT)
    }

  @JvmStatic
  fun isPipeTransformMethod(element: PsiElement?): Boolean =
    (element is TypeScriptFunction
     && TRANSFORM_METHOD == element.name
     && getPipe(element) != null)

  @JvmStatic
  fun getExportedDeclarationToModuleMap(location: PsiElement): MultiMap<Angular2Declaration, Angular2Module> {
    val project = location.project
    return JSTypeEvaluationLocationProvider.getCachedValueOnCurrentTsConfig(location) {
      val result = MultiMap<Angular2Declaration, Angular2Module>()
      getAllModules(project).forEach { module -> module.allExportedDeclarations.forEach { decl -> result.putValue(decl, module) } }
      create(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  @JvmStatic
  fun getDeclarationToModuleMap(location: PsiElement): MultiMap<Angular2Declaration, Angular2Module> {
    val project = location.project
    return JSTypeEvaluationLocationProvider.getCachedValueOnCurrentTsConfig(location) {
      val result = MultiMap<Angular2Declaration, Angular2Module>()
      getAllModules(project).forEach { module -> module.declarations.forEach { decl -> result.putValue(decl, module) } }
      create(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  @JvmStatic
  fun getAllModules(project: Project): List<Angular2Module> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      create(entitySources.flatMap { it.getAllModules(project) }.toList(),
             PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  @JvmStatic
  fun isDeclaredClass(typeScriptClass: TypeScriptClass): Boolean {
    return typeScriptClass.attributeList?.hasModifier(JSAttributeList.ModifierType.DECLARE) == true
  }

  private fun findDirectivesCandidates(project: Project, indexLookupName: String): List<Angular2Directive> =
    CachedValuesManager.getManager(project).getCachedValue(project) {
      create(ConcurrentHashMap<String, List<Angular2Directive>>(),
             PsiModificationTracker.MODIFICATION_COUNT,
             VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)
    }.computeIfAbsent(indexLookupName) {
      entitySources.flatMap { it.findDirectiveCandidates(project, indexLookupName) }.toList()
    }

  private fun isSupportedEntityClass(cls: Class<out PsiElement>): Boolean {
    val supportedClasses = EP_NAME.computeIfAbsent(Angular2EntitiesProvider::class.java) {
      EP_NAME.extensionList.flatMap { it.getSupportedEntityPsiElements() }.toSet()
    }
    return supportedClasses.any { it.isAssignableFrom(cls) }
  }
}
