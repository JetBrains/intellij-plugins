package com.intellij.jps.flex.model.bc.impl;

import com.intellij.jps.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.jps.flex.model.bc.JpsLibraryDependencyEntry;
import com.intellij.jps.flex.model.bc.JpsLinkageType;
import com.intellij.jps.flex.model.lib.JpsFlexLibraryProperties;
import com.intellij.jps.flex.model.lib.JpsFlexLibraryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.library.JpsLibrary;
import org.jetbrains.jps.model.library.JpsTypedLibrary;
import org.jetbrains.jps.model.module.JpsModule;

class JpsModuleLibraryDependencyEntryImpl extends JpsFlexDependencyEntryBase<JpsModuleLibraryDependencyEntryImpl>
  implements JpsLibraryDependencyEntry {

  private String myLibraryId;

  JpsModuleLibraryDependencyEntryImpl(final String libraryId, final JpsLinkageType linkageType) {
    super(linkageType);
    myLibraryId = libraryId;
  }

  private JpsModuleLibraryDependencyEntryImpl(final JpsModuleLibraryDependencyEntryImpl original) {
    super(original);
    myLibraryId = original.myLibraryId;
  }

  @NotNull
  public JpsModuleLibraryDependencyEntryImpl createCopy() {
    return new JpsModuleLibraryDependencyEntryImpl(this);
  }

  public void applyChanges(@NotNull final JpsModuleLibraryDependencyEntryImpl modified) {
    super.applyChanges(modified);
    myLibraryId = modified.myLibraryId;
  }

// ------------------------------------

  @Nullable
  public JpsLibrary getLibrary() {
    final JpsModule module = ((JpsFlexBuildConfiguration)myParent.getParent().getParent()).getModule();

    for (JpsLibrary library : module.getLibraryCollection().getLibraries()) {
      if (library.getType() == JpsFlexLibraryType.INSTANCE &&
          myLibraryId
            .equals(((JpsTypedLibrary<JpsSimpleElement<JpsFlexLibraryProperties>>)library).getProperties().getData().getLibraryId())) {
        return library;
      }
    }
    return null;
  }
}
