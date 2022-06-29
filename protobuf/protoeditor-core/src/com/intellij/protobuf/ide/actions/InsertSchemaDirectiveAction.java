/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.actions;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.protobuf.lang.psi.PbTextFile;
import com.intellij.protobuf.lang.resolve.directive.SchemaComment;
import com.intellij.protobuf.lang.resolve.directive.SchemaDirective;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * An {@link AnAction action} that inserts <code>proto-file</code> and <code>proto-message</code>
 * comments into a text format file using a live template to guide the user.
 *
 * <p>Comments are inserted at the start of the file. If either of the comments already exist,
 * existing values will be used and the comments will be moved to the end of the first comment
 * block.
 */
public class InsertSchemaDirectiveAction extends AnAction {

  public static final String ACTION_ID = "prototext.InsertSchemaDirective";

  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = event.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      return;
    }
    Editor editor = event.getData(CommonDataKeys.EDITOR);
    if (editor == null) {
      return;
    }
    PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
    if (!(file instanceof PbTextFile)) {
      return;
    }
    PbTextFile textFile = (PbTextFile) file;
    if (!textFile.isWritable()) {
      return;
    }

    WriteCommandAction.runWriteCommandAction(
        project,
        event.getPresentation().getText(),
        /* groupID= */ null,
        () -> insertFileAnnotation(project, textFile, editor));
  }

  /** Update action visibility: only show for physical, writeable text format files. */
  @Override
  public void update(AnActionEvent event) {
    PsiFile file = event.getData(CommonDataKeys.PSI_FILE);
    if (!(file instanceof PbTextFile)) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }
    PbTextFile textFile = (PbTextFile) file;
    if (!textFile.isWritable()) {
      event.getPresentation().setEnabledAndVisible(false);
      return;
    }

    event.getPresentation().setEnabledAndVisible(true);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  private static void insertFileAnnotation(Project project, PbTextFile file, Editor editor) {
    PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
    SchemaDirective directive = SchemaDirective.find(file);
    String existingFilename = null;
    String existingMessageName = null;
    Set<String> existingImportNames = new TreeSet<>();
    if (directive != null) {
      existingFilename = directive.getFilename();
      existingMessageName = directive.getMessageName();
      removeComment(directive.getFileComment());
      removeComment(directive.getMessageComment());
      for (SchemaComment importComment : directive.getImportComments()) {
        String name = importComment.getName();
        if (name != null) {
          existingImportNames.add(name);
        }
        removeComment(importComment);
      }
      PsiDocumentManager.getInstance(file.getProject())
          .doPostponedOperationsAndUnblockDocument(editor.getDocument());
    }
    PsiElement firstChild = file.getFirstChild();
    boolean existingCommentAtTop = firstChild instanceof PsiComment && firstChild.getTextOffset() == 0;
    Template template =
        createFileAnnotationTemplate(
            file.getProject(), existingFilename, existingMessageName, existingImportNames, existingCommentAtTop);

    editor.getCaretModel().moveToOffset(0);
    TemplateManager.getInstance(file.getProject()).startTemplate(editor, template);
  }

  private static void removeComment(SchemaComment comment) {
    if (comment == null) {
      return;
    }
    PsiElement element = comment.getComment();
    element.delete();
  }

  private static Template createFileAnnotationTemplate(
      Project project, String filename, String messageName, Collection<String> importNames,
      boolean existingCommentAtTop) {
    StringBuilder templateBuilder = new StringBuilder();
    templateBuilder
        .append("# proto-file: ")
        .append("$FILE$")
        .append('\n');
    templateBuilder
        .append("# proto-message: ")
        .append("$MESSAGE$")
        .append('\n');
    for (String importName : importNames) {
      templateBuilder.append("# proto-import: ").append(importName).append('\n');
    }
    if (existingCommentAtTop) {
      templateBuilder.append("#\n");
    } else {
      templateBuilder.append('\n');
    }
    Template template =
        TemplateManager.getInstance(project)
            .createTemplate(/* key= */ "", /* group= */ "", templateBuilder.toString());
    template.addVariable(
            "FILE",
            "complete()",
            '"' + StringUtil.notNullize(filename) + '"',
            /* isAlwaysStopAt= */ StringUtil.isEmptyOrSpaces(filename)
    );
    template.addVariable(
            "MESSAGE",
            "complete()",
            '"' + StringUtil.notNullize(messageName) + '"',
            /* isAlwaysStopAt= */ StringUtil.isEmptyOrSpaces(messageName)
    );
    return template;
  }
}
