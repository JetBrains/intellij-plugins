// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsAirPackageEntry;
import com.intellij.flex.model.bc.JpsAirSigningOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;

import java.util.List;

abstract class JpsAirPackagingOptionsBase<Self extends JpsAirPackagingOptionsBase<Self>> extends JpsCompositeElementBase<Self> {

  private boolean myEnabled = false;
  private boolean myUseGeneratedDescriptor = true;
  private @NotNull String myCustomDescriptorPath = "";
  private @NotNull String myPackageFileName = "";

  protected JpsAirPackagingOptionsBase() {
    myContainer.setChild(JpsAirPackageEntryImpl.COLLECTION_ROLE);
    myContainer.setChild(JpsAirSigningOptionsImpl.ROLE);
  }

  protected JpsAirPackagingOptionsBase(final JpsAirPackagingOptionsBase<Self> original) {
    super(original);
    myEnabled = original.myEnabled;
    myUseGeneratedDescriptor = original.myUseGeneratedDescriptor;
    myCustomDescriptorPath = original.myCustomDescriptorPath;
    myPackageFileName = original.myPackageFileName;
  }

// ------------------------------------

  public boolean isEnabled() {
    return myEnabled;
  }

  public void setEnabled(final boolean enabled) {
    myEnabled = enabled;
  }

  public boolean isUseGeneratedDescriptor() {
    return myUseGeneratedDescriptor;
  }

  public void setUseGeneratedDescriptor(final boolean useGeneratedDescriptor) {
    myUseGeneratedDescriptor = useGeneratedDescriptor;
  }

  public @NotNull String getCustomDescriptorPath() {
    return myCustomDescriptorPath;
  }

  public void setCustomDescriptorPath(final @NotNull String customDescriptorPath) {
    myCustomDescriptorPath = customDescriptorPath;
  }

  public @NotNull String getPackageFileName() {
    return myPackageFileName;
  }

  public void setPackageFileName(final @NotNull String packageFileName) {
    myPackageFileName = packageFileName;
  }

  public @NotNull List<JpsAirPackageEntry> getFilesToPackage() {
    return myContainer.getChild(JpsAirPackageEntryImpl.COLLECTION_ROLE).getElements();
  }

  public @NotNull JpsAirSigningOptions getSigningOptions() {
    return myContainer.getChild(JpsAirSigningOptionsImpl.ROLE);
  }
}
