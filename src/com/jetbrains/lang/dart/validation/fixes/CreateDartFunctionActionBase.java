package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartArgumentList;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;

abstract public class CreateDartFunctionActionBase extends BaseCreateFix {
  private static final Logger LOG = Logger.getInstance(CreateDartFunctionActionBase.class);

  protected final String myFunctionName;

  public CreateDartFunctionActionBase(@NotNull String name) {
    myFunctionName = name;
  }

  @NotNull
  public String getFamilyName() {
    return DartBundle.message("dart.create.function.intention.family");
  }

  @Override
  protected boolean isAvailable(Project project, PsiElement element, Editor editor, PsiFile file) {
    return PsiTreeUtil.getParentOfType(myElement, DartReference.class) != null;
  }

  @Override
  protected void applyFix(Project project, @NotNull PsiElement psiElement, Editor editor) {
    final TemplateManager templateManager = TemplateManager.getInstance(project);
    Template template = templateManager.createTemplate("", "");
    template.setToReformat(true);

    if (!buildTemplate(template, psiElement)) {
      return;
    }

    PsiElement anchor = findAnchor(psiElement);

    if (anchor == null) {
      CommonRefactoringUtil.showErrorHint(
        project,
        editor,
        DartBundle.message("dart.create.function.intention.family"),
        DartBundle.message("dart.cannot.find.place.to.create"),
        null
      );
      return;
    }

    navigate(project, editor, anchor.getTextOffset(), anchor.getContainingFile().getVirtualFile());

    templateManager.startTemplate(editor, template);
  }

  protected boolean buildTemplate(Template template, PsiElement psiElement) {
    DartCallExpression callExpression = PsiTreeUtil.getParentOfType(psiElement, DartCallExpression.class);
    if (callExpression == null) {
      LOG.debug(getName() + " cannot find function call for: " + psiElement.getText());
      return false;
    }

    buildFunctionText(template, callExpression);
    return true;
  }

  protected void buildFunctionText(Template template, @NotNull DartCallExpression callExpression) {
    template.addTextSegment(myFunctionName);
    template.addTextSegment("(");
    buildParameters(template, callExpression);
    template.addTextSegment("){\n");
    template.addEndVariable();
    template.addTextSegment("\n}\n");
  }

  private void buildParameters(Template template, DartCallExpression callExpression) {
    DartArgumentList argumentList = callExpression.getArguments().getArgumentList();
    if (argumentList != null) {
      DartPresentableUtil.appendArgumentList(template, argumentList);
    }
  }
}
