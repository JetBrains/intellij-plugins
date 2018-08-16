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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartPubCacheRepairAction extends DartPubActionBase {
  @Override
  @NotNull
  protected String getTitle(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    return DartBundle.message("dart.pub.cache.repair.title");
  }

  @Override
  @Nullable
  protected String[] calculatePubParameters(@NotNull final Project project, @NotNull final VirtualFile pubspecYamlFile) {
    final int choice = Messages.showOkCancelDialog(project, DartBundle.message("dart.pub.cache.repair.message"),
                                                   DartBundle.message("dart.pub.cache.repair.title"), Messages.getWarningIcon());
    return choice == Messages.OK ? new String[]{"cache", "repair"} : null;
  }
}
