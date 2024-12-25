// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.imports;

import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DartImportOptimizer implements ImportOptimizer {
  @Override
  public @NotNull Runnable processFile(final @NotNull PsiFile file) {
    DartAnalysisServerService.getInstance(file.getProject()).serverReadyForRequest();
    return new CollectingInfoRunnable() {
      private boolean myFileChanged = false;

      @Override
      public void run() {
        final VirtualFile vFile = DartResolveUtil.getRealVirtualFile(file);
        if (vFile != null) {
          final String filePath = vFile.getPath();
          final SourceFileEdit fileEdit = DartAnalysisServerService.getInstance(file.getProject()).edit_organizeDirectives(filePath);
          if (fileEdit != null) {
            if (AssistUtils.applyFileEdit(file.getProject(), fileEdit)) {
              myFileChanged = true;
              final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
              if (document != null) {
                // Tricky story. Committing a document here is required in order to guarantee that DartPostFormatProcessor.processText() is called afterwards.
                PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);
              }
            }
          }
        }
      }

      @Override
      public @Nullable String getUserNotificationInfo() {
        return myFileChanged ? DartBundle.message("organized.directives") : null;
      }
    };
  }

  @Override
  public boolean supports(@NotNull PsiFile file) {
    return file instanceof DartFile;
  }
}
