// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2Module;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Objects;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;
import static java.util.Arrays.asList;
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.inspections.Angular2SourceEntityListValidator.ValidationResults;

public abstract class Angular2ModuleConfigurationInspection extends LocalInspectionTool {

  private final ProblemType myProblemType;

  protected Angular2ModuleConfigurationInspection(@NotNull ProblemType type) {
    myProblemType = type;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JSElementVisitor() {
      @Override
      public void visitES6Decorator(ES6Decorator decorator) {
        getValidationResults(decorator).registerProblems(myProblemType, holder);
      }
    };
  }

  @NotNull
  public static ValidationResults<ProblemType> getValidationResults(@NotNull ES6Decorator decorator) {
    if (!MODULE_DEC.equals(decorator.getDecoratorName())
        || !Angular2LangUtil.isAngular2Context(decorator)) {
      return ValidationResults.empty();
    }
    return CachedValuesManager.getCachedValue(
      decorator, () -> CachedValueProvider.Result.create(
        validate(decorator),
        PsiModificationTracker.MODIFICATION_COUNT));
  }

  @NotNull
  private static ValidationResults<ProblemType> validate(@NotNull ES6Decorator decorator) {
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
    protected void processNonEntityClass(@NotNull JSClass aClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      "Class '" + aClass.getName() + "' is neither Angular Component, Directive nor Pipe.");
    }
  }


  private abstract static class ImportExportValidator<T extends Angular2Entity> extends Angular2SourceEntityListValidator<T, ProblemType> {

    protected final Angular2Module myModule;

    protected ImportExportValidator(@NotNull Class<T> entityClass, @NotNull String propertyName, @NotNull Angular2Module module) {
      super(entityClass, propertyName);
      myModule = module;
    }

    protected void checkCyclicDependencies(@NotNull Angular2Module module) {
      try {
        checkCyclicDependencies(ContainerUtil.newLinkedHashSet(myModule), module);
      }
      catch (RecurrentImportException e) {
        registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT, e.getMessage());
      }
    }

    private void checkCyclicDependencies(@NotNull LinkedHashSet<Angular2Module> visitedModules,
                                         @NotNull Angular2Module module) throws RecurrentImportException {
      if (!visitedModules.add(module)) {
        if (module == myModule) {
          throw new RecurrentImportException("Cyclic dependency of modules: "
                                             + StringUtil.join(visitedModules, Angular2Module::getName, " -> ")
                                             + " -> " + module.getName());
        }
        return;
      }
      for (Angular2Module child : module.getImports()) {
        checkCyclicDependencies(visitedModules, child);
      }
      for (Angular2Entity child : module.getExports()) {
        if (child instanceof Angular2Module) {
          checkCyclicDependencies(visitedModules, (Angular2Module)child);
        }
      }
      visitedModules.remove(module);
    }
  }

  private static class ImportsValidator extends ImportExportValidator<Angular2Module> {

    private ImportsValidator(@NotNull Angular2Module module) {
      super(Angular2Module.class, IMPORTS_PROP, module);
    }

    @Override
    protected void processNonEntityClass(@NotNull JSClass aClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      "Class '" + aClass.getName() + "' is not an Angular Module.");
    }

    @Override
    protected void processEntity(@NotNull Angular2Module module) {
      if (module.equals(myModule)) {
        registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT,
                        "Module '" + module.getName() + "' cannot import itself");
      }
      else {
        checkCyclicDependencies(module);
      }
    }
  }

  private static class ExportsValidator extends ImportExportValidator<Angular2Entity> {

    protected ExportsValidator(@NotNull Angular2Module module) {
      super(Angular2Entity.class, EXPORTS_PROP, module);
    }

    @Override
    protected void processNonEntityClass(@NotNull JSClass aClass) {
      registerProblem(ProblemType.ENTITY_WITH_MISMATCHED_TYPE,
                      "Class '" + aClass.getName() +
                      "' is neither Angular Module, Component, Directive nor Pipe.",
                      Objects.requireNonNull(myModule).isScopeFullyResolved()
                      ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                      : ProblemHighlightType.WEAK_WARNING);
    }

    @Override
    protected void processEntity(@NotNull Angular2Entity entity) {
      if (entity instanceof Angular2Module) {
        if (entity.equals(myModule)) {
          registerProblem(ProblemType.RECURSIVE_IMPORT_EXPORT,
                          "Module '" + entity.getName() + "' cannot export itself");
        }
        else {
          checkCyclicDependencies((Angular2Module)entity);
        }
      }
      else if (entity instanceof Angular2Declaration) {
        if (!Objects.requireNonNull(myModule).getDeclarationsInScope().contains(entity)) {
          registerProblem(ProblemType.UNDECLARED_EXPORT,
                          "Cannot export '" +
                          notNull(doIfNotNull(entity.getTypeScriptClass(), TypeScriptClass::getName),
                                  () -> entity.getName()) +
                          "' as it is neither declared nor imported by the module.",
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

  private static final class RecurrentImportException extends Exception {
    private RecurrentImportException(String message) {
      super(message);
    }
  }
}
