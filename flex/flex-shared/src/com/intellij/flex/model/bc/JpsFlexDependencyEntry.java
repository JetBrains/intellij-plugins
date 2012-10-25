package com.intellij.flex.model.bc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElement;

public interface JpsFlexDependencyEntry extends JpsElement {

  @NotNull
  LinkageType getLinkageType();
}
