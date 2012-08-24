package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsAirPackageEntry;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementCreator;
import org.jetbrains.jps.model.impl.JpsElementBase;
import org.jetbrains.jps.model.impl.JpsElementChildRoleBase;
import org.jetbrains.jps.model.impl.JpsElementCollectionRole;

public class JpsAirPackageEntryImpl extends JpsElementBase<JpsAirPackageEntryImpl> implements JpsAirPackageEntry {

  static JpsAirPackageEntryRole ROLE = new JpsAirPackageEntryRole();
  static JpsElementCollectionRole<JpsAirPackageEntry> COLLECTION_ROLE = JpsElementCollectionRole.create(ROLE);

  private @NotNull String myFilePath = "";
  private @NotNull String myPathInPackage = "";

  JpsAirPackageEntryImpl() {
  }

  private JpsAirPackageEntryImpl(final JpsAirPackageEntryImpl original) {
    myFilePath = original.myFilePath;
    myPathInPackage = original.myPathInPackage;
  }

  @NotNull
  public JpsAirPackageEntryImpl createCopy() {
    return new JpsAirPackageEntryImpl(this);
  }

  public void applyChanges(@NotNull final JpsAirPackageEntryImpl modified) {
    myFilePath = modified.myFilePath;
    myPathInPackage = modified.myPathInPackage;
  }

// ---------------------------------------------

  @NotNull
  public String getFilePath() {
    return myFilePath;
  }

  @NotNull
  public String getPathInPackage() {
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

  private static class JpsAirPackageEntryRole extends JpsElementChildRoleBase<JpsAirPackageEntry>
    implements JpsElementCreator<JpsAirPackageEntry> {

    private JpsAirPackageEntryRole() {
      super("air package entry");
    }

    @NotNull
    public JpsAirPackageEntry create() {
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
