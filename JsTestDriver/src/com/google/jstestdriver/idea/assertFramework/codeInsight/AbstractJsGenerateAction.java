package com.google.jstestdriver.idea.assertFramework.codeInsight;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractJsGenerateAction extends AnAction {

  @NotNull
  public abstract String getHumanReadableDescription();

  public abstract boolean isEnabled(@NotNull GenerateActionContext context);

  public abstract void actionPerformed(@NotNull GenerateActionContext context);

  @Override
  public final void actionPerformed(AnActionEvent e) {
    final GenerateActionContext generateContext = fetchContext(e.getDataContext());
    if (generateContext != null) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          generateContext.getEditor().getSelectionModel().removeSelection();
        }
      });
      actionPerformed(generateContext);
    }
  }

  @Override
  public final void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    GenerateActionContext generateContext = fetchContext(e.getDataContext());

    boolean enabled = generateContext != null;
    if (enabled) {
      enabled = isEnabled(generateContext);
    }
    if (enabled) {
      enabled = CommonRefactoringUtil.checkReadOnlyStatus(generateContext.getJsFile());
    }
    if (!enabled) {
      presentation.setVisible(false);
      return;
    }

    presentation.setVisible(true);
    presentation.setText(getHumanReadableDescription(), true);
    presentation.setEnabled(true);
  }

  @Nullable
  private static GenerateActionContext fetchContext(DataContext dataContext) {
    Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
    if (editor == null) {
      return null;
    }
    PsiFile psiFile = LangDataKeys.PSI_FILE.getData(dataContext);
    if (!(psiFile instanceof JSFile)) {
      return null;
    }
    return new GenerateActionContext((JSFile)psiFile, editor);
  }
}
