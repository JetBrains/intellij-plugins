package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class BinaryFileManager extends EntityListManager<VirtualFile, AssetInfo> {
  public static BinaryFileManager getInstance() {
    return ServiceManager.getService(BinaryFileManager.class);
  }

  public boolean isRegistered(@NotNull VirtualFile virtualFile) {
    return list.contains(virtualFile);
  }

  public int getId(@NotNull VirtualFile virtualFile) {
    return list.getId(virtualFile);
  }

  public AssetInfo getInfo(int id) {
    return list.getInfo(id);
  }

  public int add(@NotNull VirtualFile virtualFile) {
    return list.add(new AssetInfo(virtualFile));
  }

  public int add(@NotNull VirtualFile virtualFile, String mimeType) {
    return list.add(new AssetInfo(virtualFile, mimeType));
  }

  public int registerFile(@NotNull VirtualFile virtualFile, BinaryFileType type) throws InvalidPropertyException {
    int id = list.add(new AssetInfo(virtualFile));
    try {
      FlexUIDesignerApplicationManager.getInstance().getClient().registerBinaryFile(id, virtualFile, type);
      return id;
    }
    catch (IOException e) {
      throw new InvalidPropertyException(e, "error.cannot.write.binary.file", virtualFile.getName());
    }
  }
}

class AssetInfo extends InfoList.Info<VirtualFile> {
  public final String mimeType;

  public AssetInfo(VirtualFile element) {
    this(element, null);
  }

  public AssetInfo(VirtualFile virtualFile, @Nullable String mimeType) {
    super(virtualFile);
    this.mimeType = mimeType;
  }
}