// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.SmartList
import com.intellij.util.containers.Stack
import org.angular2.Angular2DecoratorUtil.DECLARATIONS_PROP
import org.angular2.Angular2DecoratorUtil.EXPORTS_PROP
import org.angular2.Angular2DecoratorUtil.IMPORTS_PROP
import org.angular2.Angular2DecoratorUtil.MODULE_DEC
import org.angular2.Angular2DecoratorUtil.isAngularEntityDecorator
import org.angular2.entities.*
import org.angular2.entities.Angular2EntityUtils.forEachModule
import org.angular2.inspections.Angular2SourceEntityListValidator.ValidationResults
import org.angular2.lang.Angular2Bundle

abstract class AngularModuleConfigurationInspection protected constructor(private val myProblemType: ProblemType) : LocalInspectionTool() {

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
    UNDECLARED_EXPORT
  }

  private class DeclarationsValidator(decorator: ES6Decorator,
                                      results: ValidationResults<ProblemType>)
    : Angular2SourceEntityListValidator<Angular2Declaration, ProblemType>(
    decorator, results, Angular2Declaration::class.java, DECLARATIONS_PROP) {

    override fun processAcceptableEntity(entity: Angular2Declaration) {
      val aClass = entity.typeScriptClass
      if (entity.isStandalone && aClass != null) {
        registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                        Angular2Bundle.message("angular.inspection.wrong-entity-type.message.standalone-declarable", aClass.name!!))
      }
    }

    override fun processNonAcceptableEntityClass(aClass: JSClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      Angular2Bundle.message("angular.inspection.wrong-entity-type.message.not-declarable", aClass.name!!))
    }
  }

  private abstract class ImportExportValidator<T : Angular2Entity>
  protected constructor(decorator: ES6Decorator,
                        results: ValidationResults<ProblemType>,
                        entityClass: Class<T>,
                        propertyName: String,
                        protected val module: Angular2Module)
    : Angular2SourceEntityListValidator<T, ProblemType>(decorator, results, entityClass, propertyName) {

    protected fun checkCyclicDependencies(module: Angular2Module) {
      val cycleTrack = Stack<Angular2Module>()
      val processedModules = HashSet<Angular2Module>()
      val dfsStack = Stack<MutableList<Angular2Module>>()

      cycleTrack.push(this.module)
      dfsStack.push(SmartList(module))
      while (!dfsStack.isEmpty()) {
        val curNode = dfsStack.peek()
        if (curNode.isEmpty()) {
          dfsStack.pop()
          cycleTrack.pop()
        }
        else {
          val toProcess = curNode.removeAt(curNode.size - 1)
          if (toProcess === this.module) {
            cycleTrack.push(this.module)
            registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT, Angular2Bundle.message(
              "angular.inspection.cyclic-module-dependency.message.cycle",
              cycleTrack.joinToString(
                " " + Angular2Bundle.message("angular.inspection.cyclic-module-dependency.message.separator") + " ") { it.getName() }
            ))
            return
          }
          if (processedModules.add(toProcess)) {
            cycleTrack.push(toProcess)
            val dependencies = ArrayList<Angular2Module>()
            forEachModule(toProcess.exports) { dependencies.add(it) }
            forEachModule(toProcess.imports) { dependencies.add(it) }
            dfsStack.push(dependencies)
          }
        }
      }
    }
  }

  private class ImportsValidator(decorator: ES6Decorator,
                                 results: ValidationResults<ProblemType>,
                                 module: Angular2Module)
    : ImportExportValidator<Angular2Entity>(decorator, results, Angular2Entity::class.java, IMPORTS_PROP, module) {

    override fun processNonAcceptableEntityClass(aClass: JSClass) {
      reportMismatchedType(aClass)
    }

    override fun processAcceptableEntity(entity: Angular2Entity) {
      if (entity == module) {
        registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT,
                        Angular2Bundle.message("angular.inspection.cyclic-module-dependency.message.self-import", entity.getName()))
      }
      else if (entity is Angular2Module) {
        checkCyclicDependencies(entity)
      }
      else if (!Angular2EntityUtils.isImportableEntity(entity)) {
        val aClass = entity.typeScriptClass
        if (aClass != null) {
          reportMismatchedType(aClass)
        }
      }
    }

    private fun reportMismatchedType(aClass: JSClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      Angular2Bundle.message("angular.inspection.wrong-entity-type.message.not-importable", aClass.name!!))
    }
  }

  private class ExportsValidator(decorator: ES6Decorator,
                                 results: ValidationResults<ProblemType>,
                                 module: Angular2Module)
    : ImportExportValidator<Angular2Entity>(decorator, results, Angular2Entity::class.java, EXPORTS_PROP, module) {

    override fun processNonAcceptableEntityClass(aClass: JSClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      Angular2Bundle.message("angular.inspection.wrong-entity-type.message.not-entity", aClass.name!!),
                      if (module.isScopeFullyResolved)
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                      else
                        ProblemHighlightType.WEAK_WARNING)
    }

    override fun processAcceptableEntity(entity: Angular2Entity) {
      if (entity is Angular2Module) {
        if (entity == module) {
          registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT,
                          Angular2Bundle.message("angular.inspection.cyclic-module-dependency.message.self-export", entity.getName()))
        }
        else {
          checkCyclicDependencies(entity)
        }
      }
      else if (entity is Angular2Declaration) {
        if (!module.declarationsInScope.contains(entity)) {
          registerProblem(ProblemType.UNDECLARED_EXPORT,
                          Angular2Bundle.message("angular.inspection.undefined-export.message",
                                                 entity.typeScriptClass?.name ?: entity.getName(),
                                                 module.getName()),
                          if (module.isScopeFullyResolved)
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

    private fun getValidationResults(decorator: ES6Decorator): ValidationResults<ProblemType> {
      return if (isAngularEntityDecorator(decorator, MODULE_DEC))
        CachedValuesManager.getCachedValue(decorator) {
          CachedValueProvider.Result.create(
            validate(decorator),
            PsiModificationTracker.MODIFICATION_COUNT)
        }
      else
        ValidationResults.empty()
    }

    private fun validate(decorator: ES6Decorator): ValidationResults<ProblemType> {
      val module = Angular2EntitiesProvider.getModule(decorator) ?: return ValidationResults.empty()
      val results = ValidationResults<ProblemType>()

      for (validator in listOf(ImportsValidator(decorator, results, module),
                               DeclarationsValidator(decorator, results),
                               ExportsValidator(decorator, results, module))) {
        validator.validate()
      }
      return results
    }
  }
}
