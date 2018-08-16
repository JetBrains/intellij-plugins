package com.intellij.lang.javascript.flex;

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
public class FlexModuleType extends ModuleType<FlexModuleBuilder> {
  @NonNls public static final String MODULE_TYPE_ID = "Flex";

  public FlexModuleType() {
    super(MODULE_TYPE_ID);
  }

  @Override
  @NotNull
  public FlexModuleBuilder createModuleBuilder() {
    return new FlexModuleBuilder();
  }

  @Override
  @NotNull
  public String getName() {
    return FlexBundle.message("flash.module.type.name");
  }

  @Override
  @NotNull
  public String getDescription() {
    return FlexBundle.message("flash.module.type.description");
  }

  @Override
  public Icon getNodeIcon(final boolean isOpened) {
    return FlexIcons.Flex.Flash_module_closed;
  }

  public static FlexModuleType getInstance() {
    return (FlexModuleType)ModuleTypeManager.getInstance().findByID(MODULE_TYPE_ID);
  }

  @Override
  public boolean isSupportedRootType(JpsModuleSourceRootType type) {
    return type == JavaSourceRootType.SOURCE || type == JavaSourceRootType.TEST_SOURCE;
  }
}
