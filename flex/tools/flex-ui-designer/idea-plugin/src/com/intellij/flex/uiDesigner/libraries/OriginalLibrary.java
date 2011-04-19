package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.io.InfoList;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TLinkable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class OriginalLibrary extends InfoList.Info<VirtualFile> implements Library, TLinkable {
  public static final String DEFAULTS_CSS = "defaults.css";
  private static final String CATALOG = "catalog.xml";
  private static final String SWF = "library.swf";

  private TLinkable previous;
  private TLinkable next;

  public byte[] inheritingStyles;
  public byte[] defaultsStyle;

  private final String path;
  private final boolean fromSdk;

  public boolean filtered;

  public int inDegree;
  public int definitionCounter;

  public CharSequence mxCoreFlexModuleFactoryClassName;

  public final Set<CharSequence> unresolvedDefinitions = new THashSet<CharSequence>();
  public final Set<OriginalLibrary> successors = new THashSet<OriginalLibrary>();
  public final Set<OriginalLibrary> parents = new THashSet<OriginalLibrary>();

  // en_US => {"layout", "components"}
  public final Map<String,THashSet<String>> resourceBundles = new THashMap<String,THashSet<String>>();

  public OriginalLibrary(String relativePath, VirtualFile file, boolean fromSdk) {
    super(file);

    this.path = relativePath;
    this.fromSdk = fromSdk;
  }

  public boolean hasUnresolvedDefinitions() {
    return !unresolvedDefinitions.isEmpty();
  }

  public boolean hasDefinitions() {
    return definitionCounter > 0;
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

  public boolean isFromSdk() {
    return fromSdk;
  }

  @Override
  public TLinkable getNext() {
    return next;
  }

  @Override
  public TLinkable getPrevious() {
    return previous;
  }

  @Override
  public void setNext(TLinkable linkable) {
    next = linkable;
  }

  @Override
  public void setPrevious(TLinkable linkable) {
    previous = linkable;
  }
}