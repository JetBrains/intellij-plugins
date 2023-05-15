package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexBCConfigurable;
import com.intellij.openapi.ui.MasterDetailsComponent;
import org.jetbrains.annotations.NotNull;

public class BuildConfigurationNode extends MasterDetailsComponent.MyNode {

  public BuildConfigurationNode(CompositeConfigurable configurable) {
    super(configurable);
  }

  @NotNull
  @Override
  public String getDisplayName() {
    final FlexBCConfigurable configurable = FlexBCConfigurable.unwrap((CompositeConfigurable)getUserObject());
    return configurable.getTreeNodeText();
  }
}
