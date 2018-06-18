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
package com.jetbrains.lang.dart.fixes;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
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

    for (DartQuickFix fix: myQuickFixes) {
      fix.setSourceChange(null);
    }

    final Consumer<List<AnalysisErrorFixes>> consumer = fixes -> {
      final long psiModCountWhenReceivedFixes = myPsiManager.getModificationTracker().getModificationCount();
      final long vfsModCountWhenReceivedFixes = VirtualFileManager.getInstance().getModificationCount();
      if (myPsiModCountWhenRequestSent != psiModCountWhenReceivedFixes || myVfsModCountWhenRequestSent != vfsModCountWhenReceivedFixes) {
        return;
      }

      if (fixes == null || fixes.isEmpty()) {
        // Avoid confusion like in https://github.com/dart-lang/sdk/issues/27629, let 'Suppress...' quick fixes be on the 2nd level only.
        //if (myErrorCode != null && myFile.getFileType() == DartFileType.INSTANCE) {
        //  myQuickFixes.get(0).setSuppressActionDelegate(new DartProblemGroup.DartSuppressAction(myErrorCode, myErrorSeverity, false));
        //  myQuickFixes.get(1).setSuppressActionDelegate(new DartProblemGroup.DartSuppressAction(myErrorCode, myErrorSeverity, true));
        //}
      }
      else {
        int index = 0;
        for (AnalysisErrorFixes fix: fixes) {
          for (SourceChange sourceChange: fix.getFixes()) {
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
