package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;

public class BinaryFileManager {
  private final InfoList<VirtualFile, InfoList.Info<VirtualFile>> files = new InfoList<VirtualFile, InfoList.Info<VirtualFile>>();

  public void reset() {
    files.clear();
  }

  public static BinaryFileManager getInstance() {
    return ServiceManager.getService(BinaryFileManager.class);
  }

  public boolean isRegistered(VirtualFile virtualFile) {
    return files.contains(virtualFile);
  }

  public int getId(VirtualFile virtualFile) {
    return files.getId(virtualFile);
  }

  public int add(VirtualFile virtualFile) {
    return files.add(new InfoList.Info<VirtualFile>(virtualFile));
  }
}
