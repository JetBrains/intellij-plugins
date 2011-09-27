package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.AssetCounter;
import com.intellij.flex.uiDesigner.io.Info;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Library extends Info<VirtualFile> {
  public static final String DEFAULTS_CSS = "defaults.css";
  private static final String CATALOG = "catalog.xml";
  public static final String SWF = "library.swf";

  public byte[] inheritingStyles;
  public byte[] defaultsStyle;
  public AssetCounter assetCounter;

  private final String path;

  // en_US => {"layout", "components"}
  public final Map<String,THashSet<String>> resourceBundles = new THashMap<String,THashSet<String>>();

  public Library(String relativePath, VirtualFile file) {
    super(file);

    path = relativePath;
  }

  public boolean hasResourceBundles() {
    return !resourceBundles.isEmpty();
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
    return getSwfFile(element);
  }

  @NotNull
  public static VirtualFile getSwfFile(VirtualFile swcFile) {
    VirtualFile child = swcFile.findChild(SWF);
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
}