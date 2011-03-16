package com.intellij.flex.uiDesigner;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class OriginalLibrary implements Library {
  private static final String DEFAULTS_CSS = "defaults.css";
  private static final String CATALOG = "catalog.xml";
  private static final String SWF = "library.swf";
  
  public transient int id = -1;
  public transient int sessionId = -1;

  // path to SWC, not to our cache dir
  private VirtualFile file;

  public byte[] inheritingStyles;
  public byte[] defaultsStyle;

  private String path;
  private boolean fromSdk;

  @SuppressWarnings({"UnusedDeclaration"})
  public OriginalLibrary() {
  }

  public OriginalLibrary(String relativePath, VirtualFile file, boolean fromSdk) {
    this.path = relativePath;
    this.file = file;
    this.fromSdk = fromSdk;
  }

  public @Nullable VirtualFile getDefaultsCssFile() {
    return file.findChild(DEFAULTS_CSS);
  }
  
  public @NotNull VirtualFile getCatalogFile() {
    VirtualFile child = file.findChild(CATALOG);
    assert child != null;
    return child;
  }
  
  public @NotNull VirtualFile getSwfFile() {
    VirtualFile child = file.findChild(SWF);
    assert child != null;
    return child;
  }

  public @NotNull String getPath() {
    return path;
  }

  public VirtualFile getFile() {
    return file;
  }
  
  @Override
  public String toString() {
    return getFile().getNameWithoutExtension();
  }

  public boolean isFromSdk() {
    return fromSdk;
  }
}