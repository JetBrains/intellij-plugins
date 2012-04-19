package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.AssetCounter;
import org.jetbrains.io.Info;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Library extends Info<VirtualFile> {
  public static final String DEFAULTS_CSS = "defaults.css";
  static final String CATALOG = "catalog.xml";
  static final String SWF = "library.swf";

  public byte[] inheritingStyles;
  @Nullable
  public byte[] defaultsStyle;
  @Nullable
  public AssetCounter assetCounter;

  // en_US => {"layout", "components"}
  public final Map<String, THashSet<String>> resourceBundles = new THashMap<String, THashSet<String>>();

  Library(VirtualFile file) {
    super(file);
  }

  public boolean hasResourceBundles() {
    return !resourceBundles.isEmpty();
  }

  public boolean isStyleOwner() {
    return inheritingStyles != null || defaultsStyle != null;
  }

  @Nullable
  public VirtualFile getDefaultsCssFile() {
    return element.findChild(DEFAULTS_CSS);
  }

  public VirtualFile getCatalogFile() {
    return getCatalogFile(element);
  }

  public VirtualFile getSwfFile() {
    return getSwfFile(element);
  }

  @Nullable
  public static VirtualFile getSwfFile(VirtualFile swcFile) {
    return swcFile.findChild(SWF);
  }

  public static VirtualFile getCatalogFile(VirtualFile swcFile) {
    return swcFile.findChild(CATALOG);
  }

  public VirtualFile getFile() {
    return element;
  }

  @Override
  public String toString() {
    return getFile().getNameWithoutExtension();
  }
}