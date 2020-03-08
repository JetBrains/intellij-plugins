// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.pubServer.DartWebdev;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartPubBuildAction extends DartPubActionBase {
  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    final Project project = e.getProject();
    if (project != null && DartWebdev.INSTANCE.useWebdev(DartSdk.getDartSdk(project))) {
      e.getPresentation().setText(DartBundle.message("action.text.webdev.build"));
      e.getPresentation().setDescription(DartBundle.message("action.description.run.webdev.build"));
    }
    else {
      e.getPresentation().setText(DartBundle.message("action.test.pub.build"));
      e.getPresentation().setDescription(DartBundle.message("action.description.run.pub.build"));
    }
  }

  @Override
  @NotNull
  protected String getTitle(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    final String projectName = PubspecYamlUtil.getDartProjectName(pubspecYamlFile);
    final String prefix = projectName == null ? "" : ("[" + projectName + "] ");
    return prefix + DartBundle
      .message(DartWebdev.INSTANCE.useWebdev(DartSdk.getDartSdk(project)) ? "dart.webdev.build.title" : "dart.pub.build.title");
  }

  @Override
  protected String @Nullable [] calculatePubParameters(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    final DartPubBuildDialog dialog = new DartPubBuildDialog(project, pubspecYamlFile.getParent());
    dialog.show();
    if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
      return null;
    }

    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null) return null; // can't happen, already checked

    if (DartWebdev.INSTANCE.useWebdev(sdk)) {
      if (!DartWebdev.INSTANCE.ensureWebdevActivated(project)) return null;

      return new String[]{"global", "run", "webdev", "build", "--output=" + dialog.getInputFolder() + ":" + dialog.getOutputFolder()};
    }

    return new String[]{"build", "--mode=" + dialog.getPubBuildMode(), "--output=" + dialog.getOutputFolder()};
  }
}
