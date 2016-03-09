package com.jetbrains.lang.dart.fixes;

import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.dartlang.analysis.server.protocol.AnalysisErrorFixes;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DartQuickFixSet {
  private static final int MAX_QUICK_FIXES = 5;

  @NotNull private final String myFilePath;
  private final int myOffset;

  private boolean myInitialized = false;
  @NotNull private final List<DartQuickFix> myQuickFixes = new ArrayList<DartQuickFix>(MAX_QUICK_FIXES);

  private final long myPsiModificationCount;

  public DartQuickFixSet(@NotNull final String filePath, final int offset, final long psiModificationCount) {
    myFilePath = filePath;
    myOffset = offset;
    myPsiModificationCount = psiModificationCount;

    for (int i = 0; i < MAX_QUICK_FIXES; i++) {
      myQuickFixes.add(new DartQuickFix(this, i));
    }
  }

  @NotNull
  public List<DartQuickFix> getQuickFixes() {
    return myQuickFixes;
  }

  public long getPsiModificationCount() {
    return myPsiModificationCount;
  }

  void ensureInitialized() {
    if (myInitialized) return;
    myInitialized = true;

    final List<AnalysisErrorFixes> fixes = DartAnalysisServerService.getInstance().edit_getFixes(myFilePath, myOffset);
    if (fixes != null) {
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
