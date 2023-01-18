package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableDependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableSharedLibraryEntry;
import org.jetbrains.annotations.NotNull;

class SharedLibraryEntryImpl implements ModifiableSharedLibraryEntry, StatefulDependencyEntry {

  private final DependencyTypeImpl myDependencyType = new DependencyTypeImpl();

  @NotNull
  private final String myLibraryName;

  @NotNull
  private final String myLibraryLevel;


  SharedLibraryEntryImpl(@NotNull String libraryName, @NotNull String libraryLevel) {
    myLibraryName = libraryName;
    myLibraryLevel = libraryLevel;
  }

  @Override
  @NotNull
  public String getLibraryName() {
    return myLibraryName;
  }

  @Override
  @NotNull
  public String getLibraryLevel() {
    return myLibraryLevel;
  }

  @NotNull
  @Override
  public DependencyTypeImpl getDependencyType() {
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
