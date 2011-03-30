package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.AbstractFileManager.FileInfo;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.Nullable;

abstract class AbstractFileManager<I extends FileInfo> {
  private final TIntArrayList freeIndices = new TIntArrayList();
  private int counter;
  private int sessionId;

  protected boolean isRegistered(@Nullable I info) {
    return info != null && info.sessionId == sessionId;
  }

  public void reset(int sessionId) {
    counter = 0;
    freeIndices.resetQuick();
    this.sessionId = sessionId;
  }

  protected void initInfo(I info) {
    info.id = freeIndices.isEmpty() ? counter++ : freeIndices.remove(freeIndices.size() - 1);
    info.sessionId = sessionId;
  }

  protected static class FileInfo {
    private int id;
    private int sessionId;

    public int getId() {
      return id;
    }
  }
}