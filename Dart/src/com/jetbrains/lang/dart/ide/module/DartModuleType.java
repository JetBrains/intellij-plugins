package com.jetbrains.lang.dart.ide.module;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.template.DartEmptyApplicationGenerator;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartModuleType extends ModuleType<ModuleBuilder> {

  public static final String MODULE_ID = "DART_MODULE";

  public static final String GROUP_NAME = DartBundle.message("dart.module.type.name");

  private static final DartEmptyApplicationGenerator DEFAULT_PROJECT_TYPE = new DartEmptyApplicationGenerator();

  public DartModuleType() { super(MODULE_ID); }

  @NotNull
  public static DartModuleType getInstance() {
    return (DartModuleType)ModuleTypeManager.getInstance().findByID(MODULE_ID);
  }

  @NotNull
  @Override
  public ModuleBuilder createModuleBuilder() {
    return new DartModuleBuilder(DEFAULT_PROJECT_TYPE);
  }

  @NotNull
  public <T> ModuleBuilder createModuleBuilder(@NotNull DartProjectTemplate<T> dartProjectTemplate) {
    return new DartModuleBuilder(dartProjectTemplate);
  }

  @Override
  public Icon getBigIcon() { return DartIcons.Dart_24; }

  @Override
  public Icon getNodeIcon(final boolean isOpened) { return AllIcons.Nodes.Module; }

  @NotNull
  @Override
  public String getName() { return DEFAULT_PROJECT_TYPE.getName(); }

  @NotNull
  @Override
  public String getDescription() { return DEFAULT_PROJECT_TYPE.getDescription(); }

}
