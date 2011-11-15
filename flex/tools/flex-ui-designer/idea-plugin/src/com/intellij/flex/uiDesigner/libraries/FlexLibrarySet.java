package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.AssetCounterInfo;
import com.intellij.flex.uiDesigner.abc.AbcTranscoder;
import com.intellij.openapi.util.Condition;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class FlexLibrarySet extends LibrarySet {
  public final AssetCounterInfo assetCounterInfo = new AssetCounterInfo();
  final ContainsCondition contains;

  public FlexLibrarySet(int id, @Nullable LibrarySet parent,
                        List<Library> items, List<Library> resourceBundleOnlyItems, ContainsCondition contains) {
    super(id, parent, items, resourceBundleOnlyItems);
    this.contains = contains;
  }

  static class ContainsCondition implements Condition<String> {
    private final Set<CharSequence> globalDefinitions;
    private final Set<CharSequence> ownDefinitions;

    public ContainsCondition(Set<CharSequence> globalDefinitions, THashMap<CharSequence, Definition> definitionMap) {
      this.globalDefinitions = globalDefinitions;
      ownDefinitions = new THashSet<CharSequence>(definitionMap.size(), AbcTranscoder.HASHING_STRATEGY);
      ownDefinitions.addAll(definitionMap.keySet());
    }

    @Override
    public boolean value(String name) {
      return globalDefinitions.contains(name) || ownDefinitions.contains(name);
    }
  }
}