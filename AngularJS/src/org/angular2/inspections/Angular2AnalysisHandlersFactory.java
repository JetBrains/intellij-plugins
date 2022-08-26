// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnalysisHandlersFactory;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSThisExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.lang.javascript.validation.JSProblemReporter;
import com.intellij.lang.javascript.validation.JSReferenceChecker;
import com.intellij.lang.javascript.validation.TypeScriptReferenceChecker;
import com.intellij.lang.javascript.validation.fixes.CreateJSFunctionIntentionAction;
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import org.angular2.entities.Angular2ComponentLocator;
import org.angular2.inspections.quickfixes.Angular2FixesFactory;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.intellij.util.ObjectUtils.notNull;

public class Angular2AnalysisHandlersFactory extends TypeScriptAnalysisHandlersFactory {

  @Override
  public @NotNull InspectionSuppressor getInspectionSuppressor() {
    return Angular2InspectionSuppressor.INSTANCE;
  }

  @Override
  public @NotNull JSReferenceChecker getReferenceChecker(@NotNull JSProblemReporter<?> reporter) {
    return new TypeScriptReferenceChecker(reporter) {
      @Override
      protected void addCreateFromUsageFixesForCall(@NotNull JSReferenceExpression methodExpression,
                                                    boolean isNewExpression,
                                                    ResolveResult @NotNull [] resolveResults,
                                                    @NotNull List<LocalQuickFix> quickFixes) {
        if (methodExpression instanceof Angular2PipeReferenceExpression) {
          // TODO Create pipe from usage
          return;
        }
        JSExpression qualifier = methodExpression.getQualifier();
        if (qualifier == null || qualifier instanceof JSThisExpression) {
          JSClass componentClass = Angular2ComponentLocator.findComponentClass(methodExpression);
          if (componentClass != null && methodExpression.getReferenceName() != null) {
            quickFixes.add(new CreateComponentMethodIntentionAction(methodExpression));
          }
          return;
        }
        super.addCreateFromUsageFixesForCall(methodExpression, isNewExpression, resolveResults, quickFixes);
      }

      @Override
      protected @NotNull @InspectionMessage String createUnresolvedCallReferenceMessage(@NotNull JSReferenceExpression methodExpression, boolean isNewExpression) {
        if (methodExpression instanceof Angular2PipeReferenceExpression) {
          return Angular2Bundle.message("angular.inspection.unresolved-pipe.message", methodExpression.getReferenceName());
        }
        return super.createUnresolvedCallReferenceMessage(methodExpression, isNewExpression);
      }

      @Override
      protected void reportUnresolvedReference(ResolveResult @NotNull [] resolveResults,
                                               @NotNull JSReferenceExpression referenceExpression,
                                               @NotNull List<LocalQuickFix> quickFixes,
                                               @NotNull @InspectionMessage String message,
                                               boolean isFunction,
                                               boolean inTypeContext) {
        if (referenceExpression instanceof Angular2PipeReferenceExpression) {
          Angular2FixesFactory.addUnresolvedDeclarationFixes(referenceExpression, quickFixes);
          // todo reject core TS quickFixes
        }
        super.reportUnresolvedReference(resolveResults, referenceExpression, quickFixes, message, isFunction, inTypeContext);
      }

      @Override
      protected boolean addCreateFromUsageFixes(JSReferenceExpression referenceExpression,
                                                ResolveResult[] resolveResults,
                                                List<? super LocalQuickFix> quickFixes,
                                                boolean inTypeContext,
                                                boolean ecma) {
        JSExpression qualifier = referenceExpression.getQualifier();
        if (qualifier == null || qualifier instanceof JSThisExpression) {
          JSClass componentClass = Angular2ComponentLocator.findComponentClass(referenceExpression);
          if (componentClass != null
              && referenceExpression.getReferenceName() != null) {
            quickFixes.add(new CreateComponentFieldIntentionAction(referenceExpression));
          }
          return inTypeContext;
        }
        return super.addCreateFromUsageFixes(referenceExpression, resolveResults, quickFixes, inTypeContext, ecma);
      }
    };
  }

  private static void addClassMemberModifiers(Template template, boolean staticContext, @NotNull JSClass targetClass) {
    if (DialectDetector.isTypeScript(targetClass)) {
      if (TypeScriptCodeStyleSettings.getTypeScriptSettings(targetClass).USE_PUBLIC_MODIFIER) {
        template.addTextSegment("public ");
      }
      if (staticContext) {
        template.addTextSegment("static ");
      }
    }
  }

  private static @NotNull SmartPsiElementPointer<JSReferenceExpression> createPointerFor(@NotNull JSReferenceExpression methodExpression) {
    return SmartPointerManager.getInstance(methodExpression.getProject()).createSmartPsiElementPointer(methodExpression);
  }

  private static class CreateComponentFieldIntentionAction extends CreateJSVariableIntentionAction {

    private final SmartPsiElementPointer<JSReferenceExpression> myRefExpressionPointer;

    CreateComponentFieldIntentionAction(JSReferenceExpression referenceExpression) {
      super(referenceExpression.getReferenceName(), true, false, false);
      myRefExpressionPointer = createPointerFor(referenceExpression);
    }

    @Override
    protected void applyFix(Project project, PsiElement psiElement, @NotNull PsiFile file, @Nullable Editor editor) {
      JSClass componentClass = Angular2ComponentLocator.findComponentClass(psiElement);
      assert componentClass != null;
      doApplyFix(project, componentClass, componentClass.getContainingFile(), null);
    }

    @Override
    protected JSReferenceExpression beforeStartTemplateAction(JSReferenceExpression referenceExpression,
                                                              Editor editor,
                                                              @NotNull PsiElement anchor,
                                                              boolean isStaticContext) {
      return referenceExpression;
    }

    @Override
    protected boolean skipParentIfClass() {
      return false;
    }

    @Override
    protected @NotNull Pair<JSReferenceExpression, PsiElement> calculateAnchors(PsiElement psiElement) {
      return Pair.create(myRefExpressionPointer.getElement(), psiElement.getLastChild());
    }

    @Override
    protected void addAccessModifier(Template template,
                                     @NotNull JSReferenceExpression referenceExpression,
                                     boolean staticContext,
                                     @NotNull JSClass targetClass) {
      addClassMemberModifiers(template, staticContext, targetClass);
    }
  }

  private static class CreateComponentMethodIntentionAction extends CreateJSFunctionIntentionAction {
    private final SmartPsiElementPointer<JSReferenceExpression> myRefExpressionPointer;

    CreateComponentMethodIntentionAction(JSReferenceExpression methodExpression) {
      super(methodExpression.getReferenceName(), true, false);
      myRefExpressionPointer = createPointerFor(methodExpression);
    }

    @Override
    protected void applyFix(Project project, PsiElement psiElement, @NotNull PsiFile file, @Nullable Editor editor) {
      JSClass componentClass = Angular2ComponentLocator.findComponentClass(psiElement);
      assert componentClass != null;
      doApplyFix(project, componentClass, componentClass.getContainingFile(), null);
    }

    @Override
    protected JSReferenceExpression beforeStartTemplateAction(JSReferenceExpression referenceExpression,
                                                              Editor editor,
                                                              @Nullable PsiElement anchor,
                                                              boolean isStaticContext) {
      return referenceExpression;
    }

    @Override
    protected boolean skipParentIfClass() {
      return false;
    }

    @Override
    protected @NotNull Pair<JSReferenceExpression, PsiElement> calculateAnchors(PsiElement psiElement) {
      return Pair.create(myRefExpressionPointer.getElement(), psiElement.getLastChild());
    }

    @Override
    protected void writeFunctionAndName(Template template,
                                        String createdMethodName,
                                        @NotNull PsiElement anchorParent,
                                        @Nullable PsiElement clazz,
                                        JSReferenceExpression referenceExpression) {
      if (referenceExpression.getQualifier() instanceof JSThisExpression) {
        createdMethodName = notNull(referenceExpression.getReferenceName(), createdMethodName);
      }
      template.addTextSegment(JSClassUtils.createClassFunctionName(createdMethodName, anchorParent));
    }

    @Override
    protected void addAccessModifier(Template template,
                                     @NotNull JSReferenceExpression referenceExpression,
                                     boolean staticContext,
                                     @NotNull JSClass targetClass) {
      addClassMemberModifiers(template, staticContext, targetClass);
    }
  }
}
