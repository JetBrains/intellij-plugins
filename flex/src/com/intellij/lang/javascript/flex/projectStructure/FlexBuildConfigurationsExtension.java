package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.Dependencies;
import com.intellij.lang.javascript.flex.projectStructure.model.DependencyEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModuleLibraryEntry;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.projectStructure.ui.CompositeConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.DependenciesConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexBCConfigurable;
import com.intellij.lang.javascript.flex.projectStructure.ui.FlexProjectStructureUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureExtension;
import com.intellij.openapi.roots.ui.configuration.projectRoot.RemoveConfigurableHandler;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.ui.navigation.Place;
import icons.FlexIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlexBuildConfigurationsExtension extends ModuleStructureExtension {

  final FlexBCConfigurator myConfigurator;

  public FlexBuildConfigurationsExtension() {
    myConfigurator = new FlexBCConfigurator();
  }

  public static FlexBuildConfigurationsExtension getInstance() {
    return ModuleStructureExtension.EP_NAME.findExtension(FlexBuildConfigurationsExtension.class);
  }

  public FlexBCConfigurator getConfigurator() {
    return myConfigurator;
  }

  @Override
  public void reset(Project project) {
    myConfigurator.reset(project);
  }

  @Override
  public boolean addModuleNodeChildren(final Module module,
                                       final MasterDetailsComponent.MyNode moduleNode,
                                       final Runnable treeNodeNameUpdater) {
    if (!(ModuleType.get(module) instanceof FlexModuleType)) {
      return false;
    }

    final List<CompositeConfigurable> configurables = myConfigurator.getOrCreateConfigurables(module, treeNodeNameUpdater);
    for (final CompositeConfigurable configurable : configurables) {
      if (MasterDetailsComponent.findNodeByObject(moduleNode, configurable.getEditableObject()) == null) {
        moduleNode.add(new BuildConfigurationNode(configurable));
      }
    }

    return configurables.size() > 0;
  }

  @Override
  public void moduleRemoved(final Module module) {
    myConfigurator.moduleRemoved(module);
  }

  @Override
  public boolean isModified() {
    return myConfigurator.isModified();
  }

  @Override
  public void apply() throws ConfigurationException {
    myConfigurator.apply();
  }

  @Override
  public void afterModelCommit() {
    myConfigurator.afterModelCommit();
  }

  @Override
  public void disposeUIResources() {
    myConfigurator.dispose();
  }

  @Override
  public List<RemoveConfigurableHandler<?>> getRemoveHandlers() {
    return Collections.singletonList(new RemoveConfigurableHandler<>(CompositeConfigurable.class) {
      @Override
      public boolean canBeRemoved(@NotNull Collection<? extends ModifiableFlexBuildConfiguration> configurations) {
        return myConfigurator.canBeRemoved(configurations.toArray(new ModifiableFlexBuildConfiguration[0]));
      }

      @Override
      public boolean remove(@NotNull Collection<? extends ModifiableFlexBuildConfiguration> configurations) {
        for (ModifiableFlexBuildConfiguration configuration : configurations) {
          myConfigurator.removeConfiguration(configuration);
        }
        return true;
      }
    });
  }

  @Override
  public boolean canBeCopied(final NamedConfigurable configurable) {
    return configurable instanceof CompositeConfigurable;
  }

  @Override
  public void copy(final NamedConfigurable configurable, final Runnable treeNodeNameUpdater) {
    myConfigurator.copy(((CompositeConfigurable)configurable), treeNodeNameUpdater);
  }

  @Override
  public Collection<AnAction> createAddActions(final NullableComputable<MasterDetailsComponent.MyNode> selectedNodeRetriever,
                                               final Runnable treeNodeNameUpdater,
                                               final Project project,
                                               final MasterDetailsComponent.MyNode root) {
    final Collection<AnAction> actions = new ArrayList<>(2);
    actions.add(new DumbAwareAction(FlexBundle.message("create.bc.action.text"), FlexBundle.message("create.bc.action.description"),
                                    FlexIcons.Flash_run_config) {
      @Override
      public void update(@NotNull final AnActionEvent e) {
        e.getPresentation().setVisible(getFlexModuleForNode(selectedNodeRetriever.compute()) != null);
      }

      @Override
      public void actionPerformed(@NotNull final AnActionEvent e) {
        final Module module = getFlexModuleForNode(selectedNodeRetriever.compute());
        myConfigurator.addConfiguration(module, treeNodeNameUpdater);
      }
    });
    return actions;
  }

  @Nullable
  private static Module getFlexModuleForNode(@Nullable MasterDetailsComponent.MyNode node) {
    while (node != null) {
      final NamedConfigurable configurable = node.getConfigurable();
      final Object editableObject = configurable == null ? null : configurable.getEditableObject();
      if (editableObject instanceof Module && ModuleType.get((Module)editableObject) instanceof FlexModuleType) {
        return (Module)editableObject;
      }
      final TreeNode parent = node.getParent();
      node = parent instanceof MasterDetailsComponent.MyNode ? (MasterDetailsComponent.MyNode)parent : null;
    }
    return null;
  }

  @Override
  @Nullable
  public ActionCallback selectOrderEntry(@NotNull final Module module, @Nullable final OrderEntry entry) {
    if (ModuleType.get(module) != FlexModuleType.getInstance()) {
      return null;
    }

    if (entry instanceof LibraryOrderEntry) {
      final Library library = ((LibraryOrderEntry)entry).getLibrary();
      if (library != null && library.getTable() == null && ((LibraryEx)library).getKind() == FlexLibraryType.FLEX_LIBRARY) {
        final String libraryId = FlexProjectRootsUtil.getLibraryId(library);

        final List<CompositeConfigurable> configurables = myConfigurator.getBCConfigurables(module);
        // several build configurations may depend on the same library, here we select the first one we find
        for (CompositeConfigurable configurable : configurables) {
          final FlexBCConfigurable bcConfigurable = FlexBCConfigurable.unwrap(configurable);
          final Dependencies dependencies = bcConfigurable.getDependenciesConfigurable().getEditableObject();
          for (DependencyEntry e : dependencies.getEntries()) {
            if (!(e instanceof ModuleLibraryEntry) || !((ModuleLibraryEntry)e).getLibraryId().equals(libraryId)) {
              continue;
            }

            final Place p = FlexProjectStructureUtil.createPlace(bcConfigurable, DependenciesConfigurable.getTabName());
            final DependenciesConfigurable.Location.TableEntry tableEntry =
              DependenciesConfigurable.Location.TableEntry.forModuleLibrary(libraryId);
            p.putPath(FlexBCConfigurable.LOCATION_ON_TAB, tableEntry);
            return ProjectStructureConfigurable.getInstance(module.getProject()).navigateTo(p, true);
          }
        }
      }
    }
    return ProjectStructureConfigurable.getInstance(module.getProject()).select(module.getName(), null, true);
  }
}
