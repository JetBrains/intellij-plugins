package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdkProperties;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexIdeBCConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexSdkModificator;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EventDispatcher;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ChangeListener;
import java.util.*;

/**
 * User: ksafonov
 */
public class FlexProjectConfigurationEditor implements Disposable {

  private static final Logger LOG = Logger.getInstance(FlexProjectConfigurationEditor.class.getName());

  private static class Editor extends FlexIdeBuildConfigurationImpl {
    private final Module myModule;
    private final FlexIdeBuildConfigurationImpl myOrigin;

    Editor(FlexIdeBuildConfigurationImpl origin, Module module) {
      myOrigin = origin;
      myModule = module;
      origin.applyTo(this);
    }

    public FlexIdeBuildConfigurationImpl commit() {
      applyTo(myOrigin);
      return myOrigin;
    }

    public boolean isModified() {
      return !isEqual(myOrigin);
    }
  }

  public interface ProjectModifiableModelProvider {
    Module[] getModules();

    ModifiableRootModel getModuleModifiableModel(Module module);

    void addListener(FlexIdeBCConfigurator.Listener listener, Disposable parentDisposable);

    void commitModifiableModels() throws ConfigurationException;

    LibraryTableBase.ModifiableModelEx getGlobalLibrariesModifiableModel();
  }

  public interface ModulesModelChangeListener extends EventListener {
    void modulesModelsChanged(Collection<Module> modules);
  }
  
  private boolean myDisposed;
  private final ProjectModifiableModelProvider myProvider;

  @Nullable
  private final Project myProject;

  private final Map<Module, List<Editor>> myModule2Editors = new HashMap<Module, List<Editor>>();
  private final FlexSdksEditor mySdksEditor;
  private final EventDispatcher<ModulesModelChangeListener> myModulesModelChangeEventDispatcher =
    EventDispatcher.create(ModulesModelChangeListener.class);

  public FlexProjectConfigurationEditor(@Nullable Project project, ProjectModifiableModelProvider provider) {
    myProject = project;
    myProvider = provider;
    mySdksEditor = new FlexSdksEditor(this);

    provider.addListener(new FlexIdeBCConfigurator.Listener() {
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
      public void buildConfigurationRemoved(FlexIdeBCConfigurable configurable) {
        assertAlive();
        Editor editor = (Editor)configurable.getEditableObject();
        List<Editor> editors = myModule2Editors.get(editor.myModule);
        boolean contained = editors.remove(editor);
        LOG.assertTrue(contained);
      }
    }, this);

    for (Module module : provider.getModules()) {
      if (!isFlex(module)) {
        continue;
      }
      addEditorsForModule(module);
    }
  }

  public void addModulesModelChangeListener(ModulesModelChangeListener listener, Disposable parentDisposable) {
    myModulesModelChangeEventDispatcher.addListener(listener, parentDisposable);
  }

  private void addEditorsForModule(Module module) {
    FlexIdeBuildConfiguration[] buildConfigurations = FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations();
    List<Editor> configEditors = new ArrayList<Editor>(buildConfigurations.length);
    for (FlexIdeBuildConfiguration buildConfiguration : buildConfigurations) {
      configEditors.add(new Editor((FlexIdeBuildConfigurationImpl)buildConfiguration, module));
    }
    myModule2Editors.put(module, configEditors);
  }

  @Override
  public void dispose() {
    myDisposed = true;
    myModule2Editors.clear();
  }

  public ModifiableFlexIdeBuildConfiguration[] getConfigurations(Module module) {
    assertAlive();
    List<Editor> editors = myModule2Editors.get(module);
    if (editors == null) {
      // module was just created
      addEditorsForModule(module);
      editors = myModule2Editors.get(module);
    }
    return editors.toArray(new ModifiableFlexIdeBuildConfiguration[editors.size()]);
  }

  private void assertAlive() {
    LOG.assertTrue(!myDisposed, "Already disposed");
  }

  public ModifiableFlexIdeBuildConfiguration createConfiguration(Module module) {
    assertAlive();
    List<Editor> editors = myModule2Editors.get(module);
    Editor newConfig = new Editor(new FlexIdeBuildConfigurationImpl(), module);
    editors.add(newConfig);
    return newConfig;
  }

  public ModifiableFlexIdeBuildConfiguration copyConfiguration(ModifiableFlexIdeBuildConfiguration configuration,
                                                               BuildConfigurationNature newNature) {
    assertAlive();
    Module module = ((Editor)configuration).myModule;
    List<Editor> editors = myModule2Editors.get(module);
    FlexIdeBuildConfigurationImpl copy = ((Editor)configuration).getCopy();
    Editor newConfig = new Editor(copy, module);
    newConfig.setNature(newNature);
    // just to simplify serialized view
    resetNonApplicableValuesToDefaults(newConfig);
    editors.add(newConfig);
    return newConfig;
  }

  private static void resetNonApplicableValuesToDefaults(final ModifiableFlexIdeBuildConfiguration configuration) {
    final FlexIdeBuildConfiguration defaultConfiguration = new FlexIdeBuildConfigurationImpl();

    if (configuration.getOutputType() != OutputType.RuntimeLoadedModule) {
      configuration.setOptimizeFor(defaultConfiguration.getOptimizeFor());
    }

    if (configuration.getOutputType() == OutputType.Library) {
      configuration.setMainClass(defaultConfiguration.getMainClass());
    }

    if (configuration.getTargetPlatform() != TargetPlatform.Web || configuration.getOutputType() != OutputType.Application) {
      configuration.setUseHtmlWrapper(defaultConfiguration.isUseHtmlWrapper());
      configuration.setWrapperTemplatePath(defaultConfiguration.getWrapperTemplatePath());
    }

    if (configuration.getTargetPlatform() != TargetPlatform.Web) {
      configuration.getDependencies().setTargetPlayer(defaultConfiguration.getDependencies().getTargetPlayer());
    }

    if (configuration.getTargetPlatform() == TargetPlatform.Mobile || configuration.isPureAs()) {
      configuration.getDependencies().setComponentSet(defaultConfiguration.getDependencies().getComponentSet());
    }

    BuildConfigurationNature nature = configuration.getNature();
    for (Iterator<ModifiableDependencyEntry> i = configuration.getDependencies().getModifiableEntries().iterator(); i.hasNext(); ) {
      if (!BCUtils.isApplicable(nature, i.next().getDependencyType().getLinkageType())) {
        i.remove();
      }
    }

    if (configuration.getTargetPlatform() != TargetPlatform.Desktop || configuration.getOutputType() != OutputType.Application) {
      ((AirDesktopPackagingOptionsImpl)defaultConfiguration.getAirDesktopPackagingOptions())
        .applyTo(((AirDesktopPackagingOptionsImpl)configuration.getAirDesktopPackagingOptions()));
    }

    if (configuration.getTargetPlatform() != TargetPlatform.Mobile || configuration.getOutputType() != OutputType.Application) {
      ((AndroidPackagingOptionsImpl)defaultConfiguration.getAndroidPackagingOptions())
        .applyTo(((AndroidPackagingOptionsImpl)configuration.getAndroidPackagingOptions()));
      ((IosPackagingOptionsImpl)defaultConfiguration.getIosPackagingOptions())
        .applyTo(((IosPackagingOptionsImpl)configuration.getIosPackagingOptions()));
    }
  }

  public Module getModule(ModifiableFlexIdeBuildConfiguration configuration) {
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
      Set<String> names = new HashSet<String>();
      for (Editor editor : editors) {
        if (!names.add(editor.getName())) {
          throw new ConfigurationException(
            "Duplicate build configuration name '" + editor.getName() + "' in module '" + module.getName() + "'");
        }
      }
    }
  }

  public void commit() throws ConfigurationException {
    for (Module module : myModule2Editors.keySet()) {
      ModifiableRootModel modifiableModel = myProvider.getModuleModifiableModel(module);

      // ---------------- SDK entries ----------------------
      Map<LibraryEx, Boolean> sdksToAdd = new HashMap<LibraryEx, Boolean>(); // Library -> add_library_entry_flag
      for (Editor editor : myModule2Editors.get(module)) {
        SdkEntry sdkEntry = editor.getDependencies().getSdkEntry();
        if (sdkEntry != null) {
          LibraryEx sdkLibrary = ((SdkEntryImpl)sdkEntry).findLibrary(myProvider.getGlobalLibrariesModifiableModel().getLibraries());
          if (sdkLibrary != null) {
            sdksToAdd.put(sdkLibrary, true);
          }
        }
      }

      Collection<OrderEntry> sdkEntriesToRemove = new ArrayList<OrderEntry>();
      for (OrderEntry orderEntry : modifiableModel.getOrderEntries()) {
        if (orderEntry instanceof LibraryOrderEntry) {
          if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel())) {
            LibraryEx sdkLibrary = (LibraryEx)((LibraryOrderEntry)orderEntry).getLibrary();
            if (sdksToAdd.containsKey(sdkLibrary)) {
              sdksToAdd.put(sdkLibrary, false);
            }
            else if (sdkLibrary != null && sdkLibrary.getType() instanceof FlexSdkLibraryType) {
              sdkEntriesToRemove.add(orderEntry);
            }
          }
        }
      }

      for (OrderEntry e : sdkEntriesToRemove) {
        modifiableModel.removeOrderEntry(e);
      }

      for (LibraryEx library : sdksToAdd.keySet()) {
        if (!library.isDisposed() &&
            sdksToAdd.get(library) &&
            myProvider.getGlobalLibrariesModifiableModel().getLibraryByName(library.getName()) != null) {
          modifiableModel.addLibraryEntry(library);
        }
      }

      // ---------------- modules entries ----------------------
      final Map<Module, Boolean> modulesToAdd = new HashMap<Module, Boolean>(); // Library -> add_module_entry_flag
      for (Editor editor : myModule2Editors.get(module)) {
        for (DependencyEntry dependencyEntry : editor.getDependencies().getEntries()) {
          if (dependencyEntry instanceof BuildConfigurationEntry) {
            Editor bc = findBc((BuildConfigurationEntry)dependencyEntry);
            if (bc != null && bc.myModule != module) {
              modulesToAdd.put(bc.myModule, true);
            }
          }
        }
      }

      List<OrderEntry> moduleOrderEntriesToRemove = ContainerUtil.filter(modifiableModel.getOrderEntries(), new Condition<OrderEntry>() {
        @Override
        public boolean value(OrderEntry orderEntry) {
          if (orderEntry instanceof ModuleOrderEntry) {
            Module m = ((ModuleOrderEntry)orderEntry).getModule();
            if (modulesToAdd.containsKey(m)) {
              modulesToAdd.put(m, false);
              return false;
            }
            else {
              return true;
            }
          }
          return false;
        }
      });

      for (OrderEntry orderEntry : moduleOrderEntriesToRemove) {
        modifiableModel.removeOrderEntry(orderEntry);
      }
      for (Module m : modulesToAdd.keySet()) {
        if (modulesToAdd.get(m)) {
          modifiableModel.addModuleOrderEntry(m);
        }
      }
    }

    // ---------------- do commit ----------------------
    Collection<Module> modulesWithChangedModifiableModel = ContainerUtil.findAll(myModule2Editors.keySet(), new Condition<Module>() {
      @Override
      public boolean value(Module module) {
        return myProvider.getModuleModifiableModel(module).isChanged();
      }
    });

    if (!modulesWithChangedModifiableModel.isEmpty()) {
      myProvider.commitModifiableModels();
      myModulesModelChangeEventDispatcher.getMulticaster().modulesModelsChanged(modulesWithChangedModifiableModel);
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        for (Module module : myModule2Editors.keySet()) {
          Function<Editor, FlexIdeBuildConfigurationImpl> f = new Function<Editor, FlexIdeBuildConfigurationImpl>() {
            @Override
            public FlexIdeBuildConfigurationImpl fun(Editor editor) {
              return editor.commit();
            }
          };
          FlexIdeBuildConfigurationImpl[] current =
            ContainerUtil.map2Array(myModule2Editors.get(module), FlexIdeBuildConfigurationImpl.class, f);
          ((FlexBuildConfigurationManagerImpl)FlexBuildConfigurationManager.getInstance(module)).setBuildConfigurations(current);
        }

        if (mySdksEditor.isModified()) {
          mySdksEditor.commit();
        }

        if (myProject != null) {
          FlexBuildConfigurationManagerImpl.resetHighlighting(myProject);
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

      FlexIdeBuildConfiguration[] originalConfigurations = FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations();
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

    if (mySdksEditor.isModified()) {
      return true;
    }
    return false;
  }

  private static boolean isFlex(Module module) {
    return ModuleType.get(module) == FlexModuleType.getInstance();
  }

  public ModifiableBuildConfigurationEntry createBcEntry(ModifiableDependencies dependant,
                                                         ModifiableFlexIdeBuildConfiguration dependency,
                                                         @Nullable String currentName) {
    assertAlive();
    Module dependencyModule = ((Editor)dependency).myModule;
    ModifiableBuildConfigurationEntry e =
      new BuildConfigurationEntryImpl(dependencyModule, currentName != null ? currentName : dependency.getName());

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
    LOG.assertTrue(findLibrary(modifiableModel, dependencyLibraryId) != null);
    return e;
  }

  @Nullable
  private static Library findLibrary(ModifiableRootModel modifiableModel, String libraryId) {
    for (Library library : modifiableModel.getModuleLibraryTable().getLibraries()) {
      if (((LibraryEx)library).getType() instanceof FlexLibraryType) { // allow subclasses
        if (libraryId.equals(FlexProjectRootsUtil.getLibraryId(library))) {
          return library;
        }
      }
    }
    return null;
  }

  public void setEntries(ModifiableDependencies dependant, List<? extends ModifiableDependencyEntry> newEntries) {
    assertAlive();

    Map<String, ModifiableDependencyEntry> moduleLibrariesEntries = new HashMap<String, ModifiableDependencyEntry>();
    for (ModifiableDependencyEntry entry : dependant.getModifiableEntries()) {
      if (entry instanceof ModuleLibraryEntry) {
        moduleLibrariesEntries.put(((ModuleLibraryEntry)entry).getLibraryId(), entry);
      }
    }

    List<ModifiableDependencyEntry> entriesToRemove = new ArrayList<ModifiableDependencyEntry>(dependant.getModifiableEntries());
    for (Iterator<? extends ModifiableDependencyEntry> i = newEntries.iterator(); i.hasNext(); ) {
      ModifiableDependencyEntry entry = i.next();
      if (entry instanceof ModuleLibraryEntry) {
        ModifiableDependencyEntry existingEntry = moduleLibrariesEntries.get(((ModuleLibraryEntry)entry).getLibraryId());
        if (existingEntry != null) {
          entriesToRemove.remove(existingEntry);
          existingEntry.getDependencyType().copyFrom(entry.getDependencyType());
          i.remove();
        }
      }
    }

    Editor dependantEditor = getEditor(dependant);
    ModifiableRootModel dependantModifiableModel = myProvider.getModuleModifiableModel(dependantEditor.myModule);

    for (DependencyEntry entry : entriesToRemove) {
      if (entry instanceof ModuleLibraryEntry) {
        ModuleLibraryEntry libraryEntry = (ModuleLibraryEntry)entry;
        Library dependencyLibrary = findLibrary(dependantModifiableModel, libraryEntry.getLibraryId());
        if (dependencyLibrary != null) {
          LibraryOrderEntry orderEntry = dependantModifiableModel.findLibraryOrderEntry(dependencyLibrary);
          LOG.assertTrue(orderEntry != null);
          // TODO should we explicitly delete library?
          dependantModifiableModel.removeOrderEntry(orderEntry);
        }
      }
    }
    dependant.getModifiableEntries().removeAll(entriesToRemove);
    dependant.getModifiableEntries().addAll(newEntries);
  }


  @Nullable
  private Editor findBc(final BuildConfigurationEntry bcEntry) {
    Module dependencyModule = ContainerUtil.find(myModule2Editors.keySet(), new Condition<Module>() {
      @Override
      public boolean value(Module module) {
        return bcEntry.getModuleName().equals(module.getName());
      }
    });

    if (dependencyModule == null) {
      return null;
    }

    return ContainerUtil.find(myModule2Editors.get(dependencyModule), new Condition<Editor>() {
      @Override
      public boolean value(Editor editor) {
        return editor.getName().equals(bcEntry.getBcName());
      }
    });
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

  public LibraryTableBase.ModifiableModelEx getLibraryModel(ModifiableDependencies dependencies) {
    assertAlive();
    ModifiableRootModel modifiableModel = myProvider.getModuleModifiableModel(getEditor(dependencies).myModule);
    return (LibraryTableBase.ModifiableModelEx)modifiableModel.getModuleLibraryTable().getModifiableModel();
  }

  public LibraryEx[] getSdksLibraries() {
    return ContainerUtil.mapNotNull(myProvider.getGlobalLibrariesModifiableModel().getLibraries(), new Function<Library, LibraryEx>() {
      @Override
      public LibraryEx fun(Library library) {
        return FlexSdk.isFlexSdk(library) ? (LibraryEx)library : null;
      }
    }, new LibraryEx[0]);
  }

  @Nullable
  public FlexSdk findSdk(String libraryId) {
    return mySdksEditor.findSdk(libraryId);
  }

  public void addSdkListListener(ChangeListener changeListener, Disposable parentDisposable) {
    mySdksEditor.addSdkListListener(changeListener, parentDisposable);
  }

  @NotNull
  public FlexSdk findOrCreateSdk(@NotNull String homePath) {
    return mySdksEditor.findOrCreateSdk(homePath);
  }

  public void setSdkLibraryUsed(Object user, @Nullable LibraryEx sdk) {
    mySdksEditor.setUsed(user, sdk);
  }

  public LibraryEditor getSdkLibraryEditor(Library library) {
    return ((LibrariesModifiableModel)myProvider.getGlobalLibrariesModifiableModel()).getLibraryEditor(library);
  }

  public void removeFlexSdkLibrary(Library library) {
    myProvider.getGlobalLibrariesModifiableModel().removeLibrary(library);
  }

  public LibraryEx createFlexSdkLibrary(String homePath) {
    final VirtualFile sdkHome = LocalFileSystem.getInstance().findFileByPath(homePath);

    LibraryTableBase.ModifiableModelEx model = myProvider.getGlobalLibrariesModifiableModel();
    Set<String> existingNames = ContainerUtil.map2Set(model.getLibraries(), new Function<Library, String>() {
      @Override
      public String fun(Library library) {
        return library.getName();
      }
    });

    LibraryEx library = (LibraryEx)model.createLibrary("Flex SDK", FlexSdkLibraryType.getInstance());
    LibraryEx.ModifiableModelEx libraryModifiableModel = (LibraryEx.ModifiableModelEx)library.getModifiableModel();
    ((FlexSdkProperties)libraryModifiableModel.getProperties()).setId(FlexLibraryIdGenerator.generateId());
    ((FlexSdkProperties)libraryModifiableModel.getProperties()).setHomePath(homePath);
    final FlexSdkModificator sdkModificator = new FlexSdkModificator(libraryModifiableModel, existingNames);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        FlexSdkUtils.setupSdkPaths(sdkHome, null, sdkModificator);
        sdkModificator.commitChanges();
      }
    });
    return library;
  }

  @Nullable
  public FlexIdeBuildConfiguration findCurrentConfiguration(Module module, final FlexIdeBuildConfiguration origin) {
    return ContainerUtil.find(myModule2Editors.get(module), new Condition<Editor>() {
      @Override
      public boolean value(Editor editor) {
        return editor.myOrigin == origin;
      }
    });
  }
}


