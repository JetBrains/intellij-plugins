package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableDependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableModuleLibraryEntry;
import org.jetbrains.annotations.NotNull;

class ModuleLibraryEntryImpl implements ModifiableModuleLibraryEntry, StatefulDependencyEntry {

  private final DependencyTypeImpl myDependencyType = new DependencyTypeImpl();

  @NotNull
  private final String myLibraryId;

  ModuleLibraryEntryImpl(@NotNull String libraryId) {
    myLibraryId = libraryId;
  }

  @Override
  @NotNull
  public String getLibraryId() {
    return myLibraryId;
  }

  @NotNull
  @Override
  public DependencyTypeImpl getDependencyType() {
    return myDependencyType;
  }

  public ModuleLibraryEntryImpl getCopy() {
    ModuleLibraryEntryImpl copy = new ModuleLibraryEntryImpl(myLibraryId);
    applyTo(copy);
    return copy;
  }

  private void applyTo(ModifiableModuleLibraryEntry copy) {
    myDependencyType.applyTo(copy.getDependencyType());
  }

  @Override
  public boolean isEqual(ModifiableDependencyEntry other) {
    if (!(other instanceof ModuleLibraryEntryImpl)) return false;
    if (!myLibraryId.equals(((ModuleLibraryEntryImpl)other).myLibraryId)) return false;
    if (!myDependencyType.isEqual(((ModuleLibraryEntryImpl)other).myDependencyType)) return false;
    return true;
  }

  @Override
  public EntryState getState() {
    EntryState state = new EntryState();
    state.LIBRARY_ID = myLibraryId;
    state.DEPENDENCY_TYPE = myDependencyType.getState();
    return state;
  }
}
