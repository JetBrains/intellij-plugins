// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexBCConfigurable;
import com.intellij.openapi.ui.MasterDetailsComponent;
import org.jetbrains.annotations.NotNull;

public class BuildConfigurationNode extends MasterDetailsComponent.MyNode {

  public BuildConfigurationNode(CompositeConfigurable configurable) {
    super(configurable);
  }

  @Override
  public @NotNull String getDisplayName() {
    final FlexBCConfigurable configurable = FlexBCConfigurable.unwrap((CompositeConfigurable)getUserObject());
    return configurable.getTreeNodeText();
  }
}
