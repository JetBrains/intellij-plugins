// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableDependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableSharedLibraryEntry;
import org.jetbrains.annotations.NotNull;

class SharedLibraryEntryImpl implements ModifiableSharedLibraryEntry, StatefulDependencyEntry {

  private final DependencyTypeImpl myDependencyType = new DependencyTypeImpl();

  private final @NotNull String myLibraryName;

  private final @NotNull String myLibraryLevel;


  SharedLibraryEntryImpl(@NotNull String libraryName, @NotNull String libraryLevel) {
    myLibraryName = libraryName;
    myLibraryLevel = libraryLevel;
  }

  @Override
  public @NotNull String getLibraryName() {
    return myLibraryName;
  }

  @Override
  public @NotNull String getLibraryLevel() {
    return myLibraryLevel;
  }

  @Override
  public @NotNull DependencyTypeImpl getDependencyType() {
    return myDependencyType;
  }

  public SharedLibraryEntryImpl getCopy() {
    SharedLibraryEntryImpl copy = new SharedLibraryEntryImpl(myLibraryName, myLibraryLevel);
    applyTo(copy);
    return copy;
  }

  private void applyTo(ModifiableSharedLibraryEntry copy) {
    myDependencyType.applyTo(copy.getDependencyType());
  }

  @Override
  public boolean isEqual(ModifiableDependencyEntry other) {
    if (!(other instanceof SharedLibraryEntryImpl)) return false;
    if (!myLibraryName.equals(((SharedLibraryEntryImpl)other).myLibraryName)) return false;
    if (!myLibraryLevel.equals(((SharedLibraryEntryImpl)other).myLibraryLevel)) return false;
    if (!myDependencyType.isEqual(((SharedLibraryEntryImpl)other).myDependencyType)) return false;
    return true;
  }

  @Override
  public EntryState getState() {
    EntryState state = new EntryState();
    state.LIBRARY_NAME = myLibraryName;
    state.LIBRARY_LEVEL = myLibraryLevel;
    state.DEPENDENCY_TYPE = myDependencyType.getState();
    return state;
  }
}
