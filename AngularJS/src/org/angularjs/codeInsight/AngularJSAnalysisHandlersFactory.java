package org.angularjs.codeInsight;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.javascript.JSAnalysisHandlersFactory;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor;
import com.intellij.lang.javascript.validation.JavaScriptAnnotatingVisitor;
import com.intellij.lang.javascript.validation.fixes.CreateJSFunctionIntentionAction;
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import org.angularjs.index.AngularJS2IndexingHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AngularJSAnalysisHandlersFactory extends JSAnalysisHandlersFactory {
  @NotNull
  @Override
  public JSAnnotatingVisitor createAnnotatingVisitor(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
    return new JavaScriptAnnotatingVisitor(psiElement, holder) {
      @Override
      protected void addCreateFromUsageFixesForCall(@NotNull JSCallExpression node,
                                                    @NotNull JSReferenceExpression referenceExpression,
                                                    @NotNull ResolveResult[] resolveResults,
                                                    @NotNull List<LocalQuickFix> quickFixes) {
        if (referenceExpression.getQualifier() != null) return;

        JSClass directive = AngularJS2IndexingHandler.findDirectiveClass(node);
        if (directive != null) {
          quickFixes.add(new CreateJSFunctionIntentionAction(referenceExpression.getReferencedName(), true) {
            @Override
            protected void applyFix(Project project, PsiElement psiElement, PsiFile file, Editor editor) {
              JSClass directive = AngularJS2IndexingHandler.findDirectiveClass(psiElement);
              assert directive != null;
              doApplyFix(project, directive, directive.getContainingFile(), null, null);
            }

            @NotNull
            @Override
            protected Pair<JSReferenceExpression, PsiElement> calculateAnchors(PsiElement psiElement) {
              return Pair.create(referenceExpression, psiElement.getLastChild());
            }

            @Override
            protected void writeFunctionAndName(Template template,
                                                String createdMethodName,
                                                PsiFile file,
                                                JSClass clazz,
                                                JSReferenceExpression referenceExpression) {
              template.addTextSegment(JSClassUtils.createClassFunctionName(createdMethodName, file));
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

        JSClass directive = AngularJS2IndexingHandler.findDirectiveClass(referenceExpression);
        if (directive != null) {
          quickFixes.add(new CreateJSVariableIntentionAction(referenceExpression.getReferencedName(), true, false) {
            @Override
            protected void applyFix(Project project, PsiElement psiElement, PsiFile file, Editor editor) {
              JSClass directive = AngularJS2IndexingHandler.findDirectiveClass(psiElement);
              assert directive != null;
              doApplyFix(project, directive, directive.getContainingFile(), null, null);
            }

            @NotNull
            @Override
            protected Pair<JSReferenceExpression, PsiElement> calculateAnchors(PsiElement psiElement) {
              return Pair.create(referenceExpression, psiElement.getLastChild());
            }

            @Override
            protected JSExpression addAccessModifier(Template template,
                                                     JSReferenceExpression referenceExpression,
                                                     PsiFile file,
                                                     boolean staticContext,
                                                     JSClass contextClass) {
              return referenceExpression.getQualifier();
            }
          });
        }

        return inTypeContext;
      }
    };
  }
}
