package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;

public class PerforceJobFieldValue {
  private final PerforceJobField myField;
  private String myValue;

  public PerforceJobFieldValue(@NotNull PerforceJobField field, @NotNull String value) {
    myField = field;
    myValue = value;
  }

  @NotNull public PerforceJobField getField() {
    return myField;
  }

  @NotNull public @NlsSafe String getValue() {
    return myValue;
  }

  public void setValue(@NotNull final String value) {
    myValue = value;
  }
}
