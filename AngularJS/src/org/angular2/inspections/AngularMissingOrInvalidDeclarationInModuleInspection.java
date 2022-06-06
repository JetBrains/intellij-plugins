// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.testIntegration.TestFinderHelper;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.*;
import org.angular2.entities.source.Angular2SourceModule;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static com.intellij.util.ObjectUtils.*;
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.entities.Angular2EntityUtils.renderEntityList;

public class AngularMissingOrInvalidDeclarationInModuleInspection extends LocalInspectionTool {

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JSElementVisitor() {

      @Override
      public void visitES6Decorator(ES6Decorator decorator) {
        if (isAngularEntityDecorator(decorator, COMPONENT_DEC, DIRECTIVE_DEC, PIPE_DEC) && !TestFinderHelper.isTest(decorator)) {
          Angular2Declaration declaration = tryCast(Angular2EntitiesProvider.getEntity(decorator), Angular2Declaration.class);
          if (declaration != null) {
            Collection<Angular2Module> modules = declaration.getAllDeclaringModules();
            if (Angular2FrameworkHandler.EP_NAME.extensions().anyMatch(h -> h.suppressModuleInspectionErrors(modules, declaration))) {
              return;
            }
            PsiElement classIdentifier = notNull(doIfNotNull(getClassForDecoratorElement(decorator),
                                                             TypeScriptClass::getNameIdentifier), decorator);
            if (modules.isEmpty()) {
              holder.registerProblem(classIdentifier,
                                     Angular2Bundle.message("angular.inspection.invalid-declaration-in-module.message.not-declared",
                                                            Angular2EntityUtils.getEntityClassName(decorator)),
                                     allSourceDeclarationsResolved(decorator.getProject())
                                     ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                     : ProblemHighlightType.WEAK_WARNING);
            }
            else if (modules.size() > 1) {
              holder.registerProblem(classIdentifier,
                                     Angular2Bundle.message("angular.inspection.invalid-declaration-in-module.message.declared-in-many",
                                                            Angular2EntityUtils.getEntityClassName(decorator),
                                                            renderEntityList(modules)));
            }
          }
        }
      }
    };
  }


  private static boolean allSourceDeclarationsResolved(@NotNull Project project) {
    List<Angular2Module> modules = Angular2EntitiesProvider.getAllModules(project);
    return ContainerUtil.and(modules, m -> !(m instanceof Angular2SourceModule) || m.areDeclarationsFullyResolved());
  }
}
