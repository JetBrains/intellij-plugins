// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsAirPackageEntry;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementCreator;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.ex.JpsElementCollectionRole;

public class JpsAirPackageEntryImpl extends JpsElementBase<JpsAirPackageEntryImpl> implements JpsAirPackageEntry {

  static final JpsAirPackageEntryRole ROLE = new JpsAirPackageEntryRole();
  static final JpsElementCollectionRole<JpsAirPackageEntry> COLLECTION_ROLE = JpsElementCollectionRole.create(ROLE);

  private @NotNull String myFilePath = "";
  private @NotNull String myPathInPackage = "";

  JpsAirPackageEntryImpl() {
  }

  private JpsAirPackageEntryImpl(final JpsAirPackageEntryImpl original) {
    myFilePath = original.myFilePath;
    myPathInPackage = original.myPathInPackage;
  }

  @Override
  public @NotNull JpsAirPackageEntryImpl createCopy() {
    return new JpsAirPackageEntryImpl(this);
  }

// ---------------------------------------------

  @Override
  public @NotNull String getFilePath() {
    return myFilePath;
  }

  @Override
  public @NotNull String getPathInPackage() {
    return myPathInPackage;
  }

// ---------------------------------------------

  State getState() {
    final State state = new State();
    state.FILE_PATH = myFilePath;
    state.PATH_IN_PACKAGE = myPathInPackage;
    return state;
  }

  void loadState(final State state) {
    myFilePath = state.FILE_PATH;
    myPathInPackage = state.PATH_IN_PACKAGE;
  }

  private static final class JpsAirPackageEntryRole extends JpsElementChildRoleBase<JpsAirPackageEntry>
    implements JpsElementCreator<JpsAirPackageEntry> {

    private JpsAirPackageEntryRole() {
      super("air package entry");
    }

    @Override
    public @NotNull JpsAirPackageEntry create() {
      return new JpsAirPackageEntryImpl();
    }
  }

  @Tag("FilePathAndPathInPackage")
  public static class State {
    @Attribute("file-path")
    public String FILE_PATH = "";

    @Attribute("path-in-package")
    public String PATH_IN_PACKAGE = "";
  }
}
