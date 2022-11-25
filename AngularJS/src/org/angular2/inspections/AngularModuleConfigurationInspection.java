// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.SmartList;
import com.intellij.util.containers.Stack;
import org.angular2.entities.*;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;
import static java.util.Arrays.asList;
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.entities.Angular2EntityUtils.forEachModule;
import static org.angular2.inspections.Angular2SourceEntityListValidator.ValidationResults;

public abstract class AngularModuleConfigurationInspection extends LocalInspectionTool {

  private final ProblemType myProblemType;

  protected AngularModuleConfigurationInspection(@NotNull ProblemType type) {
    myProblemType = type;
  }

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JSElementVisitor() {
      @Override
      public void visitES6Decorator(ES6Decorator decorator) {
        getValidationResults(decorator).registerProblems(myProblemType, holder);
      }
    };
  }

  public static @NotNull ValidationResults<ProblemType> getValidationResults(@NotNull ES6Decorator decorator) {
    return isAngularEntityDecorator(decorator, MODULE_DEC)
           ? CachedValuesManager.getCachedValue(decorator,
                                                () -> CachedValueProvider.Result.create(
                                                  validate(decorator),
                                                  PsiModificationTracker.MODIFICATION_COUNT))
           : ValidationResults.empty();
  }

  private static @NotNull ValidationResults<ProblemType> validate(@NotNull ES6Decorator decorator) {
    Angular2Module module = Angular2EntitiesProvider.getModule(decorator);
    if (module == null) {
      return ValidationResults.empty();
    }
    ValidationResults<ProblemType> results = new ValidationResults<>();

    asList(new ImportsValidator(module),
           new DeclarationsValidator(),
           new ExportsValidator(module)).forEach(
      val -> val.validate(decorator, results)
    );
    return results;
  }

  protected enum ProblemType {
    ENTITY_WITH_MISMATCHED_TYPE,
    RECURSIVE_IMPORT_EXPORT,
    UNDECLARED_EXPORT
  }

  private static class DeclarationsValidator extends Angular2SourceEntityListValidator<Angular2Declaration, ProblemType> {

    protected DeclarationsValidator() {
      super(Angular2Declaration.class, DECLARATIONS_PROP);
    }

    @Override
    protected void processAcceptableEntity(@NotNull Angular2Declaration entity) {
      var aClass = entity.getTypeScriptClass();
      if (entity.isStandalone() && aClass != null) {
        registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                        Angular2Bundle.message("angular.inspection.wrong-entity-type.message.standalone-declarable", aClass.getName()));
      }
    }

    @Override
    protected void processNonAcceptableEntityClass(@NotNull JSClass aClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      Angular2Bundle.message("angular.inspection.wrong-entity-type.message.not-declarable", aClass.getName()));
    }
  }

  private abstract static class ImportExportValidator<T extends Angular2Entity> extends Angular2SourceEntityListValidator<T, ProblemType> {

    protected final Angular2Module myModule;

    protected ImportExportValidator(@NotNull Class<T> entityClass, @NotNull String propertyName, @NotNull Angular2Module module) {
      super(entityClass, propertyName);
      myModule = module;
    }

    protected void checkCyclicDependencies(@NotNull Angular2Module module) {
      Stack<Angular2Module> cycleTrack = new Stack<>();
      Set<Angular2Module> processedModules = new HashSet<>();
      Stack<List<Angular2Module>> dfsStack = new Stack<>();

      cycleTrack.push(myModule);
      dfsStack.push(new SmartList<>(module));
      while (!dfsStack.isEmpty()) {
        List<Angular2Module> curNode = dfsStack.peek();
        if (curNode.isEmpty()) {
          dfsStack.pop();
          cycleTrack.pop();
        }
        else {
          Angular2Module toProcess = curNode.remove(curNode.size() - 1);
          if (toProcess == myModule) {
            cycleTrack.push(myModule);
            registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT, Angular2Bundle.message(
              "angular.inspection.cyclic-module-dependency.message.cycle",
              StringUtil.join(cycleTrack, Angular2Module::getName,
                              " " + Angular2Bundle.message("angular.inspection.cyclic-module-dependency.message.separator") + " ")));
            return;
          }
          if (processedModules.add(toProcess)) {
            cycleTrack.push(toProcess);
            List<Angular2Module> dependencies = new ArrayList<>();
            forEachModule(toProcess.getExports(), dependencies::add);
            forEachModule(toProcess.getImports(), dependencies::add);
            dfsStack.push(dependencies);
          }
        }
      }
    }
  }

  private static final class ImportsValidator extends ImportExportValidator<Angular2Entity> {

    private ImportsValidator(@NotNull Angular2Module module) {
      super(Angular2Entity.class, IMPORTS_PROP, module);
    }

    @Override
    protected void processNonAcceptableEntityClass(@NotNull JSClass aClass) {
      reportMismatchedType(aClass);
    }

    @Override
    protected void processAcceptableEntity(@NotNull Angular2Entity entity) {
      if (entity.equals(myModule)) {
        registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT,
                        Angular2Bundle.message("angular.inspection.cyclic-module-dependency.message.self-import", entity.getName()));
      }
      else if (entity instanceof Angular2Module) {
        checkCyclicDependencies((Angular2Module)entity);
      }
      else if (!Angular2EntityUtils.isImportableEntity(entity)) {
        var aClass = entity.getTypeScriptClass();
        if (aClass != null) {
          reportMismatchedType(aClass);
        }
      }
    }

    private void reportMismatchedType(@NotNull JSClass aClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      Angular2Bundle.message("angular.inspection.wrong-entity-type.message.not-importable", aClass.getName()));
    }
  }

  private static class ExportsValidator extends ImportExportValidator<Angular2Entity> {

    protected ExportsValidator(@NotNull Angular2Module module) {
      super(Angular2Entity.class, EXPORTS_PROP, module);
    }

    @Override
    protected void processNonAcceptableEntityClass(@NotNull JSClass aClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      Angular2Bundle.message("angular.inspection.wrong-entity-type.message.not-entity", aClass.getName()),
                      Objects.requireNonNull(myModule).isScopeFullyResolved()
                      ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                      : ProblemHighlightType.WEAK_WARNING);
    }

    @Override
    protected void processAcceptableEntity(@NotNull Angular2Entity entity) {
      if (entity instanceof Angular2Module) {
        if (entity.equals(myModule)) {
          registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT,
                          Angular2Bundle.message("angular.inspection.cyclic-module-dependency.message.self-export", entity.getName()));
        }
        else {
          checkCyclicDependencies((Angular2Module)entity);
        }
      }
      else if (entity instanceof Angular2Declaration) {
        if (!Objects.requireNonNull(myModule).getDeclarationsInScope().contains(entity)) {
          registerProblem(ProblemType.UNDECLARED_EXPORT,
                          Angular2Bundle.message("angular.inspection.undefined-export.message",
                                                 notNull(doIfNotNull(entity.getTypeScriptClass(), TypeScriptClass::getName),
                                                         () -> entity.getName()),
                                                 myModule.getName()),
                          Objects.requireNonNull(myModule).isScopeFullyResolved()
                          ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                          : ProblemHighlightType.WEAK_WARNING);
        }
      }
      else {
        throw new IllegalArgumentException(entity.getClass().getName());
      }
    }
  }
}
