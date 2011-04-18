package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class BinaryFileManager extends EntityListManager<VirtualFile, InfoList.Info<VirtualFile>> {
  public static BinaryFileManager getInstance() {
    return ServiceManager.getService(BinaryFileManager.class);
  }

  public boolean isRegistered(@NotNull VirtualFile virtualFile) {
    return list.contains(virtualFile);
  }

  public int getId(@NotNull VirtualFile virtualFile) {
    return list.getId(virtualFile);
  }

  public int add(@NotNull VirtualFile virtualFile) {
    return list.add(new InfoList.Info<VirtualFile>(virtualFile));
  }

  public int registerFile(@NotNull VirtualFile virtualFile, BinaryFileType type) throws InvalidPropertyException {
    int id = list.add(new InfoList.Info<VirtualFile>(virtualFile));
    try {
      FlexUIDesignerApplicationManager.getInstance().getClient().registerBinaryFile(id, virtualFile, type);
      return id;
    }
    catch (IOException e) {
      throw new InvalidPropertyException(e, "error.cannot.write.binary.file", virtualFile.getName());
    }
  }
}