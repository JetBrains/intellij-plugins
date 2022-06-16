// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex;

import com.intellij.ide.projectWizard.NewProjectWizardConstants.Generators;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import icons.FlexIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;

/**
 * @author Maxim.Mossienko
 */
public final class FlexModuleType extends ModuleType<FlexModuleBuilder> {
  @NonNls public static final String MODULE_TYPE_ID = Generators.FLASH;

  public FlexModuleType() {
    super(MODULE_TYPE_ID);
  }

  @Override
  public @NotNull FlexModuleBuilder createModuleBuilder() {
    return new FlexModuleBuilder();
  }

  @Override
  public @NotNull String getName() {
    return FlexBundle.message("flash.module.type.name");
  }

  @Override
  public @NotNull String getDescription() {
    return FlexBundle.message("flash.module.type.description");
  }

  @Override
  public @NotNull Icon getNodeIcon(final boolean isOpened) {
    return FlexIcons.Flex.Flash_module_closed;
  }

  public static FlexModuleType getInstance() {
    return (FlexModuleType)ModuleTypeManager.getInstance().findByID(MODULE_TYPE_ID);
  }

  @Override
  public boolean isSupportedRootType(JpsModuleSourceRootType<?> type) {
    return type == JavaSourceRootType.SOURCE || type == JavaSourceRootType.TEST_SOURCE;
  }
}
