package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.io.IOUtil;
import org.jetbrains.annotations.NotNull;

class Definition {
  private final LibrarySetItem library;

  public CharSequence[] dependencies;
  public int hasUnresolvedDependencies = UnresolvedState.UNKNOWN;

  private CharSequence timeAsCharSequence;
  public long time = -1;

  public void setTimeAsCharSequence(CharSequence timeAsCharSequence) {
    this.timeAsCharSequence = timeAsCharSequence;
  }

  @SuppressWarnings("UnusedDeclaration")
  public long getTime() {
    if (time == -1) {
      time = IOUtil.parsePositiveLong(timeAsCharSequence);
    }

    return time;
  }

  Definition(final LibrarySetItem library) {
    this.library = library;
  }

  @NotNull
  public LibrarySetItem getLibrary() {
    return library;
  }

  public void markAsUnresolved(CharSequence name) {
    library.filteredDefinitions.add(name);
    library.definitionCounter--;
  }

  static interface UnresolvedState {
    int UNKNOWN = 0;
    int YES = 1;
    int NO = -1;
  }
}
