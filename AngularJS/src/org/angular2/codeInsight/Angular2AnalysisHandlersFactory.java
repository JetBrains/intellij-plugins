package org.angular2.codeInsight;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JSAnalysisHandlersFactory;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.lang.javascript.validation.JSReferenceChecker;
import com.intellij.lang.javascript.validation.JSReferenceInspectionProblemReporter;
import com.intellij.lang.javascript.validation.fixes.CreateJSFunctionIntentionAction;
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction;
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import org.angular2.index.Angular2IndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2AnalysisHandlersFactory extends JSAnalysisHandlersFactory {
  @NotNull
  @Override
  public JSReferenceChecker getReferenceChecker(@NotNull JSReferenceInspectionProblemReporter reporter) {
    return new JSReferenceChecker(reporter) {
      @Override
      protected void addCreateFromUsageFixesForCall(@NotNull JSReferenceExpression methodExpression,
                                                    boolean isNewExpression,
                                                    @NotNull ResolveResult[] resolveResults,
                                                    @NotNull List<LocalQuickFix> quickFixes) {
        if (methodExpression.getQualifier() != null) return;

        JSClass componentClass = Angular2IndexingHandler.findComponentClass(methodExpression);
        if (componentClass != null) {
          SmartPsiElementPointer<JSReferenceExpression> refExpressionPointer = createPointerFor(methodExpression);

          quickFixes.add(new CreateJSFunctionIntentionAction(methodExpression.getReferenceName(), true, false) {
            @Override
            protected void applyFix(Project project, PsiElement psiElement, PsiFile file, Editor editor) {
              JSClass componentClass = Angular2IndexingHandler.findComponentClass(psiElement);
              assert componentClass != null;
              doApplyFix(project, componentClass, componentClass.getContainingFile(), null);
            }

            @NotNull
            @Override
            protected Pair<JSReferenceExpression, PsiElement> calculateAnchors(PsiElement psiElement) {
              return Pair.create(refExpressionPointer.getElement(), psiElement.getLastChild());
            }

            @Override
            protected void writeFunctionAndName(Template template,
                                                String createdMethodName,
                                                @NotNull PsiElement anchorParent,
                                                @Nullable PsiElement clazz,
                                                JSReferenceExpression referenceExpression) {
              template.addTextSegment(JSClassUtils.createClassFunctionName(createdMethodName, anchorParent));
            }

            @Override
            protected void addAccessModifier(Template template,
                                             JSReferenceExpression referenceExpression,
                                             boolean staticContext,
                                             @NotNull JSClass contextClass) {
              if (DialectDetector.isTypeScript(contextClass)) {
                if (TypeScriptCodeStyleSettings.getTypeScriptSettings(contextClass).USE_PUBLIC_MODIFIER) {
                  template.addTextSegment("public ");
                }
                if (staticContext) {
                  template.addTextSegment("static ");
                }
              }
            }
          });
        }
      }

      @Override
      protected boolean addCreateFromUsageFixes(JSReferenceExpression referenceExpression,
                                                ResolveResult[] resolveResults,
                                                List<LocalQuickFix> quickFixes,
                                                boolean inTypeContext,
                                                boolean ecma) {
        if (referenceExpression.getQualifier() != null) return inTypeContext;

        JSClass componentClass = Angular2IndexingHandler.findComponentClass(referenceExpression);
        if (componentClass != null) {
          SmartPsiElementPointer<JSReferenceExpression> refExpressionPointer = createPointerFor(referenceExpression);
          quickFixes.add(new CreateJSVariableIntentionAction(referenceExpression.getReferenceName(), true, false, false) {
            @Override
            protected void applyFix(Project project, PsiElement psiElement, PsiFile file, Editor editor) {
              JSClass componentClass = Angular2IndexingHandler.findComponentClass(psiElement);
              assert componentClass != null;
              doApplyFix(project, componentClass, componentClass.getContainingFile(), null);
            }

            @NotNull
            @Override
            protected Pair<JSReferenceExpression, PsiElement> calculateAnchors(PsiElement psiElement) {
              return Pair.create(refExpressionPointer.getElement(), psiElement.getLastChild());
            }

            @Override
            protected void addAccessModifier(Template template,
                                             JSReferenceExpression referenceExpression,
                                             boolean staticContext,
                                             @NotNull JSClass contextClass) {
            }
          });
        }

        return inTypeContext;
      }
    };
  }

  @NotNull
  private static SmartPsiElementPointer<JSReferenceExpression> createPointerFor(@NotNull JSReferenceExpression methodExpression) {
    return SmartPointerManager.getInstance(methodExpression.getProject()).createSmartPsiElementPointer(methodExpression);
  }
}
