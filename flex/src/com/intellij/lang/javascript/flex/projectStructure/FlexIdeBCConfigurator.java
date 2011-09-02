package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.ui.AddBuildConfigurationDialog;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexSdksModifiableModel;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexIdeBCConfigurable;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.ui.navigation.Place;
import com.intellij.util.EventDispatcher;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.*;

public class FlexIdeBCConfigurator {

  public interface Listener extends EventListener {
    void moduleRemoved(Module module);

    void buildConfigurationRemoved(FlexIdeBCConfigurable configurable);
  }

  // keep these maps in sync!
  private Map<Module, List<NamedConfigurable<FlexIdeBuildConfiguration>>> myModuleToConfigurablesMap =
    new THashMap<Module, List<NamedConfigurable<FlexIdeBuildConfiguration>>>();
  private Map<FlexIdeBuildConfiguration, Module> myConfigurationsToModuleMap = new THashMap<FlexIdeBuildConfiguration, Module>();
  private boolean myModified;

  private final EventDispatcher<Listener> myEventDispatcher = EventDispatcher.create(Listener.class);

  private final LazyInitializer myFlexSdksModifiableModelInitializer = new LazyInitializer() {
    @Override
    protected void initialize() {
      mySdksModel.resetFrom(FlexSdkManager.getInstance());
    }
  };

  private final FlexSdksModifiableModel mySdksModel = new FlexSdksModifiableModel();

  public void addListener(Listener listener, Disposable parentDisposable) {
    myEventDispatcher.addListener(listener, parentDisposable);
  }

  public void reset() {
    for (final Map.Entry<Module, List<NamedConfigurable<FlexIdeBuildConfiguration>>> entry : myModuleToConfigurablesMap.entrySet()) {
      final List<NamedConfigurable<FlexIdeBuildConfiguration>> configurables = entry.getValue();

      for (final NamedConfigurable configurable : configurables) {
        configurable.reset();
      }
    }
    myModified = false;
  }

  public List<NamedConfigurable<FlexIdeBuildConfiguration>> getOrCreateConfigurables(final Module module,
                                                                                     final Runnable treeNodeNameUpdater,
                                                                                     ModifiableRootModel modifiableRootModel) {
    myFlexSdksModifiableModelInitializer.ensureInitialized();

    List<NamedConfigurable<FlexIdeBuildConfiguration>> configurables = myModuleToConfigurablesMap.get(module);

    if (configurables == null) {
      final FlexIdeBuildConfiguration[] configurations = FlexIdeBuildConfigurationManager.getInstance(module).getBuildConfigurations();
      configurables = new LinkedList<NamedConfigurable<FlexIdeBuildConfiguration>>();

      for (final FlexIdeBuildConfiguration configuration : configurations) {
        final FlexIdeBuildConfiguration clonedConfiguration = configuration.clone();
        FlexIdeBCConfigurable configurable =
          new FlexIdeBCConfigurable(module, clonedConfiguration, treeNodeNameUpdater, modifiableRootModel, mySdksModel);
        configurables.add(configurable.wrapInTabsIfNeeded());
        myConfigurationsToModuleMap.put(clonedConfiguration, module);
      }

      myModuleToConfigurablesMap.put(module, configurables);
    }

    return configurables;
  }

  public void moduleRemoved(final Module module) {
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      return;
    }

    for (final NamedConfigurable<FlexIdeBuildConfiguration> configurable : myModuleToConfigurablesMap.get(module)) {
      myConfigurationsToModuleMap.remove(configurable.getEditableObject());
    }

    myModuleToConfigurablesMap.remove(module);
    myEventDispatcher.getMulticaster().moduleRemoved(module);
  }

  public boolean isModified() {
    if (myModified || mySdksModel.isModified(FlexSdkManager.getInstance())) return true;

    for (final List<NamedConfigurable<FlexIdeBuildConfiguration>> configurables : myModuleToConfigurablesMap.values()) {
      for (final NamedConfigurable configurable : configurables) {
        if (configurable.isModified()) {
          return true;
        }
      }
    }
    return false;
  }


  public void apply() throws ConfigurationException {
    for (final Map.Entry<Module, List<NamedConfigurable<FlexIdeBuildConfiguration>>> entry : myModuleToConfigurablesMap.entrySet()) {
      final Module module = entry.getKey();

      final List<FlexIdeBuildConfiguration> configurations = new LinkedList<FlexIdeBuildConfiguration>();
      final List<NamedConfigurable<FlexIdeBuildConfiguration>> configurables = entry.getValue();

      Set<String> names = new HashSet<String>(configurables.size());
      for (NamedConfigurable<FlexIdeBuildConfiguration> configurable : configurables) {
        if (!names.add(configurable.getDisplayName())) {
          throw new ConfigurationException(
            MessageFormat.format("Module ''{0}'' has duplicate build configuration names: {1}", module.getName(),
                                 configurable.getDisplayName()));
        }
      }
      for (final NamedConfigurable<FlexIdeBuildConfiguration> configurable : configurables) {
        configurable.apply();
        configurations.add(configurable.getEditableObject().clone());
      }

      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          FlexIdeBuildConfigurationManager.getInstance(module)
            .setBuildConfigurations(configurations.toArray(new FlexIdeBuildConfiguration[configurations.size()]));
        }
      });
    }

    mySdksModel.applyTo(FlexSdkManager.getInstance());
    myModified = false;
  }

  public void dispose() {
    // configurables are disposed by MasterDetailsComponent
    myModuleToConfigurablesMap.clear();
    myConfigurationsToModuleMap.clear();
    myFlexSdksModifiableModelInitializer.dispose();
  }

  public int getBCCount(final FlexIdeBuildConfiguration configuration) {
    return getBCCount(myConfigurationsToModuleMap.get(configuration));
  }

  public int getBCCount(Module module) {
    return myModuleToConfigurablesMap.get(module).size();
  }

  public void removeConfiguration(final FlexIdeBuildConfiguration configuration) {
    myModified = true;
    final Module module = myConfigurationsToModuleMap.get(configuration);
    myConfigurationsToModuleMap.remove(configuration);
    final Iterator<NamedConfigurable<FlexIdeBuildConfiguration>> configurablesIterator = myModuleToConfigurablesMap.get(module).iterator();
    while (configurablesIterator.hasNext()) {
      final NamedConfigurable<FlexIdeBuildConfiguration> configurable = configurablesIterator.next();
      if (configuration == configurable.getEditableObject()) {
        configurablesIterator.remove();
        myEventDispatcher.getMulticaster().buildConfigurationRemoved(FlexIdeBCConfigurable.unwrapIfNeeded(configurable));
        break;
      }
    }
  }

  public void addConfiguration(final Module module, final Runnable treeNodeNameUpdater, ModulesConfigurator modulesConfigurator) {
    if (module != null) {
      ModifiableRootModel modifiableRootModel = modulesConfigurator.getModuleEditor(module).getModifiableRootModel();
      final FlexIdeBuildConfiguration configuration = new FlexIdeBuildConfiguration();
      addConfiguration(module, configuration, "Add Build Configuration", treeNodeNameUpdater, modifiableRootModel);
    }
  }

  public void copy(final FlexIdeBCConfigurable configurable, final Runnable treeNodeNameUpdater) {
    final FlexIdeBuildConfiguration configuration = configurable.getCurrentConfiguration();
    final FlexIdeBuildConfiguration newConfiguration = configuration.clone();
    final Module module = myConfigurationsToModuleMap.get(configurable.getEditableObject());
    addConfiguration(module, newConfiguration, "Copy Build Configuration", treeNodeNameUpdater, configurable.getModifiableRootModel());
  }

  private void addConfiguration(final Module module,
                                final FlexIdeBuildConfiguration configuration,
                                final String dialogTitle,
                                final Runnable treeNodeNameUpdater, ModifiableRootModel modifiableRootModel) {
    final Project project = module.getProject();
    final AddBuildConfigurationDialog dialog =
      new AddBuildConfigurationDialog(project, dialogTitle, getUsedNames(module), configuration.TARGET_PLATFORM,
                                      configuration.PURE_ACTION_SCRIPT, configuration.OUTPUT_TYPE);
    dialog.show();

    if (dialog.isOK()) {
      myModified = true;

      configuration.NAME = dialog.getName();
      configuration.TARGET_PLATFORM = dialog.getTargetPlatform();
      configuration.PURE_ACTION_SCRIPT = dialog.isPureActionScript();
      configuration.OUTPUT_TYPE = dialog.getOutputType();

      // just to simplify serialized view
      resetNonApplicableValuesToDefaults(configuration);

      // set correct output file extension for cloned configuration
      final String outputFileName = configuration.OUTPUT_FILE_NAME;
      final String lowercase = outputFileName.toLowerCase();
      if (lowercase.endsWith(".swf") || lowercase.endsWith(".swc")) {
        final String extension = configuration.OUTPUT_TYPE == FlexIdeBuildConfiguration.OutputType.Library ? ".swc" : ".swf";
        configuration.OUTPUT_FILE_NAME = outputFileName.substring(0, outputFileName.length() - ".sw_".length()) + extension;
      }

      final FlexIdeBCConfigurable configurable = new FlexIdeBCConfigurable(module, configuration, treeNodeNameUpdater, modifiableRootModel,
                                                                           mySdksModel);

      NamedConfigurable<FlexIdeBuildConfiguration> wrapped = configurable.wrapInTabsIfNeeded();
      final List<NamedConfigurable<FlexIdeBuildConfiguration>> configurables = myModuleToConfigurablesMap.get(module);
      configurables.add(wrapped);
      myConfigurationsToModuleMap.put(configuration, module);

      final MasterDetailsComponent.MyNode node = new BuildConfigurationNode(wrapped);
      FlexIdeModuleStructureExtension.addConfigurationChildNodes(project, configurable, node);

      final ModuleStructureConfigurable moduleStructureConfigurable = ModuleStructureConfigurable.getInstance(project);
      moduleStructureConfigurable.addNode(node, moduleStructureConfigurable.findModuleNode(module));

      final Place place = new Place().putPath(ProjectStructureConfigurable.CATEGORY, moduleStructureConfigurable)
        .putPath(MasterDetailsComponent.TREE_OBJECT, configuration);
      ProjectStructureConfigurable.getInstance(project).navigateTo(place, true);
    }
  }

  private static void resetNonApplicableValuesToDefaults(final FlexIdeBuildConfiguration configuration) {
    final FlexIdeBuildConfiguration defaultConfiguration = new FlexIdeBuildConfiguration();

    if (configuration.OUTPUT_TYPE != FlexIdeBuildConfiguration.OutputType.RuntimeLoadedModule) {
      configuration.OPTIMIZE_FOR = defaultConfiguration.OPTIMIZE_FOR;
    }

    if (configuration.OUTPUT_TYPE == FlexIdeBuildConfiguration.OutputType.Library) {
      configuration.MAIN_CLASS = defaultConfiguration.MAIN_CLASS;
    }

    if (configuration.TARGET_PLATFORM != FlexIdeBuildConfiguration.TargetPlatform.Web) {
      configuration.USE_HTML_WRAPPER = defaultConfiguration.USE_HTML_WRAPPER;
      configuration.WRAPPER_TEMPLATE_PATH = defaultConfiguration.WRAPPER_TEMPLATE_PATH;

      configuration.DEPENDENCIES.TARGET_PLAYER = defaultConfiguration.DEPENDENCIES.TARGET_PLAYER;
    }

    if (configuration.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Mobile || configuration.PURE_ACTION_SCRIPT) {
      configuration.DEPENDENCIES.COMPONENT_SET = defaultConfiguration.DEPENDENCIES.COMPONENT_SET;
    }
  }

  private Collection<String> getUsedNames(final Module module) {
    final Collection<String> result = new LinkedList<String>();
    for (final NamedConfigurable<FlexIdeBuildConfiguration> configurable : myModuleToConfigurablesMap.get(module)) {
      result.add(configurable.getDisplayName());
    }
    return result;
  }


  /**
   * TODO remove this
   *
   * @Deprecated
   */
  @Deprecated
  public boolean isModulesConfiguratorModified() {
    //for (List<NamedConfigurable<FlexIdeBuildConfiguration>> configurables : myModuleToConfigurablesMap.values()) {
    //  for (NamedConfigurable<FlexIdeBuildConfiguration> configurable : configurables) {
    //    if (configurable.isModuleConfiguratorModified()) {
    //      return true;
    //    }
    //  }
    //}
    return false;
  }

  public List<NamedConfigurable<FlexIdeBuildConfiguration>> getBCConfigurables(@NotNull Module module) {
    return myModuleToConfigurablesMap.get(module);
  }
}
