// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsFlexBCDependencyEntry;
import com.intellij.flex.model.bc.JpsFlexBCReference;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.LinkageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementFactory;
import org.jetbrains.jps.model.module.JpsModuleReference;

class JpsFlexBCDependencyEntryImpl extends JpsFlexDependencyEntryBase<JpsFlexBCDependencyEntryImpl> implements JpsFlexBCDependencyEntry {

  JpsFlexBCDependencyEntryImpl(final String moduleName, final String bcName, final LinkageType linkageType) {
    super(linkageType);

    final JpsModuleReference moduleRef = JpsElementFactory.getInstance().createModuleReference(moduleName);
    final JpsFlexBCReferenceImpl bcRef = new JpsFlexBCReferenceImpl(bcName, moduleRef);
    myContainer.setChild(JpsFlexBCReferenceImpl.ROLE, bcRef);
  }

  private JpsFlexBCDependencyEntryImpl(final JpsFlexBCDependencyEntryImpl original) {
    super(original);
  }

  @Override
  public @NotNull JpsFlexBCDependencyEntryImpl createCopy() {
    return new JpsFlexBCDependencyEntryImpl(this);
  }

// ------------------------------------

  @Override
  public @Nullable JpsFlexBuildConfiguration getBC() {
    final JpsFlexBCReference bcRef = myContainer.getChild(JpsFlexBCReferenceImpl.ROLE);
    return bcRef == null ? null : bcRef.resolve();
  }
}
