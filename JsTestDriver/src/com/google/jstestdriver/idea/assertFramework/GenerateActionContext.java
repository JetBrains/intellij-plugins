package com.google.jstestdriver.idea.assertFramework;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class GenerateActionContext {
  private final JSFile myJsFile;
  private final Editor myEditor;

  GenerateActionContext(@NotNull JSFile jsFile, @NotNull Editor editor) {
    myJsFile = jsFile;
    myEditor = editor;
  }

  @NotNull
  public JSFile getJsFile() {
    return myJsFile;
  }

  public int getCaretOffsetInDocument() {
    return myEditor.getCaretModel().getOffset();
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

  public void startTemplate(@NotNull Template template) {
    TemplateManager templateManager = TemplateManager.getInstance(getProject());
    templateManager.startTemplate(getEditor(), "", template);
  }
}
