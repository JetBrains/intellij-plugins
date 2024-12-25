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

  public @NotNull PerforceJobField getField() {
    return myField;
  }

  public @NotNull @NlsSafe String getValue() {
    return myValue;
  }

  public void setValue(final @NotNull String value) {
    myValue = value;
  }
}
