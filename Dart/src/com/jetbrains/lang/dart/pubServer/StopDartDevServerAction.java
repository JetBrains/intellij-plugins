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
package com.jetbrains.lang.dart.pubServer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

public class StopDartDevServerAction extends DumbAwareAction {

  public StopDartDevServerAction() {
    super(DartBundle.message("stop.dart.dev.server.action.text"), null, AllIcons.Actions.Suspend);
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabled(e.getProject() != null && PubServerManager.getInstance(e.getProject()).hasAlivePubServerProcesses());
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    if (e.getProject() != null) {
      PubServerManager.getInstance(e.getProject()).stopAllPubServerProcesses();
    }
  }
}
