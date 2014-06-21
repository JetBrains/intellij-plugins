package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.ProjectBuilder;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.icons.PhoneGapIcons;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * PhoneGapModuleType.java
 *
 * Created by Masahiro Suzuka on 2014/04/13.
 */
public class PhoneGapModuleType extends ModuleType<PhoneGapModuleBuilder> {

  public static final String MODULE_ID = "PHONEGAP_MODULE";

  public PhoneGapModuleType() {
    super(MODULE_ID);
  }

  public static PhoneGapModuleType getInstance() {
    return (PhoneGapModuleType)ModuleTypeManager.getInstance().findByID(MODULE_ID);
  }

  @NotNull
  @Override
  public PhoneGapModuleBuilder createModuleBuilder() {
    return new PhoneGapModuleBuilder();
  }

  @NotNull
  @Override
  public String getName() {
    return "PhoneGap (dev)";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "PhoneGap Application";
  }

  @Override
  public Icon getBigIcon() {
    return PhoneGapIcons.get24pxIcon();
  }

  @Override
  public Icon getNodeIcon(@Deprecated boolean b) {
    return PhoneGapIcons.get16pxIcon();
  }
}
