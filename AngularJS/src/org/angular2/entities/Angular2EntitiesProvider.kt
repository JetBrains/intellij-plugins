// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.JSImplicitElementProvider
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.ObjectUtils.tryCast
import com.intellij.util.SmartList
import com.intellij.util.containers.MultiMap
import com.intellij.util.containers.addIfNotNull
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_DEC
import org.angular2.Angular2DecoratorUtil.MODULE_DEC
import org.angular2.Angular2DecoratorUtil.PIPE_DEC
import org.angular2.Angular2DecoratorUtil.findDecorator
import org.angular2.Angular2DecoratorUtil.isAngularEntityDecorator
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.entities.Angular2EntityUtils.anyElementDirectiveIndexName
import org.angular2.entities.Angular2EntityUtils.getAttributeDirectiveIndexName
import org.angular2.entities.Angular2EntityUtils.getElementDirectiveIndexName
import org.angular2.entities.ivy.Angular2IvyUtil.getIvyEntity
import org.angular2.entities.ivy.Angular2IvyUtil.hasIvyMetadata
import org.angular2.entities.metadata.Angular2MetadataUtil
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase
import org.angular2.entities.metadata.psi.Angular2MetadataEntity
import org.angular2.entities.metadata.psi.Angular2MetadataModule
import org.angular2.entities.metadata.psi.Angular2MetadataPipe
import org.angular2.entities.source.*
import org.angular2.index.*
import org.angular2.index.Angular2IndexingHandler.Companion.NG_MODULE_INDEX_NAME
import org.angular2.index.Angular2IndexingHandler.Companion.isDirective
import org.angular2.index.Angular2IndexingHandler.Companion.isModule
import org.angular2.index.Angular2IndexingHandler.Companion.isPipe
import org.angular2.lang.Angular2LangUtil.isAngular2Context
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector
import org.angularjs.index.AngularIndexUtil
import java.util.function.Consumer

/**
 * @see Angular2ApplicableDirectivesProvider
 */
object Angular2EntitiesProvider {

  const val TRANSFORM_METHOD = "transform"

  @JvmStatic
  fun getEntity(element: PsiElement?): Angular2Entity? {
    if (element == null) {
      return null
    }
    val result = getSourceEntity(element)
    return result ?: withJsonMetadataFallback(element, { getIvyEntity(it) }, { Angular2MetadataUtil.getMetadataEntity(it) })
  }

  @JvmStatic
  fun <R, E : PsiElement> withJsonMetadataFallback(element: E,
                                                   ivy: (E) -> R,
                                                   jsonFallback: (TypeScriptClass) -> R): R? {
    val result = ivy(element)
    return if (result == null
               && element is TypeScriptClass
               && !hasIvyMetadata(element)
               && isAngular2Context(element)) {
      jsonFallback(element as TypeScriptClass)
    }
    else result
  }

  @JvmStatic
  fun getDeclaration(element: PsiElement?): Angular2Declaration? {
    return getEntity(element) as? Angular2Declaration
  }

  @JvmStatic
  fun getComponent(element: PsiElement?): Angular2Component? {
    return getEntity(element) as? Angular2Component
  }

  @JvmStatic
  fun getDirective(element: PsiElement?): Angular2Directive? {
    return getEntity(element) as? Angular2Directive
  }

  @JvmStatic
  fun getPipe(element: PsiElement?): Angular2Pipe? {
    val pipeClass = if (element is TypeScriptFunction
                        && TRANSFORM_METHOD == element.name
                        && element.context is TypeScriptClass) {
      element.context
    }
    else element
    return getEntity(pipeClass) as? Angular2Pipe
  }

  @JvmStatic
  fun getModule(element: PsiElement?): Angular2Module? {
    return getEntity(element) as? Angular2Module
  }

  @JvmStatic
  fun findElementDirectivesCandidates(project: Project, elementName: String): List<Angular2Directive> {
    return findDirectivesCandidates(project, getElementDirectiveIndexName(elementName))
  }

  @JvmStatic
  fun findAttributeDirectivesCandidates(project: Project,
                                        attributeName: String): List<Angular2Directive> {
    return findDirectivesCandidates(project, getAttributeDirectiveIndexName(attributeName))
  }

  @JvmStatic
  fun findPipes(project: Project, name: String): List<Angular2Pipe> {
    val result = SmartList<Angular2Pipe>()
    AngularIndexUtil.multiResolve(
      project, Angular2SourcePipeIndex.KEY, name) { pipe ->
      result.addIfNotNull(getSourceEntity(pipe) as? Angular2Pipe)
      true
    }
    processIvyEntities(project, name, Angular2IvyPipeIndex.KEY, Angular2Pipe::class.java) { result.add(it) }
    processMetadataEntities(project, name, Angular2MetadataPipe::class.java, Angular2MetadataPipeIndex.KEY) { result.add(it) }
    return result
  }

  @JvmStatic
  fun findDirectives(selector: Angular2DirectiveSelectorSymbol): List<Angular2Directive> {
    return when {
      selector.isElementSelector -> findElementDirectivesCandidates(selector.project, selector.name)
      selector.isAttributeSelector -> findAttributeDirectivesCandidates(selector.project, selector.name)
      else -> emptyList()
    }
  }

  @JvmStatic
  fun findComponent(selector: Angular2DirectiveSelectorSymbol): Angular2Component? {
    return findDirectives(selector).find { it.isComponent } as Angular2Component?
  }

  @JvmStatic
  fun getAllElementDirectives(project: Project): Map<String, List<Angular2Directive>> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      create(
        findDirectivesCandidates(project, anyElementDirectiveIndexName)
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
  }

  @JvmStatic
  fun getAllPipes(project: Project): Map<String, List<Angular2Pipe>> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      create(
        AngularIndexUtil.getAllKeys(Angular2SourcePipeIndex.KEY, project).asSequence()
          .plus(AngularIndexUtil.getAllKeys(Angular2MetadataPipeIndex.KEY, project))
          .plus(AngularIndexUtil.getAllKeys(Angular2IvyPipeIndex.KEY, project))
          .distinct()
          .associateBy({ it }, { findPipes(project, it) }),
        PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  @JvmStatic
  fun isPipeTransformMethod(element: PsiElement?): Boolean {
    return (element is TypeScriptFunction
            && TRANSFORM_METHOD == element.name
            && getPipe(element) != null)
  }

  @JvmStatic
  fun getExportedDeclarationToModuleMap(project: Project): MultiMap<Angular2Declaration, Angular2Module> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      val result = MultiMap<Angular2Declaration, Angular2Module>()
      getAllModules(project).forEach { module -> module.allExportedDeclarations.forEach { decl -> result.putValue(decl, module) } }
      create(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  @JvmStatic
  fun getDeclarationToModuleMap(project: Project): MultiMap<Angular2Declaration, Angular2Module> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      val result = MultiMap<Angular2Declaration, Angular2Module>()
      getAllModules(project).forEach { module -> module.declarations.forEach { decl -> result.putValue(decl, module) } }
      create(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  @JvmStatic
  fun getAllModules(project: Project): List<Angular2Module> {
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      val result = ArrayList<Angular2Module>()
      StubIndex.getInstance().processElements(Angular2SourceModuleIndex.KEY, NG_MODULE_INDEX_NAME,
                                              project, GlobalSearchScope.allScope(project),
                                              JSImplicitElementProvider::class.java) { module ->
        if (module.isValid) {
          result.addIfNotNull(getSourceEntity(module) as? Angular2Module)
        }
        true
      }
      processIvyEntities(project, NG_MODULE_INDEX_NAME, Angular2IvyModuleIndex.KEY, Angular2Module::class.java) { result.add(it) }
      processMetadataEntities(project, NG_MODULE_INDEX_NAME, Angular2MetadataModule::class.java,
                              Angular2MetadataModuleIndex.KEY) { result.add(it) }
      create<List<Angular2Module>>(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  @JvmStatic
  fun getSourceEntity(element: PsiElement): Angular2SourceEntity? {
    var elementToCheck: PsiElement? = element
    if (elementToCheck is JSImplicitElement) {
      if (!isEntityImplicitElement(elementToCheck)) {
        return null
      }
      elementToCheck = elementToCheck.getContext()
    }
    if (elementToCheck is TypeScriptClass) {
      elementToCheck = findDecorator(elementToCheck, PIPE_DEC, COMPONENT_DEC, MODULE_DEC, DIRECTIVE_DEC)
      if (elementToCheck == null) {
        return null
      }
    }
    else if (elementToCheck == null
             || elementToCheck !is ES6Decorator
             || !isAngularEntityDecorator(elementToCheck, PIPE_DEC, COMPONENT_DEC, MODULE_DEC, DIRECTIVE_DEC)) {
      return null
    }
    val dec = elementToCheck as ES6Decorator
    return CachedValuesManager.getCachedValue(dec) {
      val entity: Angular2SourceEntity? =
        dec.indexingData
          ?.implicitElements
          ?.find { isEntityImplicitElement(it) }
          ?.let { entityElement ->
            when (dec.decoratorName) {
              COMPONENT_DEC -> Angular2SourceComponent(dec, entityElement)
              DIRECTIVE_DEC -> Angular2SourceDirective(dec, entityElement)
              MODULE_DEC -> Angular2SourceModule(dec, entityElement)
              PIPE_DEC -> Angular2SourcePipe(dec, entityElement)
              else -> null
            }
          }
      create(entity, dec)
    }
  }

  @JvmStatic
  fun isDeclaredClass(typeScriptClass: TypeScriptClass): Boolean {
    return typeScriptClass.attributeList?.hasModifier(JSAttributeList.ModifierType.DECLARE) == true
  }

  private fun isEntityImplicitElement(element: JSImplicitElement): Boolean {
    return isDirective(element) || isPipe(element) || isModule(element)
  }

  private fun findDirectivesCandidates(project: Project, indexLookupName: String): List<Angular2Directive> {
    val result = ArrayList<Angular2Directive>()
    StubIndex.getInstance().processElements(
      Angular2SourceDirectiveIndex.KEY, indexLookupName, project, GlobalSearchScope.allScope(project), JSImplicitElementProvider::class.java
    ) { provider ->
      provider.indexingData
        ?.implicitElements
        ?.filter { it.isValid }
        ?.firstNotNullOfOrNull { getSourceEntity(it) as? Angular2Directive }
        ?.let { directive ->
          result.add(directive)
        }
      true
    }
    processIvyEntities(project, indexLookupName, Angular2IvyDirectiveIndex.KEY, Angular2Directive::class.java) { result.add(it) }
    processMetadataEntities(project, indexLookupName, Angular2MetadataDirectiveBase::class.java,
                            Angular2MetadataDirectiveIndex.KEY) { result.add(it) }
    return result
  }

  private fun <T : Angular2MetadataEntity<*>> processMetadataEntities(project: Project,
                                                                      name: String,
                                                                      entityClass: Class<T>,
                                                                      key: StubIndexKey<String, T>,
                                                                      consumer: Consumer<in T>) {
    StubIndex.getInstance().processElements(key, name, project, GlobalSearchScope.allScope(project), entityClass) { el ->
      if (el.isValid && !hasIvyMetadata(el)) {
        consumer.accept(el)
      }
      true
    }
  }

  private fun <T : Angular2Entity> processIvyEntities(project: Project,
                                                      name: String,
                                                      key: StubIndexKey<String, TypeScriptClass>,
                                                      entityClass: Class<T>,
                                                      consumer: Consumer<in T>) {
    StubIndex.getInstance().processElements(key, name, project, GlobalSearchScope.allScope(project), TypeScriptClass::class.java) { el ->
      if (el.isValid) {
        val entity = tryCast(getIvyEntity(el), entityClass)
        if (entity != null) {
          consumer.accept(entity)
        }
      }
      true
    }
  }
}
