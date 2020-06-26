// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsAirPackageEntry;
import com.intellij.flex.model.bc.JpsAndroidPackagingOptions;
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

final class JpsAndroidPackagingOptionsImpl extends JpsAirPackagingOptionsBase<JpsAndroidPackagingOptionsImpl>
  implements JpsAndroidPackagingOptions {

  static final JpsAndroidPackagingOptionsRole ROLE = new JpsAndroidPackagingOptionsRole();

  private JpsAndroidPackagingOptionsImpl() {
  }

  private JpsAndroidPackagingOptionsImpl(final JpsAirPackagingOptionsBase<JpsAndroidPackagingOptionsImpl> original) {
    super(original);
  }

  @Override
  @NotNull
  public JpsAndroidPackagingOptionsImpl createCopy() {
    return new JpsAndroidPackagingOptionsImpl(this);
  }

  State getState() {
    final State state = new State();
    state.ENABLED = isEnabled();
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
    setEnabled(state.ENABLED);
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

  private static class JpsAndroidPackagingOptionsRole extends JpsElementChildRoleBase<JpsAndroidPackagingOptions>
    implements JpsElementCreator<JpsAndroidPackagingOptions> {

    JpsAndroidPackagingOptionsRole() {
      super("android packaging options");
    }

    @Override
    @NotNull
    public JpsAndroidPackagingOptions create() {
      return new JpsAndroidPackagingOptionsImpl();
    }
  }

  @Tag("packaging-android")
  public static class State {
    @Attribute("enabled")
    public boolean ENABLED = false;
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
