package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsFlexBCReference;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsCompositeElement;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsElementCollection;
import org.jetbrains.jps.model.impl.JpsElementChildRoleBase;
import org.jetbrains.jps.model.impl.JpsNamedElementReferenceBase;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleReference;

class JpsFlexBCReferenceImpl
  extends JpsNamedElementReferenceBase<JpsFlexBuildConfiguration, JpsFlexBuildConfiguration, JpsFlexBCReferenceImpl>
  implements JpsFlexBCReference {

  static final JpsElementChildRole<JpsFlexBCReference> ROLE = JpsElementChildRoleBase.create("build configuration reference");

  public JpsFlexBCReferenceImpl(@NotNull final String name, final JpsModuleReference moduleReference) {
    super(name, moduleReference);
  }

  private JpsFlexBCReferenceImpl(final JpsFlexBCReferenceImpl original) {
    super(original);
  }

  @NotNull
  public JpsFlexBCReferenceImpl createCopy() {
    return new JpsFlexBCReferenceImpl(this);
  }

  @Nullable
  @Override
  protected JpsElementCollection<? extends JpsFlexBuildConfiguration> getCollection(@NotNull JpsCompositeElement parent) {
    if (!(parent instanceof JpsModule)) return null;
    JpsFlexBuildConfigurationManager manager = JpsFlexBuildConfigurationManagerImpl.getManager((JpsModule)parent);
    return manager != null ? manager.getContainer().getChild(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE) : null;
  }

  @Nullable
  @Override
  protected JpsFlexBuildConfiguration resolve(JpsFlexBuildConfiguration element) {
    return element;
  }
}
