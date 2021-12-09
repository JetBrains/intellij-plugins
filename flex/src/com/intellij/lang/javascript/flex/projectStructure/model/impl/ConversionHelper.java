// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.application.options.PathMacrosImpl;
import com.intellij.application.options.ReplacePathToMacroMap;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.components.ExpandMacroToPathMap;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public final class ConversionHelper {

  private static final class LightweightBuildConfigurationEntry
    implements BuildConfigurationEntry, ModifiableBuildConfigurationEntry, StatefulDependencyEntry {
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
    public FlexBuildConfiguration findBuildConfiguration() {
      throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ModifiableDependencyType getDependencyType() {
      return myDependencyType;
    }

    @Override
    public EntryState getState() {
      EntryState state = new EntryState();
      state.MODULE_NAME = getModuleName();
      state.BC_NAME = myBcName;
      state.DEPENDENCY_TYPE = myDependencyType.getState();
      return state;
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

  public static ModifiableFlexBuildConfiguration createBuildConfiguration(FlexBuildConfigurationManagerImpl m) {
    FlexBuildConfigurationImpl[] configurations = m.doGetBuildConfigurations();
    FlexBuildConfigurationImpl result = new FlexBuildConfigurationImpl();
    m.doSetBuildConfigurations(ArrayUtil.append(configurations, result));
    return result;
  }

  public static ModifiableModuleLibraryEntry createModuleLibraryEntry(String libraryId) {
    return new ModuleLibraryEntryImpl(libraryId);
  }

  public static ModifiableSharedLibraryEntry createSharedLibraryEntry(final String libraryName, final String libraryLevel) {
    return new SharedLibraryEntryImpl(libraryName, libraryLevel);
  }

  //public static ModifiableFlexBuildConfiguration createConfigInstance(String name) {
  //  FlexBuildConfigurationImpl c = new FlexBuildConfigurationImpl();
  //  c.setName(name);
  //  return c;
  //}
  //

  public static Element serialize(PersistentStateComponent c) {
    Element element = XmlSerializer.serialize(c.getState(), new SkipDefaultValuesSerializationFilters());
    collapsePaths(element);
    return element;
  }

  public static void collapsePaths(Element element) {
    ReplacePathToMacroMap map = new ReplacePathToMacroMap();
    PathMacrosImpl.getInstanceEx().addMacroReplacements(map);
    map.substitute(element, SystemInfo.isFileSystemCaseSensitive, true);
  }
  public static void expandPaths(Element element) {
    ExpandMacroToPathMap map = new ExpandMacroToPathMap();
    PathMacrosImpl.getInstanceEx().addMacroExpands(map);
    map.substitute(element, SystemInfo.isFileSystemCaseSensitive, true);
  }



}
