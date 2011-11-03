package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.AssetCounterInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlexLibrarySet extends LibrarySet {
  public final AssetCounterInfo assetCounterInfo = new AssetCounterInfo();

  public FlexLibrarySet(int id, @Nullable LibrarySet parent, ApplicationDomainCreationPolicy applicationDomainCreationPolicy,
                        List<LibrarySetItem> items, List<LibrarySetItem> resourceBundleOnlyItems) {
    super(id, parent, applicationDomainCreationPolicy, items, resourceBundleOnlyItems);
  }
}
