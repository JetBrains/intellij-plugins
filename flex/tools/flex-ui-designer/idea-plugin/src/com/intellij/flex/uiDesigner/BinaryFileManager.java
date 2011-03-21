package com.intellij.flex.uiDesigner;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;

public class BinaryFileManager extends AbstractFileManager<AbstractFileManager.FileInfo> {
  private static final Key<FileInfo> INFO = Key.create("FUD_BINARY_FILE_INFO");
  
  public static BinaryFileManager getInstance() {
    return ServiceManager.getService(BinaryFileManager.class);
  }
  
  public boolean isRegistered(VirtualFile virtualFile) {
    return isRegistered(virtualFile.getUserData(INFO));
  }
  
  public FileInfo getId(VirtualFile virtualFile) {
    FileInfo info = virtualFile.getUserData(INFO);
    if (!isRegistered(info)) {
      if (info == null) {
        info = new FileInfo();
      }

      initInfo(info);

      virtualFile.putUserData(INFO, info);
    }

    return info;
  }
}
