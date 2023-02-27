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

import com.intellij.CommonBundle;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.ui.BooleanCommitOption;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PairConsumer;
import com.intellij.util.ui.UIUtil;
import org.intellij.terraform.hcl.HCLBundle;
import org.intellij.terraform.hcl.psi.HCLFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TFFmtCheckinFactory extends CheckinHandlerFactory {
  private static final String TF_FMT = "TF_FMT";
  private static final Set<String> SUPPORTED_FILE_EXTENSIONS = Set.of("tf", "tfvars");

  @Override
  @NotNull
  public CheckinHandler createHandler(@NotNull final CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
    return new CheckinHandler() {
      private static final @NlsSafe String terraformFmt = "Terraform fmt";

      @Override
      public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
        return BooleanCommitOption.create(panel.getProject(), this, false, terraformFmt,
                                          () -> enabled(panel),
                                          value -> PropertiesComponent.getInstance(panel.getProject()).setValue(TF_FMT, value, false));
      }

      @Override
      public ReturnResult beforeCheckin(@Nullable CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
        if (enabled(panel)) {
          final Ref<Boolean> success = Ref.create(true);
          FileDocumentManager.getInstance().saveAllDocuments();
          for (PsiFile file : getPsiFiles()) {
            VirtualFile virtualFile = file.getVirtualFile();
            new TFFmtFileAction().doSomething(virtualFile, ModuleUtilCore.findModuleForPsiElement(file), file.getProject(), terraformFmt, true,
                result -> {
                  if (!result) success.set(false);
                });
          }
          if (!success.get()) {
            return showErrorMessage(executor);
          }
        }
        return super.beforeCheckin();
      }

      @NotNull
      private ReturnResult showErrorMessage(@Nullable CommitExecutor executor) {
        String[] buttons = new String[] {
            HCLBundle.message("terraform.fmt.commit.error.details.caption"), commitButtonMessage(executor, panel),
            CommonBundle.getCancelButtonText()
        };
        int answer = Messages.showDialog(panel.getProject(),
                                         HCLBundle.message("terraform.fmt.commit.error.message"),
                                         StringUtil.capitalizeWords(terraformFmt, true), null, buttons, 0, 1, UIUtil.getWarningIcon());
        if (answer == Messages.OK) {
          return ReturnResult.CLOSE_WINDOW;
        }
        if (answer == Messages.NO) {
          return ReturnResult.COMMIT;
        }
        return ReturnResult.CANCEL;
      }

      @NotNull
      private List<PsiFile> getPsiFiles() {
        Collection<VirtualFile> files = panel.getVirtualFiles();
        List<PsiFile> psiFiles = new ArrayList<>();
        PsiManager manager = PsiManager.getInstance(panel.getProject());
        for (VirtualFile file : files) {
          if (!SUPPORTED_FILE_EXTENSIONS.contains(file.getExtension())) continue;
          PsiFile psiFile = manager.findFile(file);
          if (psiFile instanceof HCLFile) {
            psiFiles.add(psiFile);
          }
        }
        return psiFiles;
      }
    };
  }

  @NotNull
  private static @NlsSafe String commitButtonMessage(@Nullable CommitExecutor executor, @NotNull CheckinProjectPanel panel) {
    return StringUtil.trimEnd(executor != null ? executor.getActionText() : panel.getCommitActionName(), "...");
  }

  private static boolean enabled(@NotNull CheckinProjectPanel panel) {
    return PropertiesComponent.getInstance(panel.getProject()).getBoolean(TF_FMT, false);
  }
}