package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.abc.BufferWrapper;

class Definition {
  private final LibrarySetItem library;

  // if has only one dependency
  public String dependency;
  // if has > 1
  public String[] dependencies;

  public int resolved = ResolvedState.UNKNOWN;

  private String timeAsString;
  public long time = -1;

  public BufferWrapper doAbcData;

  //public String name;

  public void setTimeAsString(String value) {
    timeAsString = value;
  }

  @SuppressWarnings("UnusedDeclaration")
  public long getTime() {
    if (time == -1) {
      time = Long.parseLong(timeAsString);
    }

    return time;
  }

  Definition(final LibrarySetItem library) {
    this.library = library;
  }

  public LibrarySetItem getLibrary() {
    return library;
  }

  public void markAsUnresolved() {
    if (library != null) {
      library.definitionCounter--;
    }
  }

  static interface ResolvedState {
    int UNKNOWN = 0;
    int YES = 1;
    int NO = -1;
  }
}
