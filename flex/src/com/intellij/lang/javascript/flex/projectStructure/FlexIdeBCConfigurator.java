package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.SdkEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.*;
import com.intellij.lang.javascript.flex.projectStructure.ui.AddBuildConfigurationDialog;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexIdeBCConfigurable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.ui.navigation.Place;
import com.intellij.util.EventDispatcher;
import com.intellij.util.Function;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.containers.ContainerUtil;
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

        @Override
        public LibraryTableBase.ModifiableModelEx getGlobalLibrariesModifiableModel() {
          return (LibraryTableBase.ModifiableModelEx)ProjectStructureConfigurable.getInstance(project).getContext()
            .getModifiableLibraryTable(ApplicationLibraryTable.getApplicationTable());
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

  public void addListener(Listener listener, Disposable parentDisposable) {
    myEventDispatcher.addListener(listener, parentDisposable);
  }

  public void reset(Project project) {
    myModifiableModelInitializer.ensureInitialized(project);
    ModuleStructureConfigurable moduleStructureConfigurable = ModuleStructureConfigurable.getInstance(project);
    for (final NamedConfigurable<ModifiableFlexIdeBuildConfiguration> configurable : myConfigurablesMap.values()) {
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
        configurable = new FlexIdeBCConfigurable(module, configuration, treeNodeNameUpdater, myConfigEditor).wrapInTabs();
        myConfigurablesMap.put(configuration, configurable);
      }
      configurables.add(configurable);
    }
    return configurables;
  }

  public void moduleRemoved(final Module module) {
    // config editor will handle event and update modifiable model on its own, we just need to update the map
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      return;
    }

    for (Iterator<ModifiableFlexIdeBuildConfiguration> i = myConfigurablesMap.keySet().iterator(); i.hasNext(); ) {
      if (myConfigEditor.getModule(i.next()) == module) {
        i.remove();
      }
    }
    myEventDispatcher.getMulticaster().moduleRemoved(module);
  }

  public boolean isModified() {
    if (myConfigEditor.isModified()) return true;

    for (final NamedConfigurable<ModifiableFlexIdeBuildConfiguration> configurable : myConfigurablesMap.values()) {
      if (configurable.isModified()) {
        return true;
      }
    }
    return false;
  }

  public void apply() throws ConfigurationException {
    for (final NamedConfigurable<ModifiableFlexIdeBuildConfiguration> configurable : myConfigurablesMap.values()) {
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

  public int getBCCount(final ModifiableFlexIdeBuildConfiguration configuration) {
    Module module = myConfigEditor.getModule(configuration);
    return myConfigEditor.getConfigurations(module).length;
  }

  public void removeConfiguration(final ModifiableFlexIdeBuildConfiguration configuration) {
    myConfigEditor.removeConfiguration(configuration);
    CompositeConfigurable configurable = myConfigurablesMap.remove(configuration);
    myEventDispatcher.getMulticaster().buildConfigurationRemoved(FlexIdeBCConfigurable.unwrap(configurable));
  }

  public void addConfiguration(final Module module, final Runnable treeNodeNameUpdater) {
    if (module == null) {
      return;
    }

    Pair<String, BuildConfigurationNature> nameAndNature =
      promptForCreation(module, "Add Build Configuration", BuildConfigurationNature.DEFAULT);
    if (nameAndNature == null) {
      return;
    }

    final ModifiableFlexIdeBuildConfiguration configuration = myConfigEditor.createConfiguration(module);
    configuration.setName(nameAndNature.first);
    configuration.setNature(nameAndNature.second);
    configuration.getDependencies().setSdkEntry(findRecentSdk());
    createConfigurableNode(configuration, module, treeNodeNameUpdater);
  }

  @Nullable
  private SdkEntry findRecentSdk() {
    // TODO assign the same SDK as neighbour configurations have
    Library[] libraries = myConfigEditor.getSdksLibraries();
    return libraries.length > 0
           ? Factory.createSdkEntry(FlexProjectRootsUtil.getSdkLibraryId(libraries[0]), FlexSdk.getHomePath(libraries[0])) : null;
  }

  public void copy(final CompositeConfigurable configurable, final Runnable treeNodeNameUpdater) {
    ModifiableFlexIdeBuildConfiguration configuration = myConfigurablesMap.getKeysByValue(configurable).get(0);

    FlexIdeBCConfigurable unwrapped = FlexIdeBCConfigurable.unwrap(configurable);
    Pair<String, BuildConfigurationNature> nameAndNature = promptForCreation(unwrapped.getModule(), "Copy Build Configuration",
                                                                             configuration.getNature());
    if (nameAndNature == null) {
      return;
    }

    ModifiableFlexIdeBuildConfiguration newConfiguration = myConfigEditor.copyConfiguration(configuration, nameAndNature.second);
    newConfiguration.setName(nameAndNature.first);
    newConfiguration.getDependencies().setSdkEntry(findRecentSdk());

    // set correct output file extension for cloned configuration
    final String outputFileName = configuration.getOutputFileName();
    final String lowercase = outputFileName.toLowerCase();
    if (lowercase.endsWith(".swf") || lowercase.endsWith(".swc")) {
      final String extension = configuration.getOutputType() == OutputType.Library ? ".swc" : ".swf";
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
    final FlexIdeBCConfigurable configurable =
      new FlexIdeBCConfigurable(module, configuration, treeNodeNameUpdater, myConfigEditor);

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
}
