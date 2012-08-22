package com.intellij.jps.flex.model.bc.impl;

import com.intellij.jps.flex.model.bc.JpsAirDesktopPackagingOptions;
import com.intellij.jps.flex.model.bc.JpsAirPackageEntry;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementCreator;
import org.jetbrains.jps.model.impl.JpsElementChildRoleBase;
import org.jetbrains.jps.model.impl.JpsElementCollectionImpl;

import java.util.ArrayList;
import java.util.List;

class JpsAirDesktopPackagingOptionsImpl extends JpsAirPackagingOptionsBase<JpsAirDesktopPackagingOptionsImpl>
  implements JpsAirDesktopPackagingOptions {

  static final JpsAirDesktopPackagingOptionsRole ROLE = new JpsAirDesktopPackagingOptionsRole();

  private JpsAirDesktopPackagingOptionsImpl() {
  }

  private JpsAirDesktopPackagingOptionsImpl(final JpsAirDesktopPackagingOptionsImpl original) {
    super(original);
  }

  @NotNull
  public JpsAirDesktopPackagingOptionsImpl createCopy() {
    return new JpsAirDesktopPackagingOptionsImpl(this);
  }

// ------------------------------------

  State getState() {
    final State state = new State();
    state.USE_GENERATED_DESCRIPTOR = isUseGeneratedDescriptor();
    state.CUSTOM_DESCRIPTOR_PATH = getCustomDescriptorPath();
    state.PACKAGE_FILE_NAME = getPackageFileName();

    for (JpsAirPackageEntry entry : getFilesToPackage()) {
      state.FILES_TO_PACKAGE.add(((JpsAirPackageEntryImpl)entry).getState());
    }

    state.SIGNING_OPTIONS = ((JpsAirSigningOptionsImpl)getSigningOptions()).getState();

    return state;
  }

  void loadState(@NotNull State state) {
    setUseGeneratedDescriptor(state.USE_GENERATED_DESCRIPTOR);
    setCustomDescriptorPath(state.CUSTOM_DESCRIPTOR_PATH);
    setPackageFileName(state.PACKAGE_FILE_NAME);

    final JpsElementCollectionImpl<JpsAirPackageEntry> packageEntries = myContainer.getChild(JpsAirPackageEntryImpl.COLLECTION_ROLE);
    assert packageEntries.getElements().isEmpty();
    for (JpsAirPackageEntryImpl.State f : state.FILES_TO_PACKAGE) {
      final JpsAirPackageEntry entry = ((JpsElementCreator<JpsAirPackageEntry>)JpsAirPackageEntryImpl.ROLE).create();
      ((JpsAirPackageEntryImpl)entry).loadState(f);
      packageEntries.addChild(entry);
    }

    ((JpsAirSigningOptionsImpl)getSigningOptions()).loadState(state.SIGNING_OPTIONS);
  }

  private static class JpsAirDesktopPackagingOptionsRole extends JpsElementChildRoleBase<JpsAirDesktopPackagingOptions>
    implements JpsElementCreator<JpsAirDesktopPackagingOptions> {

    public JpsAirDesktopPackagingOptionsRole() {
      super("air desktop packaging options");
    }

    @NotNull
    public JpsAirDesktopPackagingOptions create() {
      return new JpsAirDesktopPackagingOptionsImpl();
    }
  }

  @Tag("packaging-air-desktop")
  public static class State {
    @Attribute("use-generated-descriptor")
    public boolean USE_GENERATED_DESCRIPTOR = true;
    @Attribute("custom-descriptor-path")
    public String CUSTOM_DESCRIPTOR_PATH = "";
    @Attribute("package-file-name")
    public String PACKAGE_FILE_NAME = "";
    @Tag("files-to-package")
    @AbstractCollection(surroundWithTag = false)
    public List<JpsAirPackageEntryImpl.State> FILES_TO_PACKAGE = new ArrayList<JpsAirPackageEntryImpl.State>();
    @Property(surroundWithTag = false)
    public JpsAirSigningOptionsImpl.State SIGNING_OPTIONS = new JpsAirSigningOptionsImpl.State();
  }
}
