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
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testIntegration.TestFinderHelper;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.source.Angular2SourceModule;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.intellij.util.ObjectUtils.*;
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.entities.Angular2EntityUtils.renderEntityList;

public class AngularMissingOrInvalidDeclarationInModuleInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JSElementVisitor() {

      @Override
      public void visitES6Decorator(ES6Decorator decorator) {
        if (isAngularDecorator(decorator, COMPONENT_DEC, DIRECTIVE_DEC, PIPE_DEC)
            && !TestFinderHelper.isTest(decorator)) {
          Angular2Declaration declaration = tryCast(Angular2EntitiesProvider.getEntity(decorator),
                                                    Angular2Declaration.class);
          if (declaration != null) {
            Collection<Angular2Module> modules = declaration.getAllModules();
            PsiElement classIdentifier = notNull(doIfNotNull(PsiTreeUtil.getContextOfType(decorator, TypeScriptClass.class),
                                                             TypeScriptClass::getNameIdentifier), decorator);
            if (modules.isEmpty()) {
              holder.registerProblem(classIdentifier,
                                     Angular2Bundle.message("angular.inspection.decorator.not-declared-in-NgModule",
                                                            Angular2EntityUtils.getEntityClassName(decorator)),
                                     allSourceDeclarationsResolved(decorator.getProject())
                                     ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                     : ProblemHighlightType.WEAK_WARNING);
            }
            else if (modules.size() > 1) {
              holder.registerProblem(classIdentifier,
                                     Angular2Bundle.message("angular.inspection.decorator.declared-in-many-NgModules",
                                                            Angular2EntityUtils.getEntityClassName(decorator),
                                                            renderEntityList(modules)));
            }
          }
        }
      }
    };
  }


  private static boolean allSourceDeclarationsResolved(@NotNull Project project) {
    return !ContainerUtil.exists(Angular2EntitiesProvider.getAllModules(project),
                                 module -> module instanceof Angular2SourceModule
                                           && !module.areDeclarationsFullyResolved());
  }
}
