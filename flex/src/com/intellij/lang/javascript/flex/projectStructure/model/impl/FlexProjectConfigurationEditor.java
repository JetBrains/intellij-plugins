// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.FlexCompositeSdkManager;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexBCConfigurable;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.libraries.LibraryEditingUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.util.*;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ChangeListener;
import java.util.*;

public class FlexProjectConfigurationEditor implements Disposable {

  private static final Logger LOG = Logger.getInstance(FlexProjectConfigurationEditor.class.getName());

  private static class Editor extends FlexBuildConfigurationImpl {
    private final Module myModule;
    private final FlexBuildConfigurationImpl myOrigin;
    @Nullable
    private final String myOriginalName;

    Editor(Module module, @Nullable FlexBuildConfigurationImpl origin, boolean storeOriginalName) {
      myOrigin = origin != null ? origin : new FlexBuildConfigurationImpl();
      myOriginalName = storeOriginalName && origin != null ? origin.getName() : null;
      myModule = module;
      myOrigin.applyTo(this);
    }

    public FlexBuildConfigurationImpl commit() {
      applyTo(myOrigin);
      return myOrigin;
    }

    public boolean isModified() {
      return !isEqual(myOrigin);
    }

    @Nullable
    public String getOriginalName() {
      return myOriginalName;
    }
  }

  public interface ProjectModifiableModelProvider {
    Module[] getModules();

    ModifiableRootModel getModuleModifiableModel(Module module);

    void addListener(FlexBCConfigurator.Listener listener, Disposable parentDisposable);

    void commitModifiableModels() throws ConfigurationException;

    @Nullable
    Library findSourceLibraryForLiveName(String name, @NotNull String level);

    @Nullable
    Library findSourceLibrary(String name, @NotNull String level);
  }

  public interface ModulesModelChangeListener extends EventListener {
    void modulesModelsChanged(Collection<Module> modules);
  }

  private boolean myDisposed;
  private final ProjectModifiableModelProvider myProvider;

  @Nullable
  private final Project myProject;

  private final Map<Module, List<Editor>> myModule2Editors = new HashMap<>();
  //private final FlexSdksEditor mySdksEditor;
  private final EventDispatcher<ModulesModelChangeListener> myModulesModelChangeEventDispatcher =
    EventDispatcher.create(ModulesModelChangeListener.class);

  public FlexProjectConfigurationEditor(@Nullable Project project, ProjectModifiableModelProvider provider) {
    myProject = project;
    myProvider = provider;
    //mySdksEditor = new FlexSdksEditor(this);

    provider.addListener(new FlexBCConfigurator.Listener() {
      @Override
      public void moduleRemoved(final Module module) {
        if (!isFlex(module)) {
          return;
        }

        LOG.assertTrue(myModule2Editors.containsKey(module), "Unknown module: " + module);
        myModule2Editors.remove(module);
        //Condition<OrderEntry> c = new Condition<OrderEntry>() {
        //  @Override
        //  public boolean value(OrderEntry orderEntry) {
        //    return orderEntry instanceof ModuleOrderEntry && ((ModuleOrderEntry)orderEntry).getModule() == module;
        //  }
        //};
        //for (Module m : myModule2Editors.keySet()) {
        //  ModifiableRootModel modifiableModel = myProvider.getModuleModifiableModel(m);
        //  List<OrderEntry> orderEntriesToRemove = ContainerUtil.findAll(modifiableModel.getOrderEntries(), c);
        //  for (OrderEntry orderEntry : orderEntriesToRemove) {
        //    modifiableModel.removeOrderEntry(orderEntry);
        //  }
        //}
      }

      @Override
      public void buildConfigurationRemoved(FlexBCConfigurable configurable) {
        configurationRemoved(configurable.getEditableObject());
      }

      @Override
      public void natureChanged(final FlexBCConfigurable configurable) {
      }

      @Override
      public void buildConfigurationRenamed(final FlexBCConfigurable configurable) {
      }
    }, this);

    for (Module module : provider.getModules()) {
      if (!isFlex(module)) {
        continue;
      }
      addEditorsForModule(module);
    }
  }

  /**
   * Clients are responsible to commit or dispose returned {@code FlexProjectConfigurationEditor} as well as passed {@code projectLibrariesModel} and {@code globalLibrariesModel}.<br><br>
   * If {@code null} is given as {@code projectLibrariesModel} or {@code globalLibrariesModel} then the client must not set dependency on libraries of respective level.<br><br>
   * Removing of modules and build configurations is not allowed while returned {@code FlexProjectConfigurationEditor} exists.
   */
  public static FlexProjectConfigurationEditor createEditor(final Project project,
                                                            final Map<Module, ModifiableRootModel> moduleToModifiableModel,
                                                            final @Nullable LibraryTable.ModifiableModel projectLibrariesModel,
                                                            final @Nullable LibraryTable.ModifiableModel globalLibrariesModel) {
    LOG.assertTrue(FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor() == null,
                   "Don't create FlexProjectConfigurationEditor when Project Structure dialog is open. Use FlexBCConfigurator.getConfigEditor()");

    final ProjectModifiableModelProvider provider =
      createModelProvider(moduleToModifiableModel, projectLibrariesModel, globalLibrariesModel);

    return new FlexProjectConfigurationEditor(project, provider);
  }

  public static ProjectModifiableModelProvider createModelProvider(final Map<Module, ModifiableRootModel> moduleToModifiableModel,
                                                                   final @Nullable LibraryTable.ModifiableModel projectLibrariesModel,
                                                                   final @Nullable LibraryTable.ModifiableModel globalLibrariesModel) {
    return new ProjectModifiableModelProvider() {
      @Override
      public Module[] getModules() {
        final Set<Module> modules = moduleToModifiableModel.keySet();
        return modules.toArray(Module.EMPTY_ARRAY);
      }

      @Override
      public ModifiableRootModel getModuleModifiableModel(final Module module) {
        final ModifiableRootModel model = moduleToModifiableModel.get(module);
        LOG.assertTrue(model != null, "No model for module " + module.getName());
        return model;
      }

      @Override
      public void addListener(final FlexBCConfigurator.Listener listener,
                              final Disposable parentDisposable) {
        // modules and BCs must not be removed
      }

      @Override
      public void commitModifiableModels() throws ConfigurationException {
        // commit must be performed somewhere else
      }

      @Override
      @Nullable
      public Library findSourceLibrary(final String name, @NotNull final String level) {
        if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(level)) {
          return globalLibrariesModel.getLibraryByName(name);
        }
        else if (LibraryTablesRegistrar.PROJECT_LEVEL.equals(level)) {
          LOG.assertTrue(projectLibrariesModel != null);
          return projectLibrariesModel.getLibraryByName(name);
        }
        LOG.error("Unexpected argument: " + level);
        return null;
      }

      @Override
      public Library findSourceLibraryForLiveName(final String name, @NotNull final String level) {
        return findSourceLibrary(name, level);
      }
    };
  }

  public void configurationRemoved(@NotNull final ModifiableFlexBuildConfiguration configuration) {
    assertAlive();
    Editor editor = (Editor)configuration;
    List<Editor> editors = myModule2Editors.get(editor.myModule);
    boolean contained = editors.remove(editor);
    LOG.assertTrue(contained);
  }

  public void addModulesModelChangeListener(ModulesModelChangeListener listener, Disposable parentDisposable) {
    myModulesModelChangeEventDispatcher.addListener(listener, parentDisposable);
  }

  private void addEditorsForModule(Module module) {
    FlexBuildConfiguration[] buildConfigurations = FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations();
    List<Editor> configEditors = new ArrayList<>(buildConfigurations.length);
    for (FlexBuildConfiguration buildConfiguration : buildConfigurations) {
      configEditors.add(new Editor(module, (FlexBuildConfigurationImpl)buildConfiguration, true));
    }
    myModule2Editors.put(module, configEditors);
  }

  @Override
  public void dispose() {
    myDisposed = true;
    myModule2Editors.clear();
  }

  public ModifiableFlexBuildConfiguration[] getConfigurations(Module module) {
    assertAlive();
    List<Editor> editors = myModule2Editors.get(module);
    if (editors == null) {
      // module was just created
      addEditorsForModule(module);
      editors = myModule2Editors.get(module);
    }
    return editors.toArray(new ModifiableFlexBuildConfiguration[0]);
  }

  private void assertAlive() {
    LOG.assertTrue(!myDisposed, "Already disposed");
  }

  public ModifiableFlexBuildConfiguration createConfiguration(Module module) {
    assertAlive();
    List<Editor> editors = myModule2Editors.get(module);
    Editor newConfig = new Editor(module, null, false);
    editors.add(newConfig);
    return newConfig;
  }

  public ModifiableFlexBuildConfiguration copyConfiguration(ModifiableFlexBuildConfiguration configuration,
                                                            BuildConfigurationNature newNature) {
    assertAlive();
    Module module = ((Editor)configuration).myModule;
    List<Editor> editors = myModule2Editors.get(module);
    FlexBuildConfigurationImpl copy = ((Editor)configuration).getCopy();
    DependencyEntry[] entries = copy.getDependencies().getEntries();
    ModifiableRootModel modifiableModel = myProvider.getModuleModifiableModel(module);
    for (int i = 0; i < entries.length; i++) {
      if (entries[i] instanceof ModuleLibraryEntryImpl e) {
        LibraryEx library = findLibrary(modifiableModel, e.getLibraryId());
        if (library != null) {
          LibraryEx libraryCopy = copyModuleLibrary(modifiableModel, library);
          ModuleLibraryEntryImpl entryCopy = new ModuleLibraryEntryImpl(FlexProjectRootsUtil.getLibraryId(libraryCopy));
          e.getDependencyType().applyTo(entryCopy.getDependencyType());
          copy.getDependencies().getModifiableEntries().set(i, entryCopy);
        }
      }
    }
    Editor newConfig = new Editor(module, copy, false);
    newConfig.setNature(newNature);
    // just to simplify serialized view
    resetNonApplicableValuesToDefaults(newConfig);
    editors.add(newConfig);
    return newConfig;
  }

  private static LibraryEx copyModuleLibrary(final ModifiableRootModel modifiableModel, final LibraryEx library) {
    LibraryTable.ModifiableModel librariesModifiableModel = getTableModifiableModel(modifiableModel);
    LibraryEx libraryCopy = (LibraryEx)librariesModifiableModel.createLibrary(library.getName(), library.getKind());
    LibraryEx.ModifiableModelEx libraryCopyModel = libraryCopy.getModifiableModel();
    LibraryEditingUtil.copyLibrary(library, Collections.emptyMap(), libraryCopyModel); // will overwrite library id
    libraryCopyModel.setProperties(new FlexLibraryProperties(FlexLibraryIdGenerator.generateId())); // do assign unique library id
    libraryCopyModel.commit();
    return libraryCopy;
  }

  public static void resetNonApplicableValuesToDefaults(final ModifiableFlexBuildConfiguration bc) {
    final FlexBuildConfiguration defaultConfiguration = new FlexBuildConfigurationImpl();
    final BuildConfigurationNature nature = bc.getNature();

    if (bc.getOutputType() != OutputType.RuntimeLoadedModule) {
      bc.setOptimizeFor(defaultConfiguration.getOptimizeFor());
    }

    if (nature.isLib()) {
      bc.setMainClass(defaultConfiguration.getMainClass());
    }

    if (!nature.isWebPlatform() || !nature.isApp()) {
      bc.setUseHtmlWrapper(defaultConfiguration.isUseHtmlWrapper());
      bc.setWrapperTemplatePath(defaultConfiguration.getWrapperTemplatePath());
    }

    if (!BCUtils.canHaveRLMsAndRuntimeStylesheets(bc)) {
      bc.setRLMs(defaultConfiguration.getRLMs());
      bc.setCssFilesToCompile(defaultConfiguration.getCssFilesToCompile());
    }

    if (!ArrayUtil.contains(bc.getDependencies().getFrameworkLinkage(), BCUtils.getSuitableFrameworkLinkages(nature))) {
      bc.getDependencies().setFrameworkLinkage(defaultConfiguration.getDependencies().getFrameworkLinkage());
    }

    if (!nature.isWebPlatform()) {
      bc.getDependencies().setTargetPlayer(defaultConfiguration.getDependencies().getTargetPlayer());
    }

    if (nature.isMobilePlatform() || bc.isPureAs()) {
      bc.getDependencies().setComponentSet(defaultConfiguration.getDependencies().getComponentSet());
    }

    for (Iterator<ModifiableDependencyEntry> i = bc.getDependencies().getModifiableEntries().iterator(); i.hasNext(); ) {
      final ModifiableDependencyEntry entry = i.next();
      if (entry instanceof BuildConfigurationEntry) {
        final FlexBuildConfiguration dependencyBC = ((BuildConfigurationEntry)entry).findBuildConfiguration();
        if (dependencyBC == null || !FlexCommonUtils.checkDependencyType(bc.getOutputType(),
                                                                         dependencyBC.getOutputType(),
                                                                         entry.getDependencyType().getLinkageType())) {
          i.remove();
        }
      }
    }

    if (bc.getTargetPlatform() != TargetPlatform.Desktop || bc.getOutputType() != OutputType.Application) {
      ((AirDesktopPackagingOptionsImpl)defaultConfiguration.getAirDesktopPackagingOptions())
        .applyTo(((AirDesktopPackagingOptionsImpl)bc.getAirDesktopPackagingOptions()));
    }

    if (bc.getTargetPlatform() != TargetPlatform.Mobile || bc.getOutputType() != OutputType.Application) {
      ((AndroidPackagingOptionsImpl)defaultConfiguration.getAndroidPackagingOptions())
        .applyTo(((AndroidPackagingOptionsImpl)bc.getAndroidPackagingOptions()));
      ((IosPackagingOptionsImpl)defaultConfiguration.getIosPackagingOptions())
        .applyTo(((IosPackagingOptionsImpl)bc.getIosPackagingOptions()));
    }

    if (!nature.isLib()) {
      bc.getCompilerOptions().setFilesToIncludeInSWC(Collections.emptyList());
    }
  }

  public Module getModule(ModifiableFlexBuildConfiguration configuration) {
    assertAlive();
    return ((Editor)configuration).myModule;
  }

  @Nullable
  public Project getProject() {
    return myProject;
  }

  public void checkCanCommit() throws ConfigurationException {
    for (Module module : myModule2Editors.keySet()) {
      List<Editor> editors = myModule2Editors.get(module);
      Set<String> names = new HashSet<>();
      for (Editor editor : editors) {
        if (!names.add(editor.getName())) {
          throw new ConfigurationException(
            "Duplicate build configuration name '" + editor.getName() + "' in module '" + module.getName() + "'");
        }
      }
    }
  }

  public void commit() throws ConfigurationException {
    final Map<Pair<String, String>, String> renamedConfigs = new HashMap<>();
    for (Module module : myModule2Editors.keySet()) {
      ModifiableRootModel modifiableModel = myProvider.getModuleModifiableModel(module);
      Collection<String> usedModulesLibrariesIds = new ArrayList<>();

      // ---------------- SDK and shared libraries entries ----------------------
      Map<Library, Boolean> librariesToAdd = new LinkedHashMap<>(); // Library -> add_library_entry_flag

      final Collection<String> sdkNames = new HashSet<>();
      for (Editor editor : myModule2Editors.get(module)) {
        final SdkEntry sdkEntry = editor.getDependencies().getSdkEntry();
        if (sdkEntry != null) {
          sdkNames.add(sdkEntry.getName());
        }

        for (DependencyEntry dependencyEntry : editor.getDependencies().getEntries()) {
          if (dependencyEntry instanceof ModuleLibraryEntry moduleLibraryEntry) {
            usedModulesLibrariesIds.add(moduleLibraryEntry.getLibraryId());
          }
          if (dependencyEntry instanceof SharedLibraryEntry sharedLibraryEntry) {
            Library library =
              myProvider.findSourceLibraryForLiveName(sharedLibraryEntry.getLibraryName(), sharedLibraryEntry.getLibraryLevel());
            if (library != null) {
              librariesToAdd.put(library, true);
            }
          }
        }
        String originalName = editor.getOriginalName();
        if (originalName != null && !originalName.equals(editor.getName())) {
          renamedConfigs.put(Pair.create(module.getName(), originalName), editor.getName());
        }
      }

      final Sdk sdk;
      if (sdkNames.isEmpty()) {
        sdk = null;
      }
      else if (sdkNames.size() == 1) {
        sdk = FlexSdkUtils.findFlexOrFlexmojosSdk(sdkNames.iterator().next());
      }
      else {
        sdk = FlexCompositeSdkManager.getInstance().getOrCreateSdk(ArrayUtilRt.toStringArray(sdkNames));
      }
      modifiableModel.setSdk(sdk);

      Collection<OrderEntry> entriesToRemove = new ArrayList<>();
      for (OrderEntry orderEntry : modifiableModel.getOrderEntries()) {
        if (orderEntry instanceof LibraryOrderEntry) {
          if (((LibraryOrderEntry)orderEntry).isModuleLevel()) {
            LibraryEx library = (LibraryEx)((LibraryOrderEntry)orderEntry).getLibrary();
            if (FlexProjectRootsUtil.isFlexLibrary(library) &&
                !usedModulesLibrariesIds.contains(FlexProjectRootsUtil.getLibraryId(library))) {
              entriesToRemove.add(orderEntry);
            }
          }
          else {
            LibraryEx library = (LibraryEx)((LibraryOrderEntry)orderEntry).getLibrary();
            if (librariesToAdd.containsKey(library)) {
              librariesToAdd.put(library, false); // entry already exists for this library
            }
            else if (library != null && FlexProjectRootsUtil.isFlexLibrary(library)) {
              entriesToRemove.add(orderEntry);
            }
          }
        }
      }
      for (OrderEntry e : entriesToRemove) {
        modifiableModel.removeOrderEntry(e);
      }

      for (Library library : librariesToAdd.keySet()) {
        if (!((LibraryEx)library).isDisposed() && librariesToAdd.get(library) &&
            myProvider.findSourceLibrary(library.getName(), library.getTable().getTableLevel()) != null) {
          modifiableModel.addLibraryEntry(library);
        }
      }

      // ---------------- modules entries ----------------------
      final Set<Module> modulesToAdd = new HashSet<>();
      for (Editor editor : myModule2Editors.get(module)) {
        for (DependencyEntry dependencyEntry : editor.getDependencies().getEntries()) {
          if (dependencyEntry instanceof BuildConfigurationEntry) {
            final Module dependencyModule = findModuleWithBC((BuildConfigurationEntry)dependencyEntry);
            if (dependencyModule != null && dependencyModule != module) {
              modulesToAdd.add(dependencyModule);
            }
          }
        }
      }

      List<OrderEntry> moduleOrderEntriesToRemove = ContainerUtil.filter(modifiableModel.getOrderEntries(),
                                                                         orderEntry -> orderEntry instanceof ModuleOrderEntry && !modulesToAdd.remove(((ModuleOrderEntry)orderEntry).getModule()));

      for (OrderEntry orderEntry : moduleOrderEntriesToRemove) {
        modifiableModel.removeOrderEntry(orderEntry);
      }
      for (Module moduleToAdd : modulesToAdd) {
        modifiableModel.addModuleOrderEntry(moduleToAdd);
      }

      for (OrderEntry entry : modifiableModel.getOrderEntries()) {
        if (entry instanceof ExportableOrderEntry) {
          // transitiveness will be filtered out in FlexOrderEnumeratorHandler if needed
          ((ExportableOrderEntry)entry).setExported(true);
        }
      }
    }

    // ---------------- do commit ----------------------
    Collection<Module> modulesWithChangedModifiableModel = ContainerUtil.findAll(myModule2Editors.keySet(),
                                                                                 module -> myProvider.getModuleModifiableModel(module).isChanged());

    if (!modulesWithChangedModifiableModel.isEmpty()) {
      myProvider.commitModifiableModels();
      myModulesModelChangeEventDispatcher.getMulticaster().modulesModelsChanged(modulesWithChangedModifiableModel);
    }

    ApplicationManager.getApplication().runWriteAction(() -> {
      for (Module module : myModule2Editors.keySet()) {
        Function<Editor, FlexBuildConfigurationImpl> f = editor -> editor.commit();
        FlexBuildConfigurationImpl[] current =
          ContainerUtil.map2Array(myModule2Editors.get(module), FlexBuildConfigurationImpl.class, f);
        ((FlexBuildConfigurationManagerImpl)FlexBuildConfigurationManager.getInstance(module)).setBuildConfigurations(current);
      }

      //if (mySdksEditor.isModified()) {
      //  mySdksEditor.commit();
      //}

      if (myProject != null) {
        FlexBuildConfigurationManagerImpl.resetHighlighting(myProject);
        if (!renamedConfigs.isEmpty()) {
          myProject.getMessageBus().syncPublisher(FlexBuildConfigurationChangeListener.TOPIC).buildConfigurationsRenamed(renamedConfigs);
        }
      }
    });
  }

  public boolean isModified() {
    if (myDisposed) {
      return false;
    }

    for (Module module : myModule2Editors.keySet()) {
      if (myProvider.getModuleModifiableModel(module).isChanged()) {
        return true;
      }

      FlexBuildConfiguration[] originalConfigurations = FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations();
      List<Editor> currentConfigurations = myModule2Editors.get(module);
      if (originalConfigurations.length != currentConfigurations.size()) {
        return true;
      }
      for (Editor currentConfiguration : currentConfigurations) {
        if (currentConfiguration.isModified()) {
          return true;
        }
      }
    }

    //if (mySdksEditor.isModified()) {
    //  return true;
    //}
    return false;
  }

  private static boolean isFlex(Module module) {
    return ModuleType.get(module) == FlexModuleType.getInstance();
  }

  public ModifiableBuildConfigurationEntry createBcEntry(ModifiableDependencies dependant,
                                                         ModifiableFlexBuildConfiguration dependency,
                                                         @Nullable String dependencyCurrentName) {
    assertAlive();
    Module dependencyModule = ((Editor)dependency).myModule;
    ModifiableBuildConfigurationEntry e =
      new BuildConfigurationEntryImpl(dependencyModule, dependencyCurrentName != null ? dependencyCurrentName : dependency.getName());

    //Module dependantModule = getEditor(dependant).myModule;
    //if (dependantModule != dependencyModule) {
    //  ModifiableRootModel modifiableModel = myProvider.getModuleModifiableModel(dependantModule);
    //  if (!ArrayUtil.contains(dependencyModule, modifiableModel.getModuleDependencies())) {
    //    modifiableModel.addModuleOrderEntry(dependencyModule);
    //  }
    //}
    return e;
  }

  public ModifiableBuildConfigurationEntry createBcEntry(ModifiableDependencies dependant, String moduleName, String bcName) {
    assertAlive();
    Module dependantModule = getEditor(dependant).myModule;
    return new BuildConfigurationEntryImpl(dependantModule.getProject(), moduleName, bcName);
  }

  public ModifiableModuleLibraryEntry createModuleLibraryEntry(ModifiableDependencies dependant, String dependencyLibraryId) {
    assertAlive();
    ModuleLibraryEntryImpl e = new ModuleLibraryEntryImpl(dependencyLibraryId);

    Module dependantModule = getEditor(dependant).myModule;
    ModifiableRootModel modifiableModel = myProvider.getModuleModifiableModel(dependantModule);
    if (findLibrary(modifiableModel, dependencyLibraryId) == null) {
      LOG.warn("Module library used in build configuration is missing");
    }
    return e;
  }

  public ModifiableDependencyEntry createSharedLibraryEntry(final ModifiableDependencies dependencies,
                                                            final String libraryName,
                                                            final String libraryLevel) {
    assertAlive();
    return new SharedLibraryEntryImpl(libraryName, libraryLevel);
  }


  @Nullable
  private static LibraryEx findLibrary(ModifiableRootModel modifiableModel, String libraryId) {
    for (Library library : modifiableModel.getModuleLibraryTable().getLibraries()) {
      if (((LibraryEx)library).getKind() == FlexLibraryType.FLEX_LIBRARY) { // allow subclasses
        if (libraryId.equals(FlexProjectRootsUtil.getLibraryId(library))) {
          return (LibraryEx)library;
        }
      }
    }
    return null;
  }

  public void setEntries(ModifiableDependencies dependant, List<? extends ModifiableDependencyEntry> newEntries) {
    assertAlive();

    Map<String, ModifiableDependencyEntry> existingModuleLibrariesEntries = new HashMap<>();
    Map<Pair<String, String>, ModifiableBuildConfigurationEntry> existingBcEntries =
      new HashMap<>();
    Map<Pair<String, String>, ModifiableSharedLibraryEntry> existingSharedLibrariesEntries =
      new HashMap<>();

    for (ModifiableDependencyEntry entry : dependant.getModifiableEntries()) {
      if (entry instanceof ModuleLibraryEntry) {
        existingModuleLibrariesEntries.put(((ModuleLibraryEntry)entry).getLibraryId(), entry);
      }
      else if (entry instanceof ModifiableSharedLibraryEntry) {
        final ModifiableSharedLibraryEntry e = (ModifiableSharedLibraryEntry)entry;
        existingSharedLibrariesEntries.put(Pair.create(e.getLibraryLevel(), e.getLibraryName()), e);
      }
      else if (entry instanceof ModifiableBuildConfigurationEntry) {
        final ModifiableBuildConfigurationEntry e = (ModifiableBuildConfigurationEntry)entry;
        existingBcEntries.put(Pair.create(e.getModuleName(), e.getBcName()), e);
      }
      else {
        assert false : entry;
      }
    }

    List<ModifiableDependencyEntry> entriesToRemove = new ArrayList<>(dependant.getModifiableEntries());
    for (Iterator<? extends ModifiableDependencyEntry> i = newEntries.iterator(); i.hasNext(); ) {
      ModifiableDependencyEntry newEntry = i.next();
      ModifiableDependencyEntry existingEntry = null;
      if (newEntry instanceof ModuleLibraryEntry) {
        existingEntry = existingModuleLibrariesEntries.get(((ModuleLibraryEntry)newEntry).getLibraryId());
      }
      else if (newEntry instanceof SharedLibraryEntry e) {
        existingEntry = existingSharedLibrariesEntries.get(Pair.create(e.getLibraryLevel(), e.getLibraryName()));
      }
      else if (newEntry instanceof BuildConfigurationEntry bcEntry) {
        existingEntry = existingBcEntries.get(Pair.create(bcEntry.getModuleName(), bcEntry.getBcName()));
      }
      else {
        assert false : newEntry;
      }
      if (existingEntry != null) {
        entriesToRemove.remove(existingEntry);
        existingEntry.getDependencyType().copyFrom(newEntry.getDependencyType());
        i.remove();
      }
    }

    Editor dependantEditor = getEditor(dependant);
    ModifiableRootModel dependantModifiableModel = myProvider.getModuleModifiableModel(dependantEditor.myModule);

    for (DependencyEntry entry : entriesToRemove) {
      if (entry instanceof ModuleLibraryEntry libraryEntry) {
        Library dependencyLibrary = findLibrary(dependantModifiableModel, libraryEntry.getLibraryId());
        if (dependencyLibrary != null) {
          List<Editor> otherEditors = new ArrayList<>(myModule2Editors.get(dependantEditor.myModule));
          otherEditors.remove(dependantEditor);
          if (!libraryIsUsed(libraryEntry.getLibraryId(), otherEditors)) {
            LibraryOrderEntry orderEntry = dependantModifiableModel.findLibraryOrderEntry(dependencyLibrary);
            LOG.assertTrue(orderEntry != null);
            // TODO should we explicitly delete library as well?
            dependantModifiableModel.removeOrderEntry(orderEntry);
          }
        }
      }
    }
    dependant.getModifiableEntries().removeAll(entriesToRemove);
    dependant.getModifiableEntries().addAll(newEntries);
  }

  private static boolean libraryIsUsed(final String libraryId, final List<? extends Editor> editors) {
    for (Editor editor : editors) {
      for (ModifiableDependencyEntry entry : editor.getDependencies().getModifiableEntries()) {
        if (entry instanceof ModuleLibraryEntry && libraryId.equals(((ModuleLibraryEntry)entry).getLibraryId())) {
          return true;
        }
      }
    }
    return false;
  }

  @Nullable
  protected Module findModuleWithBC(final BuildConfigurationEntry bcEntry) {
    final Module dependencyModule = ContainerUtil.find(myModule2Editors.keySet(),
                                                       module -> bcEntry.getModuleName().equals(module.getName()));

    if (dependencyModule == null) {
      return null;
    }

    final Editor dependencyBC = ContainerUtil.find(myModule2Editors.get(dependencyModule),
                                                   editor -> editor.getName().equals(bcEntry.getBcName()));

    return dependencyBC == null ? null : dependencyModule;
  }

  @Nullable
  public LibraryOrderEntry findLibraryOrderEntry(ModifiableDependencies dependencies, ModuleLibraryEntry moduleLibraryEntry) {
    assertAlive();
    ModifiableRootModel modifiableModel = myProvider.getModuleModifiableModel(getEditor(dependencies).myModule);
    Library library = findLibrary(modifiableModel, moduleLibraryEntry.getLibraryId());
    return library != null ? modifiableModel.findLibraryOrderEntry(library) : null;
  }

  public LibraryOrderEntry findLibraryOrderEntry(ModifiableDependencies dependencies, Library library) {
    ModifiableRootModel modifiableModel = myProvider.getModuleModifiableModel(getEditor(dependencies).myModule);
    return modifiableModel.findLibraryOrderEntry(library);
  }

  private Editor getEditor(ModifiableDependencies dependencies) {
    for (List<Editor> editors : myModule2Editors.values()) {
      for (Editor editor : editors) {
        if (editor.getDependencies() == dependencies) {
          return editor;
        }
      }
    }
    throw new IllegalArgumentException("unknown dependencies instance");
  }

  public Module getModule(ModifiableDependencies dependencies) {
    assertAlive();
    return getEditor(dependencies).myModule;
  }

  public ModifiableRootModel getModifiableRootModel(final Module module) {
    return myProvider.getModuleModifiableModel(module);
  }

  public LibraryTable.ModifiableModel getLibraryModel(ModifiableDependencies dependencies) {
    assertAlive();
    ModifiableRootModel modifiableModel = myProvider.getModuleModifiableModel(getEditor(dependencies).myModule);
    return getTableModifiableModel(modifiableModel);
  }

  private static LibraryTable.ModifiableModel getTableModifiableModel(final ModifiableRootModel modifiableModel) {
    return modifiableModel.getModuleLibraryTable().getModifiableModel();
  }

  public void addSdkListListener(ChangeListener changeListener, Disposable parentDisposable) {
    //mySdksEditor.addSdkListListener(changeListener, parentDisposable);
  }

  @Nullable
  public FlexBuildConfiguration findCurrentConfiguration(final Module module, final String originalBCName) {
    return ContainerUtil.find(myModule2Editors.get(module), editor -> editor.myOrigin.getName().equals(originalBCName));
  }

  public static void makeNonStructuralModification(final FlexBuildConfiguration bc,
                                                   final Consumer<? super NonStructuralModifiableBuildConfiguration> consumer) {
    consumer.consume(new NonStructuralModifiableBuildConfiguration((FlexBuildConfigurationImpl)bc));
  }
}


