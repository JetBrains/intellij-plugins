package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.AssetCounter;
import com.intellij.flex.uiDesigner.AssetCounterInfo;
import com.intellij.flex.uiDesigner.abc.AbcTranscoder;
import com.intellij.openapi.util.Condition;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FlexLibrarySet extends LibrarySet {
  public final AssetCounterInfo assetCounterInfo;
  final ContainsCondition contains;

  private final String version;

  FlexLibrarySet(LibrarySorter.SortResult sortResult,
                 @Nullable LibrarySet parent,
                 ContainsCondition contains,
                 AssetCounter demanded,
                 String version) {
    super(sortResult.id, parent, sortResult.libraries);
    this.contains = contains;
    this.version = version;
    assetCounterInfo = new AssetCounterInfo(demanded);
  }

  public String getVersion() {
    return version;
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