package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsAirPackageEntry;
import com.intellij.flex.model.bc.JpsAirSigningOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.impl.JpsCompositeElementBase;

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

  public void applyChanges(final @NotNull Self modified) {
    super.applyChanges(modified);
    setEnabled(modified.isEnabled());
    setUseGeneratedDescriptor(modified.isUseGeneratedDescriptor());
    setCustomDescriptorPath(modified.getCustomDescriptorPath());
    setPackageFileName(modified.getPackageFileName());
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

  @NotNull
  public String getCustomDescriptorPath() {
    return myCustomDescriptorPath;
  }

  public void setCustomDescriptorPath(final @NotNull String customDescriptorPath) {
    myCustomDescriptorPath = customDescriptorPath;
  }

  @NotNull
  public String getPackageFileName() {
    return myPackageFileName;
  }

  public void setPackageFileName(final @NotNull String packageFileName) {
    myPackageFileName = packageFileName;
  }

  @NotNull
  public List<JpsAirPackageEntry> getFilesToPackage() {
    return myContainer.getChild(JpsAirPackageEntryImpl.COLLECTION_ROLE).getElements();
  }

  @NotNull
  public JpsAirSigningOptions getSigningOptions() {
    return myContainer.getChild(JpsAirSigningOptionsImpl.ROLE);
  }
}
