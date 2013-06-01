package com.jetbrains.lang.dart.ide.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NonNls;

/**
 * @author yole
 */
public abstract class DartModuleTypeBase<T extends ModuleBuilder> extends ModuleType<T> {
  protected DartModuleTypeBase(@NonNls String id) {
    super(id);
  }
}
