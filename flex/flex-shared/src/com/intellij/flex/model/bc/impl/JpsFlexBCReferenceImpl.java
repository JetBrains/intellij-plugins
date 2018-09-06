package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsFlexBCReference;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.module.JpsFlexModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsCompositeElement;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsElementCollection;
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase;
import org.jetbrains.jps.model.impl.JpsNamedElementReferenceBase;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleReference;
import org.jetbrains.jps.model.module.JpsTypedModule;

class JpsFlexBCReferenceImpl
  extends JpsNamedElementReferenceBase<JpsFlexBuildConfiguration, JpsFlexBuildConfiguration, JpsFlexBCReferenceImpl>
  implements JpsFlexBCReference {

  static final JpsElementChildRole<JpsFlexBCReference> ROLE = JpsElementChildRoleBase.create("build configuration reference");

  JpsFlexBCReferenceImpl(@NotNull final String name, final JpsModuleReference moduleReference) {
    super(name, moduleReference);
  }

  private JpsFlexBCReferenceImpl(final JpsFlexBCReferenceImpl original) {
    super(original);
  }

  @Override
  @NotNull
  public JpsFlexBCReferenceImpl createCopy() {
    return new JpsFlexBCReferenceImpl(this);
  }

  @Nullable
  @Override
  protected JpsElementCollection<? extends JpsFlexBuildConfiguration> getCollection(@NotNull JpsCompositeElement parent) {
    if (!(parent instanceof JpsModule)) return null;
    JpsTypedModule<JpsFlexBuildConfigurationManager> flexModule = ((JpsModule)parent).asTyped(JpsFlexModuleType.INSTANCE);
    if (flexModule == null) return null;

    return flexModule.getProperties().getContainer().getChild(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE);
  }

  @Nullable
  @Override
  protected JpsFlexBuildConfiguration resolve(JpsFlexBuildConfiguration element) {
    return element;
  }
}
