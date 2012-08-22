package com.intellij.jps.flex.model.bc.impl;

import com.intellij.jps.flex.model.bc.JpsFlexDependencyEntry;
import com.intellij.jps.flex.model.bc.JpsLinkageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.impl.JpsCompositeElementBase;

abstract class JpsFlexDependencyEntryBase<Self extends JpsFlexDependencyEntryBase<Self>> extends JpsCompositeElementBase<Self>
  implements JpsFlexDependencyEntry {

  private JpsLinkageType myLinkageType;

  protected JpsFlexDependencyEntryBase(final JpsLinkageType linkageType) {
    myLinkageType = linkageType;
  }

  protected JpsFlexDependencyEntryBase(final JpsFlexDependencyEntryBase<Self> original) {
    myLinkageType = original.myLinkageType;
  }

  public void applyChanges(@NotNull final Self modified) {
    super.applyChanges(modified);
    myLinkageType = modified.myLinkageType;
  }

// ------------------------------------

  @NotNull
  public JpsLinkageType getLinkageType() {
    return myLinkageType;
  }
}
