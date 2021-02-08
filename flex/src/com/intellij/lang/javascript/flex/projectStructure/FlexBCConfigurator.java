package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.SdkEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.ui.AddBuildConfigurationDialog;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexBCConfigurable;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureDaemonAnalyzer;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.navigation.Place;
import com.intellij.util.EventDispatcher;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FlexBCConfigurator {

  private static final Logger LOG = Logger.getInstance(FlexBCConfigurator.class.getName());

  public interface Listener extends EventListener {
    void moduleRemoved(Module module);

    void buildConfigurationRemoved(FlexBCConfigurable configurable);

    void natureChanged(FlexBCConfigurable configurable);

    void buildConfigurationRenamed(FlexBCConfigurable configurable);
  }

  private final BidirectionalMap<ModifiableFlexBuildConfiguration, CompositeConfigurable> myConfigurablesMap =
    new BidirectionalMap<>();

  private final BidirectionalMap<ModifiableFlexBuildConfiguration, String> myBCToOutputPathMap =
    new BidirectionalMap<>();

  private final EventDispatcher<Listener> myEventDispatcher = EventDispatcher.create(Listener.class);

  // we can have only one Project Structure configuration dialog at a time, so it's OK to hold state for one project
  private final LazyInitializer<Project> myModifiableModelInitializer = new LazyInitializer<>() {
    @Override
    protected void initialize(final Project project) {
      LOG.assertTrue(myConfigEditor == null);
      final ModulesConfigurator configurator = ProjectStructureConfigurable.getInstance(project).getContext().getModulesConfigurator();
      myConfigEditor = new FlexProjectConfigurationEditor(project, new FlexProjectConfigurationEditor.ProjectModifiableModelProvider() {

        @Override
        public Module[] getModules() {
          return configurator.getModuleModel().getModules();
        }

        @Override
        public ModifiableRootModel getModuleModifiableModel(Module module) {
          return configurator.getOrCreateModuleEditor(module).getModifiableRootModelProxy();
        }

        @Override
        public void addListener(Listener listener, Disposable parentDisposable) {
          FlexBCConfigurator.this.addListener(listener, parentDisposable);
        }

        @Override
        public void commitModifiableModels() throws ConfigurationException {
          configurator.apply();
        }

        public LibraryTable.ModifiableModel getLibrariesModifiableModel(@NotNull String level) {
          return ProjectStructureConfigurable.getInstance(project).getContext().createModifiableModelProvider(level).getModifiableModel();
        }

        @Override
        public Library findSourceLibraryForLiveName(final String name, @NotNull final String level) {
          final LibrariesModifiableModel model =
            ProjectStructureConfigurable.getInstance(project).getContext().createModifiableModelProvider(level).getModifiableModel();
          return ContainerUtil.find(model.getLibraries(), library -> name.equals(model.getLibraryEditor(library).getModel().getName()));
        }

        @Override
        public Library findSourceLibrary(final String name, @NotNull final String level) {
          return getLibrariesModifiableModel(level).getLibraryByName(name);
        }
      });
    }

    @Override
    public void doDispose() {
      Disposer.dispose(myConfigEditor);
      myConfigEditor = null;
    }
  };

  private FlexProjectConfigurationEditor myConfigEditor;

  /**
   * @return current editor if Project Structure dialog is open
   */
  @Nullable
  public FlexProjectConfigurationEditor getConfigEditor() {
    return myConfigEditor;
  }

  public void addListener(Listener listener, Disposable parentDisposable) {
    myEventDispatcher.addListener(listener, parentDisposable);
  }

  public void reset(Project project) {
    myModifiableModelInitializer.ensureInitialized(project);
    ModuleStructureConfigurable moduleStructureConfigurable = ProjectStructureConfigurable.getInstance(project).getModulesConfig();
    for (final CompositeConfigurable configurable : myConfigurablesMap.values()) {
      moduleStructureConfigurable.ensureInitialized(configurable);
    }
  }

  public List<CompositeConfigurable> getOrCreateConfigurables(final Module module, final Runnable treeNodeNameUpdater) {
    myModifiableModelInitializer.ensureInitialized(module.getProject());

    final ModifiableFlexBuildConfiguration[] configurations = myConfigEditor.getConfigurations(module);

    List<CompositeConfigurable> configurables = new ArrayList<>(configurations.length);

    for (final ModifiableFlexBuildConfiguration bc : configurations) {
      CompositeConfigurable configurable = myConfigurablesMap.get(bc);
      if (configurable == null) {
        myConfigurablesMap.put(bc, configurable = createBcConfigurable(module, bc, treeNodeNameUpdater));
      }
      configurables.add(configurable);
    }
    return configurables;
  }

  private CompositeConfigurable createBcConfigurable(final Module module,
                                                     final ModifiableFlexBuildConfiguration bc,
                                                     final Runnable treeNodeNameUpdater) {
    final ProjectStructureConfigurable c = ProjectStructureConfigurable.getInstance(module.getProject());
    final Runnable bcNatureModifier = createBCNatureModifier(bc);
    return new FlexBCConfigurable(module, bc, bcNatureModifier, myConfigEditor, c.getProjectJdksModel(), c.getContext()) {
      @Override
      public void apply() throws ConfigurationException {
        super.apply();
        myBCToOutputPathMap.put(bc, bc.getActualOutputFilePath());
      }

      @Override
      public void setDisplayName(final String name) {
        super.setDisplayName(name);
        treeNodeNameUpdater.run();
        myEventDispatcher.getMulticaster().buildConfigurationRenamed(this);
      }
    }.wrapInTabs();
  }

  private Runnable createBCNatureModifier(final ModifiableFlexBuildConfiguration bc) {
    return () -> {
      final CompositeConfigurable compositeConfigurable = myConfigurablesMap.get(bc);
      final BuildConfigurationNature oldNature = bc.getNature();
      final AddBuildConfigurationDialog dialog =
        new AddBuildConfigurationDialog(myConfigEditor.getProject(), FlexBundle.message("change.bc.type.title"),
                                        Collections.emptyList(), oldNature, false);
      dialog.reset(bc.getName(), bc.getAndroidPackagingOptions().isEnabled(), bc.getIosPackagingOptions().isEnabled());
      if (!dialog.showAndGet()) {
        return;
      }

      final BuildConfigurationNature newNature = dialog.getNature();
      if (newNature.equals(oldNature)) {
        if (newNature.isApp() && newNature.isMobilePlatform()) {
          bc.getAndroidPackagingOptions().setEnabled(dialog.isAndroidEnabled());
          bc.getIosPackagingOptions().setEnabled(dialog.isIOSEnabled());
          compositeConfigurable.reset();
        }

        return;
      }

      bc.setNature(newNature);
      fixOutputFileExtension(bc);
      if (newNature.targetPlatform != oldNature.targetPlatform || newNature.outputType != oldNature.outputType) {
        // set package names only if corresponding tabs were not applicable before
        updatePackageFileName(bc, PathUtil.suggestFileName(bc.getName()));
      }

      if (newNature.isApp() && newNature.isMobilePlatform()) {
        bc.getAndroidPackagingOptions().setEnabled(dialog.isAndroidEnabled());
        bc.getIosPackagingOptions().setEnabled(dialog.isIOSEnabled());
      }

      FlexProjectConfigurationEditor.resetNonApplicableValuesToDefaults(bc);

      final FlexBCConfigurable bcConfigurable = FlexBCConfigurable.unwrap(compositeConfigurable);
      bcConfigurable.createChildConfigurables();
      bcConfigurable.updateTabs(compositeConfigurable);
      compositeConfigurable.reset();

      myEventDispatcher.getMulticaster().natureChanged(bcConfigurable);
    };
  }

  private static void fixOutputFileExtension(final ModifiableFlexBuildConfiguration bc) {
    final String outputFileName = bc.getOutputFileName();
    final String lowercase = StringUtil.toLowerCase(outputFileName);
    final String extension = bc.getOutputType() == OutputType.Library ? ".swc" : ".swf";
    if (lowercase.endsWith(".swf") || lowercase.endsWith(".swc")) {
      bc.setOutputFileName(outputFileName.substring(0, outputFileName.length() - ".sw_".length()) + extension);
    }
    else {
      bc.setOutputFileName(outputFileName + extension);
    }
  }

  public void moduleRemoved(final Module module) {
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      return;
    }

    // config editor will handle event and update modifiable model on its own, we just need to update configurables
    Collection<ModifiableFlexBuildConfiguration> configsToRemove =
      ContainerUtil.findAll(myConfigurablesMap.keySet(), bc -> myConfigEditor.getModule(bc) == module);

    final ProjectStructureDaemonAnalyzer daemonAnalyzer =
      ProjectStructureConfigurable.getInstance(myConfigEditor.getProject()).getContext().getDaemonAnalyzer();

    for (ModifiableFlexBuildConfiguration bc : configsToRemove) {
      CompositeConfigurable configurable = myConfigurablesMap.remove(bc);
      myBCToOutputPathMap.remove(bc);
      daemonAnalyzer.removeElement(configurable.getProjectStructureElement());
      daemonAnalyzer.queueUpdateForAllElementsWithErrors();
      configurable.disposeUIResources();
    }

    myEventDispatcher.getMulticaster().moduleRemoved(module);
  }

  public boolean isModified() {
    if (myConfigEditor.isModified()) return true;

    for (final CompositeConfigurable configurable : myConfigurablesMap.values()) {
      if (configurable.isModified()) {
        return true;
      }
    }
    return false;
  }

  public void apply() throws ConfigurationException {
    final ModuleStructureConfigurable c = ProjectStructureConfigurable.getInstance(myConfigEditor.getProject()).getModulesConfig();
    for (final CompositeConfigurable configurable : myConfigurablesMap.values()) {
      c.ensureInitialized(configurable);
      if (configurable.isModified()) {
        configurable.apply();
      }
    }

    if (myConfigEditor.isModified()) {
      myConfigEditor.checkCanCommit();
      myConfigEditor.commit();
    }
  }

  public void afterModelCommit() {
    for (final CompositeConfigurable configurable : myConfigurablesMap.values()) {
      configurable.reset();
    }
  }

  public void dispose() {
    // configurables are disposed by MasterDetailsComponent
    myModifiableModelInitializer.dispose();
    myConfigurablesMap.clear();
    myBCToOutputPathMap.clear();
  }

  public void removeConfiguration(final ModifiableFlexBuildConfiguration bc) {
    CompositeConfigurable configurable = myConfigurablesMap.remove(bc);
    myBCToOutputPathMap.remove(bc);
    final ProjectStructureDaemonAnalyzer daemonAnalyzer =
      ProjectStructureConfigurable.getInstance(myConfigEditor.getProject()).getContext().getDaemonAnalyzer();
    daemonAnalyzer.removeElement(configurable.getProjectStructureElement());
    daemonAnalyzer.queueUpdateForAllElementsWithErrors();

    myEventDispatcher.getMulticaster().buildConfigurationRemoved(FlexBCConfigurable.unwrap(configurable));
  }

  public void addConfiguration(final Module module, final Runnable treeNodeNameUpdater) {
    if (module == null) {
      return;
    }

    final String title = FlexBundle.message("add.build.configuration.title", module.getName());
    final AddBuildConfigurationDialog dialog =
      new AddBuildConfigurationDialog(module.getProject(), title, getUsedNames(module), BuildConfigurationNature.DEFAULT, true);
    if (!dialog.showAndGet()) {
      return;
    }

    final ModifiableFlexBuildConfiguration bc = myConfigEditor.createConfiguration(module);
    final String bcName = dialog.getBCName();
    final String fileName = PathUtil.suggestFileName(bcName);
    final BuildConfigurationNature nature = dialog.getNature();

    bc.setName(bcName);
    bc.setNature(nature);

    final ModifiableFlexBuildConfiguration someExistingConfig = myConfigEditor.getConfigurations(module)[0];
    bc.setOutputFileName(fileName + (bc.getOutputType() == OutputType.Library ? ".swc" : ".swf"));
    bc.setOutputFolder(someExistingConfig.getOutputFolder());
    updatePackageFileName(bc, fileName);

    if (nature.isApp() && nature.isMobilePlatform()) {
      bc.getAndroidPackagingOptions().setEnabled(dialog.isAndroidEnabled());
      bc.getIosPackagingOptions().setEnabled(dialog.isIOSEnabled());
    }

    final SdkEntry sdkEntry = someExistingConfig.getDependencies().getSdkEntry();
    final SdkEntry newSdkEntry;
    if (sdkEntry != null && FlexSdkUtils.findFlexOrFlexmojosSdk(sdkEntry.getName()) != null) {
      newSdkEntry = Factory.createSdkEntry(sdkEntry.getName());
    }
    else {
      newSdkEntry = findAnySdk();
    }
    bc.getDependencies().setSdkEntry(newSdkEntry);

    createConfigurableNode(bc, module, treeNodeNameUpdater);
  }

  @Nullable
  private static SdkEntry findAnySdk() {
    final List<Sdk> sdks = FlexSdkUtils.getFlexSdks();
    return sdks.isEmpty() ? null : Factory.createSdkEntry(sdks.get(0).getName());
  }

  public void copy(final CompositeConfigurable configurable, final Runnable treeNodeNameUpdater) {
    try {
      configurable.apply();
    }
    catch (ConfigurationException ignored) {/**/}

    ModifiableFlexBuildConfiguration existingBC = myConfigurablesMap.getKeysByValue(configurable).get(0);

    FlexBCConfigurable unwrapped = FlexBCConfigurable.unwrap(configurable);
    final String title = FlexBundle.message("copy.build.configuration", existingBC.getName(), unwrapped.getModule().getName());
    Module module = unwrapped.getModule();
    AddBuildConfigurationDialog dialog =
      new AddBuildConfigurationDialog(module.getProject(), title, getUsedNames(module), existingBC.getNature(), true);
    dialog.reset("", existingBC.getAndroidPackagingOptions().isEnabled(), existingBC.getIosPackagingOptions().isEnabled());
    if (!dialog.showAndGet()) {
      return;
    }

    final String newBCName = dialog.getBCName();
    final String fileName = PathUtil.suggestFileName(newBCName);
    final BuildConfigurationNature newNature = dialog.getNature();

    ModifiableFlexBuildConfiguration newBC = myConfigEditor.copyConfiguration(existingBC, newNature);
    newBC.setName(newBCName);

    newBC.setOutputFileName(fileName + (newBC.getOutputType() == OutputType.Library ? ".swc" : ".swf"));
    updatePackageFileName(newBC, fileName);

    if (newNature.isApp() && newNature.isMobilePlatform()) {
      newBC.getAndroidPackagingOptions().setEnabled(dialog.isAndroidEnabled());
      newBC.getIosPackagingOptions().setEnabled(dialog.isIOSEnabled());
    }

    createConfigurableNode(newBC, unwrapped.getModule(), treeNodeNameUpdater);
  }

  private static void updatePackageFileName(final ModifiableFlexBuildConfiguration bc, final String packageFileName) {
    final BuildConfigurationNature nature = bc.getNature();
    if (nature.isApp()) {
      if (nature.isDesktopPlatform()) {
        bc.getAirDesktopPackagingOptions().setPackageFileName(packageFileName);
      }
      else if (nature.isMobilePlatform()) {
        bc.getAndroidPackagingOptions().setPackageFileName(packageFileName);
        bc.getIosPackagingOptions().setPackageFileName(packageFileName);
      }
    }
  }


  private void createConfigurableNode(ModifiableFlexBuildConfiguration bc, Module module, Runnable treeNodeNameUpdater) {
    CompositeConfigurable wrapped = createBcConfigurable(module, bc, treeNodeNameUpdater);
    myConfigurablesMap.put(bc, wrapped);
    final MasterDetailsComponent.MyNode node = new BuildConfigurationNode(wrapped);

    final ModuleStructureConfigurable moduleStructureConfigurable = ProjectStructureConfigurable.getInstance(module.getProject()).getModulesConfig();
    moduleStructureConfigurable.addNode(node, moduleStructureConfigurable.findModuleNode(module));

    Place place = new Place().putPath(ProjectStructureConfigurable.CATEGORY, moduleStructureConfigurable)
      .putPath(MasterDetailsComponent.TREE_OBJECT, bc);
    ProjectStructureConfigurable.getInstance(module.getProject()).navigateTo(place, true);
  }

  private Collection<String> getUsedNames(final Module module) {
    final Collection<String> result = new LinkedList<>();
    for (final ModifiableFlexBuildConfiguration configuration : myConfigEditor.getConfigurations(module)) {
      result.add(myConfigurablesMap.get(configuration).getDisplayName());
    }
    return result;
  }

  public List<CompositeConfigurable> getBCConfigurables(@NotNull Module module) {
    return ContainerUtil.map(myConfigEditor.getConfigurations(module),
                             configuration -> myConfigurablesMap.get(configuration));
  }

  public FlexBCConfigurable getBCConfigurable(@NotNull ModifiableFlexBuildConfiguration bc) {
    return FlexBCConfigurable.unwrap(myConfigurablesMap.get(bc));
  }

  @Nullable
  public List<ModifiableFlexBuildConfiguration> getBCsByOutputPath(final String outputPath) {
    return myBCToOutputPathMap.getKeysByValue(outputPath);
  }

  public Place getPlaceFor(final Module module, final String bcName) {
    Place p = new Place();
    p = p.putPath(ProjectStructureConfigurable.CATEGORY, ProjectStructureConfigurable.getInstance(myConfigEditor.getProject()).getModulesConfig());
    p = p.putPath(MasterDetailsComponent.TREE_OBJECT, myConfigEditor.findCurrentConfiguration(module, bcName));
    return p;
  }

  public boolean canBeRemoved(ModifiableFlexBuildConfiguration[] configurations) {
    Map<Module, Integer> module2ConfigCount = new HashMap<>();
    for (ModifiableFlexBuildConfiguration bc : configurations) {
      Module module = myConfigEditor.getModule(bc);
      Integer count = module2ConfigCount.get(module);
      module2ConfigCount.put(module, count != null ? count + 1 : 1);
    }

    for (Map.Entry<Module, Integer> entry : module2ConfigCount.entrySet()) {
      Module module = entry.getKey();
      if (myConfigEditor.getConfigurations(module).length == entry.getValue()) {
        return false;
      }
    }
    return true;
  }
}
