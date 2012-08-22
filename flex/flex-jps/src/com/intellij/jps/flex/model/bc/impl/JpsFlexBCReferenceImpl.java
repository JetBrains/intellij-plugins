package com.intellij.jps.flex.model.bc.impl;

import com.intellij.jps.flex.model.bc.JpsFlexBCReference;
import com.intellij.jps.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.jps.flex.model.bc.JpsFlexBuildConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsElementChildRole;
import org.jetbrains.jps.model.JpsElementReference;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.impl.JpsCompositeElementBase;
import org.jetbrains.jps.model.impl.JpsElementChildRoleBase;
import org.jetbrains.jps.model.impl.JpsNamedElementReferenceImpl;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleReference;

class JpsFlexBCReferenceImpl extends JpsNamedElementReferenceImpl<JpsFlexBuildConfiguration, JpsFlexBCReferenceImpl>
  implements JpsFlexBCReference {

  static final JpsElementChildRole<JpsFlexBCReference> ROLE = JpsElementChildRoleBase.create("build configuration reference");

  public JpsFlexBCReferenceImpl(@NotNull final String name, final JpsModuleReference moduleReference) {
    super(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE, name, new JpsFlexBCManagerReference(moduleReference));
  }

  private JpsFlexBCReferenceImpl(final JpsFlexBCReferenceImpl original) {
    super(original);
  }

  @NotNull
  public JpsFlexBCReferenceImpl createCopy() {
    return new JpsFlexBCReferenceImpl(this);
  }

  public JpsElementReference<JpsFlexBuildConfiguration> asExternal(@NotNull final JpsModel model) {
    model.registerExternalReference(this);
    return this;
  }

  private static class JpsFlexBCManagerReference extends JpsCompositeElementBase<JpsFlexBCManagerReference>
    implements JpsElementReference<JpsFlexBuildConfigurationManager> {

    private static final JpsElementChildRole<JpsModuleReference> ROLE = JpsElementChildRoleBase.create("module reference");

    private JpsFlexBCManagerReference() {
    }

    public JpsFlexBCManagerReference(final JpsModuleReference reference) {
      myContainer.setChild(ROLE, reference);
    }

    @NotNull
    public JpsFlexBCManagerReference createCopy() {
      return new JpsFlexBCManagerReference();
    }

    public JpsFlexBuildConfigurationManager resolve() {
      final JpsModule module = myContainer.getChild(ROLE).resolve();
      return module != null ? JpsFlexBuildConfigurationManagerImpl.getManager(module) : null;
    }

    public JpsElementReference<JpsFlexBuildConfigurationManager> asExternal(@NotNull final JpsModel model) {
      model.registerExternalReference(this);
      return this;
    }
  }
}
