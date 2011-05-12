package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.RequiredAssetsInfo;
import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class SwfLibrary extends InfoList.Info<VirtualFile> implements Library {
  public static final String DEFAULTS_CSS = "defaults.css";
  private static final String CATALOG = "catalog.xml";
  private static final String SWF = "library.swf";

  public byte[] inheritingStyles;
  public byte[] defaultsStyle;
  public RequiredAssetsInfo requiredAssetsInfo;

  private final String path;

  private boolean hasDefinitions = true;

  public Collection<Library> parents;

  // en_US => {"layout", "components"}
  public final Map<String,THashSet<String>> resourceBundles = new THashMap<String,THashSet<String>>();

  public SwfLibrary(String relativePath, VirtualFile file) {
    super(file);

    this.path = relativePath;
  }

  public void setHasDefinitions(boolean hasDefinitions) {
    this.hasDefinitions = hasDefinitions;
  }

  public boolean hasDefinitions() {
    return hasDefinitions;
  }

  @Override
  public Collection<Library> getParents() {
    return parents;
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
}