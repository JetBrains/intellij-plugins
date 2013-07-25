package com.intellij.coldFusion.model.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author vnikolaenko
 */
public interface CfmlComponent extends CfmlPsiElement, CfmlNamedElement {
  CfmlComponent[] EMPTY_ARRAY = new CfmlComponent[0];
  @NotNull
  CfmlFunction[] getFunctions();
  @NotNull
  CfmlFunction[] getFunctionsWithSupers(boolean isSuperPriority);
  @NotNull
  CfmlProperty[] getProperties();
  @NotNull
  CfmlProperty[] getPropertiesWithSupers(boolean isSuperPriority);
  boolean isInterface();
  @Nullable
  String getSuperName();
  String[] getInterfaceNames();
  CfmlComponent[] getImplementedInterfaces();
  @Nullable
  CfmlComponent getSuper();
  @Nullable
  CfmlComponentReference getSuperReference();

  boolean hasImplicitAccessors();
  boolean isPersistent();
}
