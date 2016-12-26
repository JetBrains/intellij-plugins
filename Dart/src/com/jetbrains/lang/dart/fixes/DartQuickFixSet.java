package com.jetbrains.lang.dart.fixes;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
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
  private long myPsiModCount;


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

  void ensureInitialized() {
    final long modCount = myPsiManager.getModificationTracker().getModificationCount();
    if (myPsiModCount == modCount) return;

    myPsiModCount = modCount;

    for (DartQuickFix fix : myQuickFixes) {
      fix.setSourceChange(null);
    }

    final List<AnalysisErrorFixes> fixes = DartAnalysisServerService.getInstance(myPsiManager.getProject()).edit_getFixes(myFile, myOffset);
    if (fixes == null || fixes.isEmpty()) {
      if (myErrorCode != null) {
        myQuickFixes.get(0).setSuppressActionDelegate(new DartProblemGroup.DartSuppressAction(myErrorCode, myErrorSeverity, false));
        myQuickFixes.get(1).setSuppressActionDelegate(new DartProblemGroup.DartSuppressAction(myErrorCode, myErrorSeverity, true));
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
  }
}
