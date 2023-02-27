/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package org.intellij.terraform.config.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.EmptyConsumer;
import com.intellij.util.ExceptionUtil;
import org.intellij.terraform.config.TerraformFileType;
import org.intellij.terraform.config.util.TFExecutor;
import org.intellij.terraform.hcl.HCLFileType;
import org.intellij.terraform.config.TerraformConstants;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("WeakerAccess")
public abstract class TFExternalToolsAction extends DumbAwareAction {
  private static final Logger LOG = Logger.getInstance(TFExternalToolsAction.class);

  private static void error(@NotNull @Nls String title, @NotNull Project project, @Nullable Exception ex) {
    String message = ex == null ? "" : ExceptionUtil.getUserStackTrace(ex, LOG);
    TerraformConstants.EXECUTION_NOTIFICATION_GROUP.createNotification(title, message, NotificationType.ERROR).notify(project);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
    if (project == null || file == null || !isAvailableOnFile(file, true)) {
      e.getPresentation().setEnabled(false);
      return;
    }
    e.getPresentation().setEnabled(true);
  }

  protected boolean isAvailableOnFile(@NotNull final VirtualFile file, boolean checkDirChildren) {
    if (!file.isInLocalFileSystem()) return false;
    if (file.isDirectory()) {
      if (!checkDirChildren) return false;
      //noinspection UnsafeVfsRecursion
      VirtualFile[] children = file.getChildren();
      if (children != null) {
        for (VirtualFile child : children) {
          if (isAvailableOnFile(child, false)) return true;
        }
      }
      return false;
    }
    return FileTypeRegistry.getInstance().isFileOfType(file, HCLFileType.INSTANCE) || FileTypeRegistry.getInstance().isFileOfType(file, TerraformFileType.INSTANCE);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    VirtualFile file = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE);
    assert project != null;
    String title = StringUtil.notNullize(e.getPresentation().getText());

    Module module = ModuleUtilCore.findModuleForFile(file, project);
    try {
      doSomething(file, module, project, title);
    } catch (ExecutionException ex) {
      error(title, project, ex);
      LOG.error(ex);
    }
  }

  protected boolean doSomething(@NotNull VirtualFile virtualFile,
                                @Nullable Module module,
                                @NotNull Project project,
                                @NotNull @Nls String title) throws ExecutionException {
    return doSomething(virtualFile, module, project, title, false);
  }

  private boolean doSomething(@NotNull VirtualFile virtualFile,
                              @Nullable Module module,
                              @NotNull Project project,
                              @NotNull @Nls String title,
                              boolean withProgress) {
    return doSomething(virtualFile, module, project, title, withProgress, EmptyConsumer.getInstance());
  }

  protected boolean doSomething(@NotNull final VirtualFile virtualFile,
                                @Nullable Module module,
                                @NotNull Project project,
                                @NotNull @Nls String title,
                                boolean withProgress,
                                @NotNull final Consumer<Boolean> consumer) {
    Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    if (document != null) {
      FileDocumentManager.getInstance().saveDocument(document);
    } else {
      FileDocumentManager.getInstance().saveAllDocuments();
    }

    createExecutor(project, module, title, virtualFile).executeWithProgress(withProgress,
        aBoolean -> {
          consumer.consume(aBoolean);
          VfsUtil.markDirtyAndRefresh(true, true, true, virtualFile);
        });
    return true;
  }

  protected TFExecutor createExecutor(@NotNull Project project,
                                      @Nullable Module module,
                                      @NotNull @Nls String title,
                                      @NotNull VirtualFile virtualFile) {
    String filePath = virtualFile.getCanonicalPath();
    assert filePath != null;
    return createExecutor(project, module, title, filePath);
  }

  @NotNull
  protected abstract TFExecutor createExecutor(@NotNull Project project,
                                               @Nullable Module module,
                                               @NotNull @Nls String title,
                                               @NotNull String filePath);
}
