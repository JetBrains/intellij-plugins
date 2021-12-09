/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.idea.perforce.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


public class TimeLapseViewAction extends RevisionGraphAction {
  @Override @NonNls
  protected String getCommandName() {
    return "annotate";
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    super.update(e);
    VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
    e.getPresentation().setEnabled(e.getPresentation().isEnabled() && vf != null && !vf.getFileType().isBinary() && !vf.isDirectory());
  }
}