package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.SdkEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.projectStructure.ui.AddBuildConfigurationDialog;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexIdeBCConfigurable;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesModifiableModel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.navigation.Place;
import com.intellij.util.EventDispatcher;
import com.intellij.util.Function;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FlexIdeBCConfigurator {

  private static final Logger LOG = Logger.getInstance(FlexIdeBCConfigurator.class.getName());

  public interface Listener extends EventListener {
    void moduleRemoved(Module module);

    void buildConfigurationRemoved(FlexIdeBCConfigurable configurable);
  }

  private final BidirectionalMap<ModifiableFlexIdeBuildConfiguration, CompositeConfigurable> myConfigurablesMap =
    new BidirectionalMap<ModifiableFlexIdeBuildConfiguration, CompositeConfigurable>();

  private final EventDispatcher<Listener> myEventDispatcher = EventDispatcher.create(Listener.class);

  // we can have only one Project Structure configuration dialog at a time, so it's OK to hold state for one project
  private final LazyInitializer<Project> myModifiableModelInitializer = new LazyInitializer<Project>() {
    @Override
    protected void initialize(final Project project) {
      LOG.assertTrue(myConfigEditor == null);
      final ModulesConfigurator configurator = ModuleStructureConfigurable.getInstance(project).getContext().getModulesConfigurator();
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
          FlexIdeBCConfigurator.this.addListener(listener, parentDisposable);
        }

        @Override
        public void commitModifiableModels() throws ConfigurationException {
          configurator.apply();
        }

        public LibraryTableBase.ModifiableModelEx getLibrariesModifiableModel(final String level) {
          return ProjectStructureConfigurable.getInstance(project).getContext().createModifiableModelProvider(level).getModifiableModel();
        }

        public Sdk[] getAllSdks() {
          return FlexSdkUtils.getAllSdks();
        }

        public Library findSourceLibraryForLiveName(final String name, final String level) {
          final LibrariesModifiableModel model =
            ProjectStructureConfigurable.getInstance(project).getContext().createModifiableModelProvider(level).getModifiableModel();
          return ContainerUtil.find(model.getLibraries(), new Condition<Library>() {
            public boolean value(final Library library) {
              return name.equals(model.getLibraryEditor(library).getModel().getName());
            }
          });
        }

        public Library findSourceLibrary(final String name, final String level) {
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
    ModuleStructureConfigurable moduleStructureConfigurable = ModuleStructureConfigurable.getInstance(project);
    for (final CompositeConfigurable configurable : myConfigurablesMap.values()) {
      moduleStructureConfigurable.ensureInitialized(configurable);
    }
  }

  public List<CompositeConfigurable> getOrCreateConfigurables(final Module module, final Runnable treeNodeNameUpdater) {
    myModifiableModelInitializer.ensureInitialized(module.getProject());

    final ModifiableFlexIdeBuildConfiguration[] configurations = myConfigEditor.getConfigurations(module);

    List<CompositeConfigurable> configurables = new ArrayList<CompositeConfigurable>(configurations.length);

    for (final ModifiableFlexIdeBuildConfiguration configuration : configurations) {
      CompositeConfigurable configurable = myConfigurablesMap.get(configuration);
      if (configurable == null) {
        final ProjectStructureConfigurable c = ProjectStructureConfigurable.getInstance(myConfigEditor.getProject());
        configurable =
          new FlexIdeBCConfigurable(module, configuration, treeNodeNameUpdater, myConfigEditor, c.getProjectJdksModel()).wrapInTabs();
        myConfigurablesMap.put(configuration, configurable);
      }
      configurables.add(configurable);
    }
    return configurables;
  }

  public void moduleRemoved(final Module module) {
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      return;
    }

    // config editor will handle event and update modifiable model on its own, we just need to update configurables
    Collection<ModifiableFlexIdeBuildConfiguration> configsToRemove =
      ContainerUtil.findAll(myConfigurablesMap.keySet(), new Condition<ModifiableFlexIdeBuildConfiguration>() {
        @Override
        public boolean value(ModifiableFlexIdeBuildConfiguration bc) {
          return myConfigEditor.getModule(bc) == module;
        }
      });

    for (ModifiableFlexIdeBuildConfiguration bc : configsToRemove) {
      CompositeConfigurable configurable = myConfigurablesMap.remove(bc);
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

  public void dispose() {
    // configurables are disposed by MasterDetailsComponent
    myModifiableModelInitializer.dispose();
    myConfigurablesMap.clear();
  }

  public void removeConfiguration(final ModifiableFlexIdeBuildConfiguration configuration) {
    CompositeConfigurable configurable = myConfigurablesMap.remove(configuration);
    myEventDispatcher.getMulticaster().buildConfigurationRemoved(FlexIdeBCConfigurable.unwrap(configurable));
  }

  public void addConfiguration(final Module module, final Runnable treeNodeNameUpdater) {
    if (module == null) {
      return;
    }

    Pair<String, BuildConfigurationNature> nameAndNature =
      promptForCreation(module, FlexBundle.message("add.build.configuration.title", module.getName()), BuildConfigurationNature.DEFAULT);
    if (nameAndNature == null) {
      return;
    }

    final ModifiableFlexIdeBuildConfiguration configuration = myConfigEditor.createConfiguration(module);
    configuration.setName(nameAndNature.first);
    configuration.setNature(nameAndNature.second);

    final ModifiableFlexIdeBuildConfiguration someExistingConfig = myConfigEditor.getConfigurations(module)[0];
    final FlexIdeBCConfigurable configurable = FlexIdeBCConfigurable.unwrap(myConfigurablesMap.get(someExistingConfig));
    try {
      configurable.apply();
    }
    catch (ConfigurationException ignored) {/**/}

    // may be also set main class, package file names?
    configuration.setOutputFileName(nameAndNature.first + (configuration.getOutputType() == OutputType.Library ? ".swc" : ".swf"));
    configuration.setOutputFolder(someExistingConfig.getOutputFolder());

    final SdkEntry sdkEntry = someExistingConfig.getDependencies().getSdkEntry();
    final SdkEntry newSdkEntry = sdkEntry == null ? findAnySdk() : Factory.createSdkEntry(sdkEntry.getName());
    configuration.getDependencies().setSdkEntry(newSdkEntry);

    createConfigurableNode(configuration, module, treeNodeNameUpdater);
  }

  @Nullable
  private SdkEntry findAnySdk() {
    final Sdk sdk = myConfigEditor.getAnyFlexSdk();
    return sdk != null ? Factory.createSdkEntry(sdk.getName()) : null;
  }

  public void copy(final CompositeConfigurable configurable, final Runnable treeNodeNameUpdater) {
    try {
      configurable.apply();
    }
    catch (ConfigurationException ignored) {/**/}

    ModifiableFlexIdeBuildConfiguration configuration = myConfigurablesMap.getKeysByValue(configurable).get(0);

    FlexIdeBCConfigurable unwrapped = FlexIdeBCConfigurable.unwrap(configurable);
    final String title = FlexBundle.message("copy.build.configuration", unwrapped.getModule().getName());
    Pair<String, BuildConfigurationNature> nameAndNature = promptForCreation(unwrapped.getModule(), title, configuration.getNature());
    if (nameAndNature == null) {
      return;
    }

    ModifiableFlexIdeBuildConfiguration newConfiguration = myConfigEditor.copyConfiguration(configuration, nameAndNature.second);
    newConfiguration.setName(nameAndNature.first);

    // set correct output file extension for cloned configuration
    final String outputFileName = configuration.getOutputFileName();
    final String lowercase = outputFileName.toLowerCase();
    if (lowercase.endsWith(".swf") || lowercase.endsWith(".swc")) {
      final String extension = newConfiguration.getOutputType() == OutputType.Library ? ".swc" : ".swf";
      newConfiguration.setOutputFileName(outputFileName.substring(0, outputFileName.length() - ".sw_".length()) + extension);
    }

    createConfigurableNode(newConfiguration, unwrapped.getModule(), treeNodeNameUpdater);
  }

  @Nullable
  private Pair<String, BuildConfigurationNature> promptForCreation(Module module,
                                                                   String dialogTitle,
                                                                   BuildConfigurationNature defaultNature) {
    Project project = module.getProject();
    AddBuildConfigurationDialog dialog = new AddBuildConfigurationDialog(project, dialogTitle, getUsedNames(module), defaultNature);
    dialog.show();
    return dialog.isOK() ? Pair.create(dialog.getName(), dialog.getNature()) : null;
  }

  private void createConfigurableNode(ModifiableFlexIdeBuildConfiguration configuration, Module module, Runnable treeNodeNameUpdater) {
    final ProjectSdksModel sdksModel =
      ProjectStructureConfigurable.getInstance(myConfigEditor.getProject()).getProjectJdksModel();
    final FlexIdeBCConfigurable configurable =
      new FlexIdeBCConfigurable(module, configuration, treeNodeNameUpdater, myConfigEditor, sdksModel);

    CompositeConfigurable wrapped = configurable.wrapInTabs();
    myConfigurablesMap.put(configuration, wrapped);
    final MasterDetailsComponent.MyNode node = new BuildConfigurationNode(wrapped);

    final ModuleStructureConfigurable moduleStructureConfigurable = ModuleStructureConfigurable.getInstance(module.getProject());
    moduleStructureConfigurable.addNode(node, moduleStructureConfigurable.findModuleNode(module));

    Place place = new Place().putPath(ProjectStructureConfigurable.CATEGORY, moduleStructureConfigurable)
      .putPath(MasterDetailsComponent.TREE_OBJECT, configuration);
    ProjectStructureConfigurable.getInstance(module.getProject()).navigateTo(place, true);
  }

  private Collection<String> getUsedNames(final Module module) {
    final Collection<String> result = new LinkedList<String>();
    for (final ModifiableFlexIdeBuildConfiguration configuration : myConfigEditor.getConfigurations(module)) {
      result.add(myConfigurablesMap.get(configuration).getDisplayName());
    }
    return result;
  }

  public List<CompositeConfigurable> getBCConfigurables(@NotNull Module module) {
    return ContainerUtil.map(myConfigEditor.getConfigurations(module),
                             new Function<ModifiableFlexIdeBuildConfiguration, CompositeConfigurable>() {
                               @Override
                               public CompositeConfigurable fun(ModifiableFlexIdeBuildConfiguration configuration) {
                                 return myConfigurablesMap.get(configuration);
                               }
                             });
  }

  public Place getPlaceFor(Module module, FlexIdeBuildConfiguration origin) {
    Place p = new Place();
    p = p.putPath(ProjectStructureConfigurable.CATEGORY, ModuleStructureConfigurable.getInstance(myConfigEditor.getProject()));
    p = p.putPath(MasterDetailsComponent.TREE_OBJECT, myConfigEditor.findCurrentConfiguration(module, origin));
    return p;
  }

  public boolean canBeRemoved(ModifiableFlexIdeBuildConfiguration[] configurations) {
    Map<Module, Integer> module2ConfigCount = new HashMap<Module, Integer>();
    for (ModifiableFlexIdeBuildConfiguration bc : configurations) {
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
