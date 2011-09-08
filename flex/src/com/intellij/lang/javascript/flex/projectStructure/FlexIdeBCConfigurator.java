package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.*;
import com.intellij.lang.javascript.flex.projectStructure.ui.AddBuildConfigurationDialog;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexIdeBCConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexSdksModifiableModel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.ui.navigation.Place;
import com.intellij.util.EventDispatcher;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;

public class FlexIdeBCConfigurator {

  public interface Listener extends EventListener {
    void moduleRemoved(Module module);

    void buildConfigurationRemoved(FlexIdeBCConfigurable configurable);
  }

  // we can have only one Project Structure configuration dialog at a time, so it's OK to hold state for one project

  // keep these maps in sync!
  private Map<Module, List<NamedConfigurable<FlexIdeBuildConfiguration>>> myModuleToConfigurablesMap =
    new THashMap<Module, List<NamedConfigurable<FlexIdeBuildConfiguration>>>();
  private Map<FlexIdeBuildConfiguration, Module> myConfigurationsToModuleMap = new THashMap<FlexIdeBuildConfiguration, Module>();
  private boolean myModified;

  private final EventDispatcher<Listener> myEventDispatcher = EventDispatcher.create(Listener.class);

  private final LazyInitializer<Project> myFlexSdksModifiableModelInitializer = new LazyInitializer<Project>() {
    @Override
    protected void initialize(Project project) {
      mySdksModel.reset(project);
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
        ModuleStructureConfigurable.getInstance(entry.getKey().getProject()).ensureInitialized(configurable);
      }
    }
    myModified = false;
  }

  public List<NamedConfigurable<FlexIdeBuildConfiguration>> getOrCreateConfigurables(final Module module,
                                                                                     final Runnable treeNodeNameUpdater) {
    myFlexSdksModifiableModelInitializer.ensureInitialized(module.getProject());

    List<NamedConfigurable<FlexIdeBuildConfiguration>> configurables = myModuleToConfigurablesMap.get(module);

    if (configurables == null) {
      final FlexIdeBuildConfiguration[] configurations = FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations();
      configurables = new LinkedList<NamedConfigurable<FlexIdeBuildConfiguration>>();

      for (final FlexIdeBuildConfiguration configuration : configurations) {
        ModifiableFlexIdeBuildConfiguration clonedConfiguration = Factory.getCopy(configuration);
        FlexIdeBCConfigurable configurable = new FlexIdeBCConfigurable(module, clonedConfiguration, mySdksModel, treeNodeNameUpdater);
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
    if (myModified || mySdksModel.isModified()) return true;

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
        configurations.add(Factory.getCopy(configurable.getEditableObject()));
      }

      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          FlexBuildConfigurationManager.getInstance(module)
            .setBuildConfigurations(configurations.toArray(new FlexIdeBuildConfiguration[configurations.size()]));
        }
      });
    }

    mySdksModel.apply();
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

  public void addConfiguration(final Module module, final Runnable treeNodeNameUpdater) {
    if (module != null) {
      final ModifiableFlexIdeBuildConfiguration configuration = Factory.createBuildConfiguration();
      configuration.getDependencies().setSdkEntry(findRecentSdk());
      addConfiguration(module, configuration, "Add Build Configuration", treeNodeNameUpdater);
    }
  }

  @Nullable
  private SdkEntry findRecentSdk() {
    // TODO assign the same SDK as neighbour configurations have
    Library[] libraries = mySdksModel.getLibraries();
    return libraries.length > 0
           ? Factory.createSdkEntry(FlexProjectRootsUtil.getSdkLibraryId(libraries[0]), FlexSdk.getHomePath(libraries[0])) : null;
  }

  public void copy(final FlexIdeBCConfigurable configurable, final Runnable treeNodeNameUpdater) {
    FlexIdeBuildConfiguration configuration = configurable.getCurrentConfiguration();
    ModifiableFlexIdeBuildConfiguration newConfiguration = Factory.getCopy(configuration);
    Module module = myConfigurationsToModuleMap.get(configurable.getEditableObject());
    addConfiguration(module, newConfiguration, "Copy Build Configuration", treeNodeNameUpdater);
  }

  private void addConfiguration(final Module module,
                                final ModifiableFlexIdeBuildConfiguration configuration,
                                final String dialogTitle,
                                final Runnable treeNodeNameUpdater) {
    final Project project = module.getProject();
    final AddBuildConfigurationDialog dialog =
      new AddBuildConfigurationDialog(project, dialogTitle, getUsedNames(module), configuration.getTargetPlatform(),
                                      configuration.isPureAs(), configuration.getOutputType());
    dialog.show();

    if (dialog.isOK()) {
      myModified = true;

      configuration.setName(dialog.getName());
      configuration.setTargetPlatform(dialog.getTargetPlatform());
      configuration.setPureAs(dialog.isPureActionScript());
      configuration.setOutputType(dialog.getOutputType());

      // just to simplify serialized view
      resetNonApplicableValuesToDefaults(configuration);

      // set correct output file extension for cloned configuration
      final String outputFileName = configuration.getOutputFileName();
      final String lowercase = outputFileName.toLowerCase();
      if (lowercase.endsWith(".swf") || lowercase.endsWith(".swc")) {
        final String extension = configuration.getOutputType() == OutputType.Library ? ".swc" : ".swf";
        configuration.setOutputFileName(outputFileName.substring(0, outputFileName.length() - ".sw_".length()) + extension);
      }

      final FlexIdeBCConfigurable configurable = new FlexIdeBCConfigurable(module, configuration, mySdksModel, treeNodeNameUpdater);

      NamedConfigurable<FlexIdeBuildConfiguration> wrapped = configurable.wrapInTabsIfNeeded();
      myModuleToConfigurablesMap.get(module).add(wrapped);
      myConfigurationsToModuleMap.put(configuration, module);

      final MasterDetailsComponent.MyNode node = new BuildConfigurationNode(wrapped);
      FlexIdeModuleStructureExtension.addConfigurationChildNodes(configurable, node);

      final ModuleStructureConfigurable moduleStructureConfigurable = ModuleStructureConfigurable.getInstance(project);
      moduleStructureConfigurable.addNode(node, moduleStructureConfigurable.findModuleNode(module));

      final Place place = new Place().putPath(ProjectStructureConfigurable.CATEGORY, moduleStructureConfigurable)
        .putPath(MasterDetailsComponent.TREE_OBJECT, configuration);
      ProjectStructureConfigurable.getInstance(project).navigateTo(place, true);
    }
  }

  private static void resetNonApplicableValuesToDefaults(final ModifiableFlexIdeBuildConfiguration configuration) {
    final FlexIdeBuildConfiguration defaultConfiguration = Factory.createBuildConfiguration();

    if (configuration.getOutputType() != OutputType.RuntimeLoadedModule) {
      configuration.setOptimizeFor(defaultConfiguration.getOptimizeFor());
    }

    if (configuration.getOutputType() == OutputType.Library) {
      configuration.setMainClass(defaultConfiguration.getMainClass());
    }

    if (configuration.getTargetPlatform() != TargetPlatform.Web ||
        configuration.getOutputType() != OutputType.Application) {
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
  }

  private Collection<String> getUsedNames(final Module module) {
    final Collection<String> result = new LinkedList<String>();
    for (final NamedConfigurable<FlexIdeBuildConfiguration> configurable : myModuleToConfigurablesMap.get(module)) {
      result.add(configurable.getDisplayName());
    }
    return result;
  }

  public List<NamedConfigurable<FlexIdeBuildConfiguration>> getBCConfigurables(@NotNull Module module) {
    return myModuleToConfigurablesMap.get(module);
  }
}
