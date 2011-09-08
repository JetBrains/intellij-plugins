package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableModuleLibraryEntry;
import org.jetbrains.annotations.NotNull;

/**
 * @author ksafonov
 */
class ModuleLibraryEntryImpl implements ModifiableModuleLibraryEntry {

  private final DependencyTypeImpl myDependencyType = new DependencyTypeImpl();

  @NotNull
  private final String myLibraryId;

  public ModuleLibraryEntryImpl(@NotNull String libraryId) {
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

  public EntryState getState() {
    EntryState state = new EntryState();
    state.LIBRARY_ID = myLibraryId;
    state.DEPENDENCY_TYPE = myDependencyType.getState();
    return state;
  }
}
