/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.jetbrains.lang.dart.assists;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.dartlang.analysis.server.protocol.SourceEdit;
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AssistUtils {
  public static void applySourceChange(@NotNull final Project project, @NotNull final SourceChange sourceChange) {
    CommandProcessor.getInstance().executeCommand(project, new Runnable() {
      @Override
      public void run() {
        final List<SourceFileEdit> fileEdits = sourceChange.getEdits();
        for (SourceFileEdit fileEdit : fileEdits) {
          final String filePath = FileUtil.toSystemIndependentName(fileEdit.getFile());
          final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
          if (file != null) {
            final Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document != null) {
              applySourceEdits(document, fileEdit.getEdits());
            }
          }
        }
      }
    }, sourceChange.getMessage(), null);
  }

  public static void applySourceEdits(@NotNull final Document document, @NotNull final List<SourceEdit> edits) {
    for (SourceEdit edit : edits) {
      final int offset = edit.getOffset();
      final int length = edit.getLength();
      document.replaceString(offset, offset + length, edit.getReplacement());
    }
  }
}
