// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.fixes;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.annotator.DartProblemGroup;
import org.dartlang.analysis.server.protocol.AnalysisErrorFixes;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartQuickFixSet {
  private static final int MAX_QUICK_FIXES = 5;

  @NotNull private final PsiManager myPsiManager;
  @NotNull private final VirtualFile myFile;
  private final int myOffset;
  @Nullable private final String myErrorCode;
  @NotNull private final String myErrorSeverity;

  @NotNull private final List<DartQuickFix> myQuickFixes = new ArrayList<>(MAX_QUICK_FIXES);
  private volatile long myPsiModCountWhenRequestSent;
  private volatile long myVfsModCountWhenRequestSent;

  public DartQuickFixSet(@NotNull final PsiManager psiManager,
                         @NotNull final VirtualFile file,
                         final int offset,
                         @Nullable final String errorCode,
                         @NotNull final String errorSeverity) {
    myPsiManager = psiManager;
    myFile = file;
    myOffset = offset;
    myErrorCode = errorCode;
    myErrorSeverity = errorSeverity;

    for (int i = 0; i < MAX_QUICK_FIXES; i++) {
      myQuickFixes.add(new DartQuickFix(this, i));
    }
  }

  @NotNull
  public List<DartQuickFix> getQuickFixes() {
    return myQuickFixes;
  }

  synchronized void ensureInitialized() {
    final long psiModCount = myPsiManager.getModificationTracker().getModificationCount();
    final long vfsModCount = VirtualFileManager.getInstance().getModificationCount();
    if (myPsiModCountWhenRequestSent == psiModCount && myVfsModCountWhenRequestSent == vfsModCount) {
      return;
    }

    myPsiModCountWhenRequestSent = psiModCount;
    myVfsModCountWhenRequestSent = vfsModCount;

    for (DartQuickFix fix : myQuickFixes) {
      fix.setSourceChange(null);
    }

    final Consumer<List<AnalysisErrorFixes>> consumer = fixes -> {
      final long psiModCountWhenReceivedFixes = myPsiManager.getModificationTracker().getModificationCount();
      final long vfsModCountWhenReceivedFixes = VirtualFileManager.getInstance().getModificationCount();

      if (psiModCount != psiModCountWhenReceivedFixes || vfsModCount != vfsModCountWhenReceivedFixes) {
        return;
      }

      if (fixes == null || fixes.isEmpty()) {
        if (myErrorCode != null && myFile.getFileType() == DartFileType.INSTANCE) {
          myQuickFixes.get(0).setSuppressActionDelegate(new DartProblemGroup.DartSuppressAction(myErrorCode, myErrorSeverity, true, false));
          //myQuickFixes.get(1).setSuppressActionDelegate(new DartProblemGroup.DartSuppressAction(myErrorCode, myErrorSeverity, true, true));
        }
      }
      else {
        int index = 0;
        for (AnalysisErrorFixes fix : fixes) {
          for (SourceChange sourceChange : fix.getFixes()) {
            myQuickFixes.get(index).setSourceChange(sourceChange);
            index++;
            if (index == MAX_QUICK_FIXES) return;
          }
        }
      }
    };

    DartAnalysisServerService.getInstance(myPsiManager.getProject()).askForFixesAndWaitABitIfReceivedQuickly(myFile, myOffset, consumer);
  }
}
