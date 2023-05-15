/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.lang.javascript.inspections.actionscript;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.macro.MacroFactory;
import com.intellij.codeInspection.*;
import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.types.TypeFromUsageDetector;
import com.intellij.lang.javascript.validation.fixes.BaseCreateFix;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 */
public class JSUntypedDeclarationInspection extends JSInspection {

  @Override
  @NotNull
  protected JSElementVisitor createVisitor(final ProblemsHolder holder, LocalInspectionToolSession session) {
    return new JSElementVisitor() {
      @Override public void visitJSVariable(final @NotNull JSVariable node) {
        process(node, holder);
      }

      @Override public void visitJSFunctionExpression(final @NotNull JSFunctionExpression node) {
        process(node, holder);
      }

      @Override public void visitJSFunctionDeclaration(final @NotNull JSFunction node) {
        if (node.isConstructor() || node.isSetProperty()) return;
        process(node, holder);
      }
    };
  }

  private static void process(final JSNamedElement node, final ProblemsHolder holder) {
    if (!DialectDetector.isActionScript(node)) return;
    ASTNode nameIdentifier = node.findNameIdentifier();

    if ((nameIdentifier != null || node instanceof JSFunction) &&
        JSPsiImplUtils.getTypeFromDeclaration(node) == null &&
        (!(node instanceof JSParameter) || !((JSParameter)node).isRest())
      ) {
      if (node instanceof JSFunctionExpression &&
          nameIdentifier !=  null &&
          PsiTreeUtil.getParentOfType(nameIdentifier.getPsi(), JSFunction.class) != node) {
        nameIdentifier = null;
      }

      LocalQuickFix[] fixes = holder.isOnTheFly() ? new LocalQuickFix[]{new AddTypeToDclFix()} : LocalQuickFix.EMPTY_ARRAY;
      holder.registerProblem(
        (nameIdentifier != null ? nameIdentifier:node.getNode().findChildByType(JSTokenTypes.FUNCTION_KEYWORD)).getPsi(),
        FlexBundle.message(
          node instanceof JSFunction ? "js.untyped.function.problem":"js.untyped.variable.problem",
          nameIdentifier != null ? nameIdentifier.getText():"anonymous"
        ),
        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        fixes
      );
    }
  }

  private static class AddTypeToDclFix implements LocalQuickFix {

    @Override
    @NotNull
    public String getFamilyName() {
      return FlexBundle.message("js.untyped.declaration.problem.addtype.fix");
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      PsiElement anchor = descriptor.getPsiElement();
      final PsiFile containingFile = anchor.getContainingFile();

      PsiElement parent = anchor.getParent();
      if (parent instanceof JSFunction) {
        anchor = ((JSFunction)parent).getParameterList();
      }

      Navigatable openDescriptor = PsiNavigationSupport.getInstance()
                                                       .createNavigatable(project, containingFile.getVirtualFile(),
                                                                          anchor.getTextRange().getEndOffset());
      openDescriptor.navigate(true);
      Editor textEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
      TemplateManager templateManager = TemplateManager.getInstance(project);

      final Template t = templateManager.createTemplate("","");
      t.addTextSegment(":");

      JSType detectedTypeFromUsage = TypeFromUsageDetector.detectTypeFromUsage(parent);

      if (detectedTypeFromUsage != null) {
        BaseCreateFix.addTypeVariable(t, "a", anchor, detectedTypeFromUsage.getTypeText(JSType.TypeTextFormat.CODE));
      }
      else {
        String defaultValue = "uint";
        if (ApplicationManager.getApplication().isUnitTestMode()) {
          t.addTextSegment(defaultValue);
        } else {
          t.addVariable("a", new MacroCallNode(MacroFactory.createMacro("complete")), new ConstantNode(defaultValue), true);
        }
      }

      templateManager.startTemplate(textEditor, t);
    }
  }
}
