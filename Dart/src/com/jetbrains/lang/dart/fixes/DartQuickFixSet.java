// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.fixes;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.annotator.DartProblemGroup;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.dartlang.analysis.server.protocol.AnalysisErrorFixes;
import org.dartlang.analysis.server.protocol.AnalysisErrorSeverity;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DartQuickFixSet {
  private static final String MIN_SDK_VERSION_WITH_IGNORE_FIXES = "2.14";
  private static final String DART_FIX_IGNORE_PREFIX = "dart.fix.ignore";
  private static final int MAX_QUICK_FIXES = 5;

  private final @NotNull PsiManager myPsiManager;
  private final @NotNull VirtualFile myFile;
  private final int myOffset;
  private final @Nullable String myErrorCode;
  private final @NotNull DartProblemGroup myProblemGroup;

  private final @NotNull List<DartQuickFix> myQuickFixes = new ArrayList<>(MAX_QUICK_FIXES);
  private volatile long myPsiModCountWhenRequestSent;
  private volatile long myVfsModCountWhenRequestSent;

  public DartQuickFixSet(@NotNull PsiManager psiManager,
                         @NotNull VirtualFile file,
                         int offset,
                         @Nullable String errorCode) {
    myPsiManager = psiManager;
    myFile = file;
    myOffset = offset;
    myErrorCode = errorCode;
    myProblemGroup = new DartProblemGroup(errorCode);

    for (int i = 0; i < MAX_QUICK_FIXES; i++) {
      myQuickFixes.add(new DartQuickFix(this, i));
    }
  }

  public @NotNull List<DartQuickFix> getQuickFixes() {
    return myQuickFixes;
  }

  public @NotNull DartProblemGroup getProblemGroup() {
    return myProblemGroup;
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

      DartSdk sdk = DartSdk.getDartSdk(myPsiManager.getProject());
      boolean ownSuppressActions = sdk != null && StringUtil.compareVersionNumbers(sdk.getVersion(), MIN_SDK_VERSION_WITH_IGNORE_FIXES) < 0;

      if (fixes == null || fixes.isEmpty()) return;

      int index = 0;
      for (AnalysisErrorFixes fix : fixes) {
        if (!Objects.equals(fix.getError().getCode(), myErrorCode)) continue;

        if (ownSuppressActions && !AnalysisErrorSeverity.ERROR.equals(fix.getError().getSeverity())) {
          myProblemGroup.setShowOwnSuppressActions(true);
        }

        for (SourceChange sourceChange : fix.getFixes()) {
          String changeId = sourceChange.getId();
          if (changeId != null && changeId.startsWith(DART_FIX_IGNORE_PREFIX)) {
            myProblemGroup.addIgnoreFix(sourceChange);
          }
          else {
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
