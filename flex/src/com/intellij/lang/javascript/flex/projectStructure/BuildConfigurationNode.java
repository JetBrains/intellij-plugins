package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.projectStructure.ui.FlexIdeBCConfigurable;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import org.jetbrains.annotations.NotNull;

/**
 * @author ksafonov
 */
public class BuildConfigurationNode extends MasterDetailsComponent.MyNode {

  public BuildConfigurationNode(NamedConfigurable configurable) {
    super(configurable);
  }

  @NotNull
  @Override
  public String getDisplayName() {
    final FlexIdeBCConfigurable configurable = FlexIdeBCConfigurable.unwrapIfNeeded((NamedConfigurable)getUserObject());
    return configurable.getTreeNodeText();
  }
}
