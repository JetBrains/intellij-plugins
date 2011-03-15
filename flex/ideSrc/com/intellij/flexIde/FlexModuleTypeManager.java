package com.intellij.flexIde;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.impl.ModuleTypeManagerImpl;

/**
 * @author ksafonov
 */
public class FlexModuleTypeManager extends ModuleTypeManagerImpl {
  @Override
  public ModuleType getDefaultModuleType() {
    return new FlexModuleType();
  }
}
