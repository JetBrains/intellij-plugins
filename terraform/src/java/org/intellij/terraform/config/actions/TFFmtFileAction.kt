// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EmptyConsumer;
import org.intellij.terraform.config.util.TFExecutor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TFFmtFileAction extends TFExternalToolsAction {

  protected TFExecutor createExecutor(@NotNull Project project, @Nullable Module module, @NotNull @Nls String title, @NotNull VirtualFile virtualFile) {
    String filePath = virtualFile.getCanonicalPath();
    assert filePath != null;
    return TFExecutor.in(project, module)
      .withPresentableName(title)
      .withParameters("fmt", filePath)
      .showOutputOnError()
      .withWorkDirectory(virtualFile.getParent().getCanonicalPath());
  }


  @Override
  protected void invoke(@NotNull Project project,
                        @Nullable Module module,
                        @NotNull @Nls String title,
                        @NotNull VirtualFile virtualFile) throws ExecutionException {
    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    if (document != null) {
      FileDocumentManager.getInstance().saveDocument(document);
    } else {
      FileDocumentManager.getInstance().saveAllDocuments();
    }

    createExecutor(project, module, title, virtualFile).executeWithProgress(false,
                                                                            aBoolean -> {
                                                                              EmptyConsumer.<Boolean>getInstance().consume(aBoolean);
                                                                              VfsUtil.markDirtyAndRefresh(true, true, true, virtualFile);
                                                                            });
  }
}