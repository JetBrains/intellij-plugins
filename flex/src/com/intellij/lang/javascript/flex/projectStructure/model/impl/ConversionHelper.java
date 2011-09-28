package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.module.Module;
import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public class ConversionHelper {

  private static class LightweightBuildConfigurationEntry implements BuildConfigurationEntry, ModifiableBuildConfigurationEntry {
    private final DependencyTypeImpl myDependencyType = new DependencyTypeImpl();

    @NotNull
    private final String myModuleName;

    @NotNull
    private final String myBcName;

    private LightweightBuildConfigurationEntry(@NotNull String moduleName,
                                               @NotNull String bcName) {
      myModuleName = moduleName;
      myBcName = bcName;
    }

    @NotNull
    @Override
    public String getBcName() {
      return myBcName;
    }

    @NotNull
    @Override
    public String getModuleName() {
      return myModuleName;
    }

    @Override
    public Module findModule() {
      throw new UnsupportedOperationException();
    }

    @Override
    public FlexIdeBuildConfiguration findBuildConfiguration() {
      throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ModifiableDependencyType getDependencyType() {
      return myDependencyType;
    }

    @Override
    public boolean isEqual(ModifiableDependencyEntry other) {
      if (!(other instanceof LightweightBuildConfigurationEntry)) return false;
      if (!myBcName.equals(((LightweightBuildConfigurationEntry)other).myBcName)) return false;
      if (!getModuleName().equals(((LightweightBuildConfigurationEntry)other).myModuleName)) return false;
      if (!myDependencyType.isEqual(((LightweightBuildConfigurationEntry)other).myDependencyType)) return false;
      return true;
    }
  }

  public static ModifiableBuildConfigurationEntry createBuildConfigurationEntry(String moduleName, String bcName) {
    return new LightweightBuildConfigurationEntry(moduleName, bcName);
  }

  public static FlexBuildConfigurationManagerImpl createBuildConfigurationManager() {
    FlexBuildConfigurationManagerImpl m = new FlexBuildConfigurationManagerImpl(null);
    m.setActiveBuildConfiguration(m.getBuildConfigurations()[0]);
    return m;
  }

  public static ModifiableModuleLibraryEntry createModuleLibraryEntry(String libraryId) {
    return new ModuleLibraryEntryImpl(libraryId);
  }

  //public static ModifiableFlexIdeBuildConfiguration createConfigInstance(String name) {
  //  FlexIdeBuildConfigurationImpl c = new FlexIdeBuildConfigurationImpl();
  //  c.setName(name);
  //  return c;
  //}
  //
  public static Element serialize(ModifiableFlexIdeBuildConfiguration configuration) {
    return XmlSerializer.serialize(((FlexIdeBuildConfigurationImpl)configuration).getState(), new SkipDefaultValuesSerializationFilters());
  }
}
