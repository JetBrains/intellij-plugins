package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartExecutionScope;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;

public class CreateDartClassAction extends BaseCreateFix {
  public final String myClassName;

  public CreateDartClassAction(String name) {
    myClassName = name;
  }

  @NotNull
  @Override
  public String getName() {
    return DartBundle.message("dart.creat.class.fix.name", myClassName);
  }

  @Override
  protected PsiElement getScopeBody(PsiElement element) {
    return PsiTreeUtil.getTopmostParentOfType(element, DartExecutionScope.class);
  }

  @Override
  protected void applyFix(Project project, @NotNull PsiElement psiElement, Editor editor) {
    PsiElement anchor = findAnchor(psiElement);
    if (anchor == null) {
      return;
    }

    final TemplateManager templateManager = TemplateManager.getInstance(project);
    Template template = templateManager.createTemplate("", "");
    template.setToReformat(true);

    template.addTextSegment("class ");
    template.addVariable(DartPresentableUtil.getExpression(myClassName), false);
    template.addTextSegment("{\n");
    template.addEndVariable();
    template.addTextSegment("\n}\n");

    navigate(project, editor, anchor.getTextOffset(), anchor.getContainingFile().getVirtualFile());

    templateManager.startTemplate(editor, template);
  }
}
