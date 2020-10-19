package org.jetbrains.idea.perforce.application;

import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PerforceNumberNameMap {
  private final BidirectionalMap<Long, String> myNumberToName = new BidirectionalMap<>();

  @Nullable
  String getName(@NotNull Long number) {
    return myNumberToName.get(number);
  }

  @NotNull
  List<String> getAllNames() {
    return new ArrayList<>(myNumberToName.values());
  }

  void put(@NotNull String name, @NotNull Long nativeNumber) {
    myNumberToName.put(nativeNumber, name);
  }

  @Nullable
  Long getNumber(@NotNull String name) {
    return ContainerUtil.getFirstItem(myNumberToName.getKeysByValue(name));
  }

  @Nullable String removeList(long nativeNumber) {
    return myNumberToName.remove(nativeNumber);
  }

  void rename(@NotNull String from, @NotNull String to) {
    Long associated = getNumber(from);
    if (associated != null) {
      myNumberToName.put(associated, to);
    }
  }


  /** @return the set of change lists not present anymore in p4, that have to be removed from IDEA */
  Set<String> updateMapping(PerforceNumberNameMap newMap) {
    List<String> removed = ContainerUtil.filter(getAllNames(), name -> newMap.getNumber(name) == null);
    myNumberToName.clear();
    myNumberToName.putAll(newMap.myNumberToName);
    return new HashSet<>(removed);
  }

  @MapAnnotation(surroundWithTag = false, surroundKeyWithTag = false, surroundValueWithTag = false)
  public Map<Long, String> getMapping() {
    return new HashMap<>(myNumberToName);
  }

  @MapAnnotation(surroundWithTag = false, surroundKeyWithTag = false, surroundValueWithTag = false)
  public void setMapping(@NotNull Map<Long, String> map) {
    myNumberToName.clear();
    myNumberToName.putAll(map);
  }
}
