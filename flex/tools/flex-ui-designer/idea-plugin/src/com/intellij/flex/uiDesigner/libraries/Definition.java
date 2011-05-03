package com.intellij.flex.uiDesigner.libraries;

import org.jetbrains.annotations.NotNull;

class Definition {
  private final OriginalLibrary library;

  public CharSequence[] dependencies;
  public int hasUnresolvedDependencies = UnresolvedState.UNKNOWN;

  private CharSequence timeAsCharSequence;
  public long time = -1;

  public void setTimeAsCharSequence(CharSequence timeAsCharSequence) {
    this.timeAsCharSequence = timeAsCharSequence;
  }

  public long getTime() {
    if (time == -1) {
      time = Long.parseLong(timeAsCharSequence.toString());
    }

    return time;
  }

  Definition(final OriginalLibrary library) {
    this.library = library;
  }

  @NotNull
  public OriginalLibrary getLibrary() {
    return library;
  }

  public void markAsUnresolved(CharSequence name) {
    library.unresolvedDefinitions.add(name);
    library.definitionCounter--;
  }

  static class UnresolvedState {
    public static int UNKNOWN = 0;
    public static int YES = 1;
    public static int NO = -1;
  }
}
