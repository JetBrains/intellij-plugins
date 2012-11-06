package com.intellij.flex.model.bc.impl;

import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.bc.JpsFlexModuleOrProjectCompilerOptions;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElementCollection;
import org.jetbrains.jps.model.ex.JpsCompositeElementBase;
import org.jetbrains.jps.model.module.JpsModule;

import java.util.ArrayList;
import java.util.List;

public class JpsFlexBuildConfigurationManagerImpl extends JpsCompositeElementBase<JpsFlexBuildConfigurationManagerImpl>
  implements JpsFlexBuildConfigurationManager {

  private static final Logger LOG = Logger.getInstance(JpsFlexBuildConfigurationManagerImpl.class.getName());

  private JpsFlexBuildConfiguration myActiveConfiguration;

  public JpsFlexBuildConfigurationManagerImpl() {
    myContainer.setChild(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE);
    myContainer.setChild(JpsFlexCompilerOptionsRole.INSTANCE);
  }

  private JpsFlexBuildConfigurationManagerImpl(final JpsFlexBuildConfigurationManagerImpl original) {
    super(original);
  }

  public static JpsFlexBuildConfigurationManager getManager(final JpsModule module) {
    return (JpsFlexBuildConfigurationManager)module.getProperties();
  }

  @NotNull
  public JpsFlexBuildConfigurationManagerImpl createCopy() {
    return new JpsFlexBuildConfigurationManagerImpl(this);
  }

  public void applyChanges(@NotNull final JpsFlexBuildConfigurationManagerImpl modified) {
    super.applyChanges(modified);
    //..
  }

// ------------------------------

  public List<JpsFlexBuildConfiguration> getBuildConfigurations() {
    return myContainer.getChild(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE).getElements();
  }

  //public JpsFlexBuildConfiguration addBuildConfiguration(String name) {
  //  return myContainer.getChild(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE).addChild(new JpsFlexBuildConfigurationImpl(name));
  //}

  public JpsFlexBuildConfiguration getActiveConfiguration() {
    return myActiveConfiguration;
  }

  @Override
  @Nullable
  public JpsFlexBuildConfiguration findConfigurationByName(final String name) {
    for (JpsFlexBuildConfiguration configuration : getBuildConfigurations()) {
      if (configuration.getName().equals(name)) {
        return configuration;
      }
    }

    return null;
  }

  public JpsFlexModuleOrProjectCompilerOptions getModuleLevelCompilerOptions() {
    return myContainer.getChild(JpsFlexCompilerOptionsRole.INSTANCE);
  }

  public JpsFlexBuildConfiguration createTemporaryCopyForCompilation(@NotNull final JpsFlexBuildConfiguration bc) {
    final JpsFlexBuildConfigurationImpl copy = ((JpsFlexBuildConfigurationImpl)bc).createCopy();
    copy.setParent(((JpsFlexBuildConfigurationImpl)bc).getParent());
    copy.setTempBCForCompilation(true);
    return copy;
  }

  private void updateActiveConfiguration(@Nullable final String activeBCName) {
    final List<JpsFlexBuildConfiguration> bcs = getBuildConfigurations();
    if (!bcs.isEmpty()) {
      myActiveConfiguration =
        activeBCName != null ? ContainerUtil.find(bcs, new Condition<JpsFlexBuildConfiguration>() {
          @Override
          public boolean value(JpsFlexBuildConfiguration bc) {
            return bc.getName().equals(activeBCName);
          }
        }) : null;

      if (myActiveConfiguration == null) {
        myActiveConfiguration = bcs.get(0);
      }
    }

    LOG.error("No Flex build configurations");
    myActiveConfiguration = null;
  }

// ------------------------------

  public State getState() {
    final State state = new State();
    for (JpsFlexBuildConfiguration configuration : getBuildConfigurations()) {
      state.CONFIGURATIONS.add(((JpsFlexBuildConfigurationImpl)configuration).getState());
    }

    state.ACTIVE_BC_NAME = myActiveConfiguration != null ? myActiveConfiguration.getName() : null;

    state.MODULE_LEVEL_COMPILER_OPTIONS = ((JpsFlexCompilerOptionsImpl)getModuleLevelCompilerOptions()).getState();
    return state;
  }

  public void loadState(final State state) {
    //if (myModule == null) {
    //  throw new IllegalStateException("Cannot load state of a dummy config manager instance");
    //}
    final JpsElementCollection<JpsFlexBuildConfiguration> bcs = myContainer.getChild(JpsFlexBuildConfigurationImpl.COLLECTION_ROLE);
    LOG.assertTrue(bcs.getElements().size() == 0);

    for (JpsFlexBCState configurationState : state.CONFIGURATIONS) {
      JpsFlexBuildConfigurationImpl bc = new JpsFlexBuildConfigurationImpl(configurationState.NAME);
      bc.loadState(configurationState);
      bcs.addChild(bc);
    }

    if (bcs.getElements().isEmpty()) {
      LOG.warn("Flex build configurations not loaded from *.iml.");
      bcs.addChild(new JpsFlexBuildConfigurationImpl(JpsFlexBuildConfiguration.UNNAMED));
    }

    updateActiveConfiguration(state.ACTIVE_BC_NAME);

    ((JpsFlexCompilerOptionsImpl)getModuleLevelCompilerOptions()).loadState(state.MODULE_LEVEL_COMPILER_OPTIONS);
  }

  public static class State {
    @Tag("configurations")
    @AbstractCollection(surroundWithTag = false, elementTag = "configuration")
    public List<JpsFlexBCState> CONFIGURATIONS = new ArrayList<JpsFlexBCState>();

    @Property(surroundWithTag = false)
    public JpsFlexCompilerOptionsImpl.State MODULE_LEVEL_COMPILER_OPTIONS = new JpsFlexCompilerOptionsImpl.State();

    @Attribute("active")
    public String ACTIVE_BC_NAME;
  }
}
