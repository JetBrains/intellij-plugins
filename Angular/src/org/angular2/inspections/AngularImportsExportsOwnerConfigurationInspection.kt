// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSRecursiveWalkingElementVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlRecursiveElementWalkingVisitor
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.util.containers.Stack
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.DECLARATIONS_PROP
import org.angular2.Angular2DecoratorUtil.EXPORTS_PROP
import org.angular2.Angular2DecoratorUtil.IMPORTS_PROP
import org.angular2.Angular2DecoratorUtil.MODULE_DEC
import org.angular2.Angular2DecoratorUtil.isAngularEntityDecorator
import org.angular2.Angular2InjectionUtils
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.Angular2HighlightingUtils.htmlClassName
import org.angular2.codeInsight.Angular2HighlightingUtils.htmlLabel
import org.angular2.codeInsight.Angular2HighlightingUtils.htmlName
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.entities.*
import org.angular2.inspections.Angular2SourceEntityListValidator.ValidationResults
import org.angular2.inspections.quickfixes.ConvertToStandaloneNonStandaloneQuickFix
import org.angular2.inspections.quickfixes.MoveDeclarationOfStandaloneToImportsQuickFix
import org.angular2.inspections.quickfixes.RemoveEntityImportQuickFix
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.html.psi.Angular2HtmlBoundAttribute

abstract class AngularImportsExportsOwnerConfigurationInspection protected constructor(private val myProblemType: ProblemType) : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : JSElementVisitor() {
      override fun visitES6Decorator(decorator: ES6Decorator) {
        getValidationResults(decorator).registerProblems(myProblemType, holder)
      }
    }
  }

  protected enum class ProblemType {
    ENTITY_WITH_MISMATCHED_TYPE,
    RECURSIVE_IMPORT_EXPORT,
    UNDECLARED_EXPORT,
    UNUSED_IMPORT,
  }

  private class DeclarationsValidator(
    decorator: ES6Decorator,
    results: ValidationResults<ProblemType>,
  ) : Angular2SourceEntityListValidator<Angular2Declaration, ProblemType>(
    decorator, results, Angular2Declaration::class.java, DECLARATIONS_PROP) {

    override fun processAcceptableEntity(entity: Angular2Declaration) {
      if (entity.isStandalone) {
        registerProblem(
          ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
          Angular2Bundle.htmlMessage("angular.inspection.wrong-entity-type.message.standalone-declarable", entity.htmlLabel),
          *listOfNotNull(
            entity.asSafely<Angular2ClassBasedEntity>()?.let { MoveDeclarationOfStandaloneToImportsQuickFix(it.className) },
            entity.asSafely<Angular2ClassBasedEntity>()?.let { ConvertToStandaloneNonStandaloneQuickFix(it.className, false) },
          ).toTypedArray()
        )
      }
    }

    override fun processNonAcceptableEntityClass(aClass: JSClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      Angular2Bundle.htmlMessage("angular.inspection.wrong-entity-type.message.not-declarable", aClass.htmlName),
                      RemoveEntityImportQuickFix(aClass.name))
    }
  }

  private class UnusedComponentImportsValidator(
    decorator: ES6Decorator,
    results: ValidationResults<ProblemType>,
    component: Angular2Component,
  ) : Angular2SourceEntityListValidator<Angular2Entity, ProblemType>(
    decorator, results, Angular2Entity::class.java, IMPORTS_PROP) {

    private val usedEntities = collectUsedDeclarations(component)

    override fun processAcceptableEntity(entity: Angular2Entity) {
      if (entity is Angular2Declaration && entity.isStandalone && !usedEntities.contains(entity)) {
        registerProblem(ProblemType.UNUSED_IMPORT,
                        Angular2Bundle.htmlMessage("angular.inspection.unused-component-import.declaration.message", entity.htmlLabel),
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        RemoveEntityImportQuickFix(entity.entitySourceName))
      }
      else if (entity is Angular2Module && entity.isStandalonePseudoModule
               && entity.allExportedDeclarations.none { usedEntities.contains(it) }
      ) {
        registerProblem(ProblemType.UNUSED_IMPORT,
                        Angular2Bundle.htmlMessage("angular.inspection.unused-component-import.pseudo-module.message", entity.htmlLabel),
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        RemoveEntityImportQuickFix(entity.entitySourceName))
      }
    }
  }

  private abstract class ImportExportValidator<T : Angular2Entity> protected constructor(
    decorator: ES6Decorator,
    results: ValidationResults<ProblemType>,
    entityClass: Class<T>,
    propertyName: String,
    protected val importsOwner: Angular2ImportsOwner,
  ) : Angular2SourceEntityListValidator<T, ProblemType>(decorator, results, entityClass, propertyName) {

    protected fun checkCyclicDependencies(owner: Angular2ImportsOwner) {
      val cycleTrack = Stack<Angular2ImportsOwner>()
      val processedContainers = HashSet<Angular2ImportsOwner>()
      val dfsStack = Stack<MutableList<Angular2ImportsOwner>>()

      cycleTrack.push(this.importsOwner)
      dfsStack.push(SmartList(owner))
      while (!dfsStack.isEmpty()) {
        val curNode = dfsStack.peek()
        if (curNode.isEmpty()) {
          dfsStack.pop()
          cycleTrack.pop()
        }
        else {
          val toProcess = curNode.removeAt(curNode.size - 1)
          if (toProcess === this.importsOwner) {
            cycleTrack.push(this.importsOwner)
            registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT, Angular2Bundle.htmlMessage(
              "angular.inspection.cyclic-module-dependency.message.cycle",
              cycleTrack.joinToString(
                " " + Angular2Bundle.message(
                  "angular.inspection.cyclic-module-dependency.message.separator") + " ") { it.htmlClassName }
            ))
            return
          }
          if (processedContainers.add(toProcess)) {
            cycleTrack.push(toProcess)
            val dependencies = ArrayList<Angular2ImportsOwner>()
            toProcess.asSafely<Angular2Module>()
              ?.exports?.filterIsInstance<Angular2ImportsOwner>()?.forEach { dependencies.add(it) }
            toProcess.imports.filterIsInstance<Angular2ImportsOwner>().forEach { dependencies.add(it) }
            dfsStack.push(dependencies)
          }
        }
      }
    }
  }

  private class ImportsValidator(
    decorator: ES6Decorator,
    results: ValidationResults<ProblemType>,
    importsOwner: Angular2ImportsOwner,
  ) : ImportExportValidator<Angular2Entity>(decorator, results, Angular2Entity::class.java, IMPORTS_PROP, importsOwner) {

    override fun processNonAcceptableEntityClass(aClass: JSClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      Angular2Bundle.htmlMessage("angular.inspection.wrong-entity-type.message.not-importable", aClass.htmlName),
                      RemoveEntityImportQuickFix(aClass.name))
    }

    override fun processAcceptableEntity(entity: Angular2Entity) {
      if (entity == importsOwner) {
        registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT,
                        Angular2Bundle.htmlMessage("angular.inspection.cyclic-module-dependency.message.self-import", entity.htmlLabel))
      }
      else if (entity is Angular2Module || (entity is Angular2Component && entity.isStandalone)) {
        checkCyclicDependencies(entity as Angular2ImportsOwner)
      }
      else if (!Angular2EntityUtils.isImportableEntity(entity)) {
        registerProblem(
          ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
          Angular2Bundle.htmlMessage("angular.inspection.wrong-entity-type.message.not-standalone", entity.htmlLabel),
          *listOfNotNull(entity.asSafely<Angular2ClassBasedEntity>()?.let { ConvertToStandaloneNonStandaloneQuickFix(it.className, true) })
            .toTypedArray()
        )
      }
    }
  }

  private class ExportsValidator(
    decorator: ES6Decorator,
    results: ValidationResults<ProblemType>,
    module: Angular2Module,
  ) : ImportExportValidator<Angular2Entity>(decorator, results, Angular2Entity::class.java, EXPORTS_PROP, module) {

    override fun processNonAcceptableEntityClass(aClass: JSClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      Angular2Bundle.htmlMessage("angular.inspection.wrong-entity-type.message.not-entity", aClass.htmlName),
                      if (importsOwner.isScopeFullyResolved)
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                      else
                        ProblemHighlightType.WEAK_WARNING,
                      RemoveEntityImportQuickFix(aClass.name))
    }

    override fun processAcceptableEntity(entity: Angular2Entity) {
      if (entity is Angular2Module) {
        if (entity == importsOwner) {
          registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT,
                          Angular2Bundle.htmlMessage("angular.inspection.cyclic-module-dependency.message.self-export", entity.htmlLabel))
        }
        else {
          checkCyclicDependencies(entity)
        }
      }
      else if (entity is Angular2Declaration) {
        if (!importsOwner.declarationsInScope.contains(entity)) {
          registerProblem(ProblemType.UNDECLARED_EXPORT,
                          Angular2Bundle.htmlMessage("angular.inspection.undefined-export.message",
                                                     entity.htmlClassName, importsOwner.htmlClassName),
                          if (importsOwner.isScopeFullyResolved)
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                          else
                            ProblemHighlightType.WEAK_WARNING
          )
        }
      }
      else {
        throw IllegalArgumentException(entity.javaClass.name)
      }
    }
  }

  companion object {

    fun getUnusedImports(decorator: ES6Decorator): List<PsiElement> =
      getValidationResults(decorator).getProblems(ProblemType.UNUSED_IMPORT).map { it.location }

    private fun getValidationResults(decorator: ES6Decorator): ValidationResults<ProblemType> {
      return if (isAngularEntityDecorator(decorator, MODULE_DEC, COMPONENT_DEC))
        CachedValuesManager.getCachedValue(decorator) {
          CachedValueProvider.Result.create(
            validate(decorator),
            PsiModificationTracker.MODIFICATION_COUNT)
        }
      else
        ValidationResults.empty()
    }

    private fun validate(decorator: ES6Decorator): ValidationResults<ProblemType> {
      val importsOwner = Angular2EntitiesProvider.getEntity(decorator).asSafely<Angular2ImportsOwner>()
                         ?: return ValidationResults.empty()
      val results = ValidationResults<ProblemType>()

      for (validator in listOfNotNull(
        ImportsValidator(decorator, results, importsOwner),
        DeclarationsValidator(decorator, results),
        importsOwner.asSafely<Angular2Module>()?.let { ExportsValidator(decorator, results, it) },
        importsOwner.asSafely<Angular2Component>()?.let { UnusedComponentImportsValidator(decorator, results, it) }
      )) {
        validator.validate()
      }
      return results
    }

    private fun collectUsedDeclarations(component: Angular2Component): Set<Angular2Declaration> {
      val result = mutableSetOf<Angular2Declaration>()
      val scope = Angular2DeclarationsScope(component)
      val pipesByName = Angular2EntitiesProvider.getAllPipes(component.sourceElement.project)
        .mapValues { it.value.filter { pipe -> scope.contains(pipe) } }

      val expressionVisitor = object : JSRecursiveWalkingElementVisitor() {

        override fun visitJSReferenceExpression(node: JSReferenceExpression) {
          if (node is Angular2PipeReferenceExpression) {
            pipesByName[node.referenceName]?.let { result.addAll(it) }
          }
          super.visitJSReferenceExpression(node)
        }
      }

      component.templateFile?.acceptChildren(object : XmlRecursiveElementWalkingVisitor() {
        override fun visitXmlTag(tag: XmlTag) {
          Angular2ApplicableDirectivesProvider(tag, scope = scope).matched.forEach(result::add)
          super.visitXmlTag(tag)
        }

        override fun visitXmlAttribute(attribute: XmlAttribute) {
          if (attribute !is Angular2HtmlBoundAttribute) {
            Angular2InjectionUtils.findInjectedAngularExpression(attribute, Angular2EmbeddedExpression::class.java)
              ?.accept(expressionVisitor)
          }
          if (attribute.name.startsWith("*")) {
            Angular2TemplateBindings.get(attribute).let {
              Angular2ApplicableDirectivesProvider(it, scope = scope).matched.forEach(result::add)
            }
          }
          super.visitXmlAttribute(attribute)
        }

        override fun visitElement(element: PsiElement) {
          if (element is Angular2EmbeddedExpression) {
            element.acceptChildren(expressionVisitor)
          }
          else {
            super.visitElement(element)
          }
        }
      })
      return result
    }
  }
}
