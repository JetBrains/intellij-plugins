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
package com.intellij.protobuf.ide.editing;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.containers.WeakList;
import com.intellij.protobuf.ide.PbIdeBundle;
import com.intellij.protobuf.ide.actions.InsertSchemaDirectiveAction;
import com.intellij.protobuf.ide.settings.PbTextLanguageSettings;
import com.intellij.protobuf.ide.settings.PbTextLanguageSettingsConfigurable;
import com.intellij.protobuf.lang.psi.PbTextFile;
import com.intellij.protobuf.lang.resolve.directive.SchemaDirective;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkListener;
import java.util.Collection;

/**
 * Provides an editor notification (a bar at the top of the editor) when a text format file is
 * opened without an associated root message. The notification provides some remediation actions.
 */
public class MissingSchemaNotificationProvider
    extends EditorNotifications.Provider<EditorNotificationPanel> {

  private static final Key<EditorNotificationPanel> KEY =
      Key.create("prototext.missing.schema.notification");
  private final Collection<VirtualFile> ignoredFiles = new WeakList<>();

  /**
   * Updates the notification for the given file. If the file no longer needs a notification, it
   * will be removed.
   *
   * @param file the file whose notifications should be reevaluated.
   */
  public static void update(PbTextFile file) {
    if (file == null) {
      return;
    }
    VirtualFile virtualFile = file.getVirtualFile();
    if (virtualFile == null) {
      return;
    }
    EditorNotifications.getInstance(file.getProject()).updateNotifications(virtualFile);
  }

  @NotNull
  @Override
  public Key<EditorNotificationPanel> getKey() {
    return KEY;
  }

  @Nullable
  @Override
  public EditorNotificationPanel createNotificationPanel(
      @NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor, @NotNull Project project) {
    if (ignoredFiles.contains(virtualFile)) {
      return null;
    }
    if (!(fileEditor instanceof TextEditor)) {
      return null;
    }
    TextEditor textEditor = (TextEditor) fileEditor;
    Editor editor = textEditor.getEditor();
    PbTextLanguageSettings settings = PbTextLanguageSettings.getInstance(project);
    if (settings == null || !settings.isMissingSchemaWarningEnabled()) {
      return null;
    }

    Document document = editor.getDocument();
    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
    PsiFile file = documentManager.getPsiFile(document);
    if (!(file instanceof PbTextFile)) {
      return null;
    }
    PbTextFile textFile = (PbTextFile) file;

    SchemaDirective existingDirective = SchemaDirective.find(textFile);
    if (existingDirective != null) {
      // One or both of the comments already exists, and should be annotated with warnings if
      // they're not correct. Defer to that and don't show the top notification bar.
      return null;
    }

    return createPanelForTextFormatFile(textFile);
  }

  private EditorNotificationPanel createPanelForTextFormatFile(PbTextFile file) {
    if (file.isBound()) {
      // File has a schema association, so we don't create the notification.
      return null;
    }

    EditorNotificationPanel panel = new EditorNotificationPanel();
    panel.setText(PbIdeBundle.message("prototext.missing.schema.message"));
    HyperlinkListener closingListener =
        (event) ->
            EditorNotifications.getInstance(file.getProject())
                .updateNotifications(file.getVirtualFile());
    // TODO(volkman): also support a project-level configuration mechanism that will work for
    // readonly files.
    if (file.isWritable()) {
      panel
          .createActionLabel(
              PbIdeBundle.message("prototext.missing.schema.insert.annotation"),
              InsertSchemaDirectiveAction.ACTION_ID)
          .addHyperlinkListener(closingListener);
    }
    PbTextLanguageSettings settings = PbTextLanguageSettings.getInstance(file.getProject());
    if (settings != null) {
      panel
          .createActionLabel(
              PbIdeBundle.message("prototext.missing.schema.settings"),
              () ->
                  ShowSettingsUtil.getInstance()
                      .showSettingsDialog(
                          file.getProject(), PbTextLanguageSettingsConfigurable.class))
          .addHyperlinkListener(closingListener);
    }
    panel
        .createActionLabel(
            PbIdeBundle.message("prototext.missing.schema.ignore"),
            () -> ignoredFiles.add(file.getVirtualFile()))
        .addHyperlinkListener(closingListener);
    return panel;
  }
}
