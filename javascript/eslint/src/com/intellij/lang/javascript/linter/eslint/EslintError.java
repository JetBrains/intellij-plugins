package com.intellij.lang.javascript.linter.eslint;

import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.javascript.linter.JSLinterError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EslintError extends JSLinterError {

  private final @Nullable FixInfo myFixInfo;
  private final @NotNull List<FixInfo> mySuggestions;

  public EslintError(int oneBasedLineNumber,
                     int oneBasedColumnNumber,
                     int oneBasedEndLineNumber,
                     int oneBasedEndColumnNumber,
                     @NotNull @InspectionMessage String description,
                     @Nullable String code,
                     @Nullable HighlightSeverity severity,
                     @Nullable FixInfo fixInfo,
                     @NotNull List<FixInfo> suggestions) {
    super(oneBasedLineNumber, oneBasedColumnNumber, oneBasedEndLineNumber, oneBasedEndColumnNumber, description, code, severity);
    myFixInfo = fixInfo;
    mySuggestions = suggestions;
  }

  public @Nullable FixInfo getFixInfo() {
    return myFixInfo;
  }

  public @NotNull List<FixInfo> getSuggestions() {
    return mySuggestions;
  }

  public static class FixInfo {
    final @Nullable @IntentionName String description;
    final int startOffset;
    final int endOffset;
    final @NotNull String replacementText;

    public FixInfo(@Nullable @IntentionName String description, int startOffset, int endOffset, @NotNull String replacementText) {
      this.description = description;
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      this.replacementText = replacementText;
    }
  }
}
