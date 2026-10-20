// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.projectStructure;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.flex.util.JSDaemonAnalyzerTestCase;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableBuildConfigurationEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationManagerImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.CustomImlComponentService;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PlatformTestUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationManagerImpl.COMPONENT_NAME;

public class FlexBuildConfigurationManagerPersistenceTest extends JSDaemonAnalyzerTestCase {
  private static final String FIRST_BC = "first";
  private static final String SECOND_BC = "second";
  private static final String LOADED_BC = "loaded";
  private static final String MAIN_CLASS = "Main";
  private static final String ADDITIONAL_OPTIONS = "-debug";
  private static final String SECOND_MODULE_NAME = "module2";

  @Override
  public void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
  }

  @Override
  protected @NotNull ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @Override
  protected String getBasePath() {
    return "";
  }

  @Override
  protected String getExtension() {
    return "as";
  }

  public void testCommitStoresConfigurations() {
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      editor.getConfigurations(myModule)[0].setName(FIRST_BC);
      editor.createConfiguration(myModule).setName(SECOND_BC);
    });
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();

    assertEquals(List.of(FIRST_BC, SECOND_BC), storedConfigurationNames(myModule));
  }

  public void testConfigurationsKeepIdentityAcrossCommit() {
    final FlexBuildConfiguration before = FlexBuildConfigurationManager.getInstance(myModule).getBuildConfigurations()[0];

    FlexTestUtils.modifyConfigs(myProject, editor -> editor.getConfigurations(myModule)[0].setName(FIRST_BC));

    final FlexBuildConfiguration after = FlexBuildConfigurationManager.getInstance(myModule).getBuildConfigurations()[0];
    assertSame(before, after);
    assertEquals(FIRST_BC, after.getName());
  }

  public void testActiveConfigurationIsStored() {
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      editor.getConfigurations(myModule)[0].setName(FIRST_BC);
      editor.createConfiguration(myModule).setName(SECOND_BC);
    });
    final FlexBuildConfigurationManager manager = FlexBuildConfigurationManager.getInstance(myModule);

    manager.setActiveBuildConfiguration(manager.findConfigurationByName(SECOND_BC));
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();

    assertEquals(SECOND_BC, storedState(myModule).myActiveConfigurationName);
  }

  public void testModuleLevelCompilerOptionIsStored() {
    FlexBuildConfigurationManager.getInstance(myModule).getModuleLevelCompilerOptions().setAdditionalOptions(ADDITIONAL_OPTIONS);
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();

    assertEquals(ADDITIONAL_OPTIONS, storedState(myModule).myModuleLevelCompilerOptions.additionalOptions);
  }

  public void testNonStructuralModificationIsStored() {
    final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration();

    FlexProjectConfigurationEditor.makeNonStructuralModification(myModule, bc, configuration -> configuration.setMainClass(MAIN_CLASS));
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();

    assertEquals(MAIN_CLASS, storedState(myModule).CONFIGURATIONS.get(0).MAIN_CLASS);
  }

  public void testStoredComponentLoadsIntoNewManager() {
    FlexTestUtils.modifyConfigs(myProject, editor -> editor.getConfigurations(myModule)[0].setName(LOADED_BC));
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();

    assertEquals(LOADED_BC, new FlexBuildConfigurationManagerImpl(myModule).getActiveConfiguration().getName());
  }

  public void testRemovedModuleDependencyIsDroppedAndStored() throws Exception {
    final Module module2 = createFlexModule();
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration app = editor.getConfigurations(myModule)[0];
      app.setName(FIRST_BC);
      final ModifiableFlexBuildConfiguration lib = editor.getConfigurations(module2)[0];
      lib.setName(SECOND_BC);
      final ModifiableBuildConfigurationEntry entry = editor.createBcEntry(app.getDependencies(), lib, null);
      app.getDependencies().getModifiableEntries().add(entry);
    });
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
    assertEquals(1, storedState(myModule).CONFIGURATIONS.get(0).DEPENDENCIES.ENTRIES.length);

    WriteAction.run(() -> ModuleManager.getInstance(myProject).disposeModule(module2));
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();

    assertEmpty(FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration().getDependencies().getEntries());
    assertEquals(0, storedState(myModule).CONFIGURATIONS.get(0).DEPENDENCIES.ENTRIES.length);
  }

  private Module createFlexModule() throws Exception {
    return WriteAction.compute(() -> {
      final ModifiableModuleModel model = ModuleManager.getInstance(myProject).getModifiableModel();
      final VirtualFile moduleDir = getOrCreateProjectBaseDir().createChildDirectory(this, SECOND_MODULE_NAME);
      final Module module = model.newModule(moduleDir.toNioPath(), FlexModuleType.getInstance().getId());
      model.commit();
      return module;
    });
  }

  private FlexBuildConfigurationManagerImpl.State storedState(final Module module) {
    final FlexBuildConfigurationManagerImpl.State state =
      CustomImlComponentService.getInstance(myProject).getComponentValue(module, COMPONENT_NAME, FlexBuildConfigurationManagerImpl.State.class);
    assertNotNull(state);
    return state;
  }

  private List<String> storedConfigurationNames(final Module module) {
    return storedState(module).CONFIGURATIONS.stream().map(it -> it.NAME).toList();
  }
}
