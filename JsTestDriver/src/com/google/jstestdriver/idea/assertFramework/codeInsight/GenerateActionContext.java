package com.google.jstestdriver.idea.assertFramework.codeInsight;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenerateActionContext {
  private final JSFile myJsFile;
  private final Editor myEditor;
  private final int myDocumentCaretOffset;

  GenerateActionContext(@NotNull JSFile jsFile, @NotNull Editor editor) {
    myJsFile = jsFile;
    myEditor = editor;
    myDocumentCaretOffset = myEditor.getCaretModel().getOffset();
  }

  @NotNull
  public JSFile getJsFile() {
    return myJsFile;
  }

  public int getDocumentCaretOffset() {
    return myDocumentCaretOffset;
  }

  @NotNull
  public Editor getEditor() {
    return myEditor;
  }

  @NotNull
  public Document getDocument() {
    return myEditor.getDocument();
  }

  @NotNull
  public Project getProject() {
    return myJsFile.getProject();
  }

  @NotNull
  public CaretModel getCaretModel() {
    return myEditor.getCaretModel();
  }

  @Nullable
  public PsiElement getPsiElementUnderCaret() {
    PsiElement element = myJsFile.findElementAt(getDocumentCaretOffset());
    if (element == null) {
      element = myJsFile.getLastChild();
    }
    return element;
  }

  public void startTemplate(@NotNull Template template) {
    TemplateManager templateManager = TemplateManager.getInstance(getProject());
    templateManager.startTemplate(getEditor(), "", template);
  }
}
