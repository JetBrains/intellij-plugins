package com.github.masahirosuzuka.PhoneGapIntelliJPlugin;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Masahiro Suzuka on 2014/04/13.
 */
public class PhoneGapModuleBuilder extends ModuleBuilder implements ModuleBuilderListener {

  public PhoneGapModuleBuilder() {
    addListener(this);
  }

  @Override
  public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {

  }

  @Override
  public void moduleCreated(@NotNull Module module) {

  }

  @Override
  public ModuleType getModuleType() {
    return PhoneGapModuleType.getInstance();
  }
}
