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
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.source.Angular2SourceModule;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.intellij.util.ObjectUtils.*;
import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2DeclarationMembershipInModuleInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JSElementVisitor() {

      @Override
      public void visitES6Decorator(ES6Decorator decorator) {
        if ((COMPONENT_DEC.equals(decorator.getDecoratorName())
             || DIRECTIVE_DEC.equals(decorator.getDecoratorName())
             || PIPE_DEC.equals(decorator.getDecoratorName()))
            && Angular2LangUtil.isAngular2Context(decorator)) {
          Angular2Declaration declaration = tryCast(Angular2EntitiesProvider.getEntity(decorator),
                                                    Angular2Declaration.class);
          if (declaration != null) {
            Collection<Angular2Module> modules = declaration.getAllModules();
            PsiElement classIdentifier = notNull(doIfNotNull(PsiTreeUtil.getContextOfType(decorator, TypeScriptClass.class),
                                                             TypeScriptClass::getNameIdentifier), decorator);
            if (modules.isEmpty()) {
              holder.registerProblem(classIdentifier,
                                     "Declaration is not included in any NgModule.",
                                     allSourceDeclarationsResolved(decorator.getProject())
                                     ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                                     : ProblemHighlightType.WEAK_WARNING);
            }
            else if (modules.size() > 1) {
              holder.registerProblem(classIdentifier,
                                     "Declaration is included in more than one NgModule.");
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
