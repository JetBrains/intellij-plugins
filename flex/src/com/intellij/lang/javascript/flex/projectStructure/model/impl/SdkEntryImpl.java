// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.SdkEntry;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author ksafonov
 */
final class SdkEntryImpl implements SdkEntry {

  @NotNull
  private final String myName;

  private final Map<String, DependencyTypeImpl> myDependencyTypes = new LinkedHashMap<>();

  SdkEntryImpl(State state) {
    myName = StringUtil.notNullize(state.NAME);
    for (EntryState entryState : state.DEPENDENCY_TYPES) {
      DependencyTypeImpl dependencyType = new DependencyTypeImpl();
      dependencyType.loadState(entryState.TYPE);
      myDependencyTypes.put(entryState.URL, dependencyType);
    }
  }

  SdkEntryImpl(@NotNull String name) {
    myName = name;
  }

  public SdkEntryImpl getCopy() {
    SdkEntryImpl copy = new SdkEntryImpl(myName);
    applyTo(copy);
    return copy;
  }

  private void applyTo(SdkEntryImpl copy) {
    copy.myDependencyTypes.clear();
    copy.myDependencyTypes.putAll(myDependencyTypes);
  }

  public boolean isEqual(@NotNull SdkEntryImpl that) {
    if (!myName.equals(that.myName)) return false;
    Iterator<String> i1 = myDependencyTypes.keySet().iterator();
    Iterator<String> i2 = that.myDependencyTypes.keySet().iterator();
    while (i1.hasNext() && i2.hasNext()) {
      String url1 = i1.next();
      String url2 = i2.next();
      if (!url1.equals(url2)) return true;
      if (!Comparing.equal(myDependencyTypes.get(url1), myDependencyTypes.get(url2))) return true;
    }
    if (i1.hasNext() || i2.hasNext()) return false;
    return true;
  }

  @Override
  @NotNull
  public String getName() {
    return myName;
  }

  public State getState() {
    State state = new State();
    state.NAME = myName;
    for (Map.Entry<String, DependencyTypeImpl> entry : myDependencyTypes.entrySet()) {
      EntryState entryState = new EntryState();
      entryState.URL = entry.getKey();
      entryState.TYPE = entry.getValue().getState();
      state.DEPENDENCY_TYPES.add(entryState);
    }
    return state;
  }

  @Tag("sdk")
  public static class State {

    @Attribute("name")
    public String NAME;

    @Tag("dependencies")
    public List<EntryState> DEPENDENCY_TYPES = new ArrayList<>();
  }

  @Tag("entry")
  public static class EntryState {
    @Attribute("url")
    public String URL;

    @Property(surroundWithTag = false)
    public DependencyTypeImpl.State TYPE;
  }
}
