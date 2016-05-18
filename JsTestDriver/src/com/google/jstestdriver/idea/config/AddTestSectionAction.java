package com.google.jstestdriver.idea.config;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;

/**
 * @author Sergey Simonchik
 */
@SuppressWarnings("IntentionDescriptionNotFoundInspection")
public class AddTestSectionAction implements IntentionAction {
  @NotNull
  @Override
  public String getText() {
    return "Add 'test:' section";
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return "JsTestDriver configuration file";
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return file != null && file.isValid() && file instanceof YAMLFile;
  }

  @Override
  public void invoke(@NotNull final Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    if (file instanceof YAMLFile && file.isValid()) {
      final YAMLFile yamlFile = (YAMLFile) file;
      ApplicationManager.getApplication().runWriteAction(() -> {
        PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
        Document document = manager.getDocument(yamlFile);
        int length = document.getTextLength();
        document.insertString(length, "\n" + "test:");
        manager.commitDocument(document);
      });
    }
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
