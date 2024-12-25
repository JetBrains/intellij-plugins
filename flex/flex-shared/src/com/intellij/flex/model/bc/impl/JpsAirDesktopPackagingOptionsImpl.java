// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsAirDesktopPackagingOptions;
import com.intellij.flex.model.bc.JpsAirPackageEntry;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementCollection;
import org.jetbrains.jps.model.JpsElementCreator;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;

import java.util.ArrayList;
import java.util.List;

final class JpsAirDesktopPackagingOptionsImpl extends JpsAirPackagingOptionsBase<JpsAirDesktopPackagingOptionsImpl>
  implements JpsAirDesktopPackagingOptions {

  static final JpsAirDesktopPackagingOptionsRole ROLE = new JpsAirDesktopPackagingOptionsRole();

  private JpsAirDesktopPackagingOptionsImpl() {
  }

  private JpsAirDesktopPackagingOptionsImpl(final JpsAirDesktopPackagingOptionsImpl original) {
    super(original);
  }

  @Override
  public @NotNull JpsAirDesktopPackagingOptionsImpl createCopy() {
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

    final JpsElementCollection<JpsAirPackageEntry> packageEntries = myContainer.getChild(JpsAirPackageEntryImpl.COLLECTION_ROLE);
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

    JpsAirDesktopPackagingOptionsRole() {
      super("air desktop packaging options");
    }

    @Override
    public @NotNull JpsAirDesktopPackagingOptions create() {
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
    @XCollection(propertyElementName = "files-to-package")
    public List<JpsAirPackageEntryImpl.State> FILES_TO_PACKAGE = new ArrayList<>();
    @Property(surroundWithTag = false)
    public JpsAirSigningOptionsImpl.State SIGNING_OPTIONS = new JpsAirSigningOptionsImpl.State();
  }
}
