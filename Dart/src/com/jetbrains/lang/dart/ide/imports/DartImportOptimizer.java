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
package com.jetbrains.lang.dart.ide.imports;

import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartImportOptimizer implements ImportOptimizer {
  @NotNull
  @Override
  public Runnable processFile(final PsiFile file) {
    DartAnalysisServerService.getInstance(file.getProject()).serverReadyForRequest(file.getProject());
    return new CollectingInfoRunnable() {
      private boolean myFileChanged = false;

      @Override
      public void run() {
        final VirtualFile vFile = DartResolveUtil.getRealVirtualFile(file);
        if (vFile != null) {
          final String filePath = vFile.getPath();
          final SourceFileEdit fileEdit = DartAnalysisServerService.getInstance(file.getProject()).edit_organizeDirectives(filePath);
          if (fileEdit != null) {
            myFileChanged = AssistUtils.applyFileEdit(file.getProject(), fileEdit);
          }
        }
      }

      @Nullable
      @Override
      public String getUserNotificationInfo() {
        return myFileChanged ? DartBundle.message("organized.directives") : null;
      }
    };
  }

  @Override
  public boolean supports(PsiFile file) {
    return file instanceof DartFile;
  }
}
