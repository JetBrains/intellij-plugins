package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class OriginalLibrary extends InfoList.Info<VirtualFile> implements Library {
  public static final String DEFAULTS_CSS = "defaults.css";
  private static final String CATALOG = "catalog.xml";
  private static final String SWF = "library.swf";

  public byte[] inheritingStyles;
  public byte[] defaultsStyle;

  private final String path;
  private final boolean fromSdk;

  public OriginalLibrary(String relativePath, VirtualFile file, boolean fromSdk) {
    super(file);

    this.path = relativePath;
    this.fromSdk = fromSdk;
  }

  @Nullable
  public VirtualFile getDefaultsCssFile() {
    return element.findChild(DEFAULTS_CSS);
  }

  @NotNull
  public VirtualFile getCatalogFile() {
    VirtualFile child = element.findChild(CATALOG);
    assert child != null;
    return child;
  }

  @NotNull
  public VirtualFile getSwfFile() {
    VirtualFile child = element.findChild(SWF);
    assert child != null;
    return child;
  }

  @NotNull
  public String getPath() {
    return path;
  }

  public VirtualFile getFile() {
    return element;
  }

  @Override
  public String toString() {
    return getFile().getNameWithoutExtension();
  }

  public boolean isFromSdk() {
    return fromSdk;
  }
}