package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * Created by Masahiro Suzuka on 2014/04/11.
 */
public class PhoneGapModuleBuilder extends ModuleBuilder {
  @Override
  public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {

  }

  @Override
  public ModuleType getModuleType() {
    return null;
  }
}
