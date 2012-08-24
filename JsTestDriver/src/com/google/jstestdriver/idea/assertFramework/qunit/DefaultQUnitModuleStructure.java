package com.google.jstestdriver.idea.assertFramework.qunit;

import org.jetbrains.annotations.NotNull;

public class DefaultQUnitModuleStructure extends AbstractQUnitModuleStructure {

  public static final String NAME = "Default Module";

  public DefaultQUnitModuleStructure(@NotNull QUnitFileStructure fileStructure) {
    super(fileStructure, NAME);
  }

}
