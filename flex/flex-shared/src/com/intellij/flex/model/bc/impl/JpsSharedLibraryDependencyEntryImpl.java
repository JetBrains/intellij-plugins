// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsLibraryDependencyEntry;
import com.intellij.flex.model.bc.LinkageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.library.JpsLibrary;
import org.jetbrains.jps.model.library.JpsLibraryReference;
import org.jetbrains.jps.model.serialization.library.JpsLibraryTableSerializer;

class JpsSharedLibraryDependencyEntryImpl extends JpsFlexDependencyEntryBase<JpsSharedLibraryDependencyEntryImpl>
  implements JpsLibraryDependencyEntry {

  private static final JpsElementChildRoleBase<JpsLibraryReference> LIB_REF_ROLE =
    JpsElementChildRoleBase.create("shared library reference");

  JpsSharedLibraryDependencyEntryImpl(final String libraryName, final String libraryLevel, final LinkageType linkageType) {
    super(linkageType);

    final JpsLibraryReference libraryRef = JpsElementFactory.getInstance()
      .createLibraryReference(libraryName, JpsLibraryTableSerializer.createLibraryTableReference(libraryLevel));
    myContainer.setChild(LIB_REF_ROLE, libraryRef);
  }

  private JpsSharedLibraryDependencyEntryImpl(final JpsSharedLibraryDependencyEntryImpl original) {
    super(original);
  }

  @Override
  public @NotNull JpsSharedLibraryDependencyEntryImpl createCopy() {
    return new JpsSharedLibraryDependencyEntryImpl(this);
  }

// ------------------------------------

  @Override
  public @Nullable JpsLibrary getLibrary() {
    return myContainer.getChild(LIB_REF_ROLE).resolve();
  }
}
