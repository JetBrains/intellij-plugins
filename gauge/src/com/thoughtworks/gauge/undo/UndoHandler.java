/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.undo;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UndoHandler {
  private final List<String> fileNames;
  private final Project project;
  private final @Nls String name;

  public UndoHandler(List<String> fileNames, Project project, @NlsContexts.Command String name) {
    this.fileNames = fileNames;
    this.project = project;
    this.name = name;
  }

  public void handle() {
    refreshFiles();
    runWriteAction();
  }

  private void refreshFiles() {
    final Map<Document, String> documentTextMap = new HashMap<>();
    for (String fileName : fileNames) {
      VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(new File(fileName));
      if (fileByIoFile != null) {
        Document document = FileDocumentManager.getInstance().getDocument(fileByIoFile);
        if (document != null) documentTextMap.put(document, document.getText());
      }
    }
    VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
    ApplicationManager.getApplication().runWriteAction(() -> {
      for (Document document : documentTextMap.keySet()) {
        document.setText(documentTextMap.get(document));
      }
    });
  }


  private void runWriteAction() {
    ApplicationManager.getApplication().runWriteAction(
      () -> CommandProcessor.getInstance().executeCommand(project, () ->
        performUndoableAction(fileNames), name, name
      )
    );
  }

  private static void performUndoableAction(List<String> filesChangedList) {
    for (String fileName : filesChangedList) {
      LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
      try {
        VirtualFile virtualFile = localFileSystem.findFileByIoFile(new File(fileName));
        if (virtualFile != null) {
          Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
          localFileSystem.refreshAndFindFileByIoFile(new File(fileName));
          if (document != null) {
            Charset encoding = EncodingManager.getInstance().getEncoding(virtualFile, true);
            if (encoding == null) {
              encoding = StandardCharsets.UTF_8;
            }

            document.setText(StringUtils.join(FileUtils.readLines(new File(fileName), encoding.toString()).toArray(), "\n"));
          }
        }
      }
      catch (Exception ex) {
        Logger.getInstance(UndoHandler.class).warn("Error during undo in Gauge", ex);
      }
    }
  }
}
