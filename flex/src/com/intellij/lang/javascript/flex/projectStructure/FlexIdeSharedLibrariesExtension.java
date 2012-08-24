package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.CreateNewLibraryAction;
import com.intellij.openapi.roots.ui.configuration.projectRoot.*;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;

import javax.swing.tree.TreeNode;
import java.util.*;

public class FlexIdeSharedLibrariesExtension extends ModuleStructureExtension {
  private static final Object GLOBAL_LIBRARIES = new Object();
  private static final Object PROJECT_LIBRARIES = new Object();

  private Project myProject;

  public void addRootNodes(final MasterDetailsComponent.MyNode parent, final Project project, final Runnable treeUpdater) {
    myProject = project;
    final ProjectStructureConfigurable projectConfig = ProjectStructureConfigurable.getInstance(project);
    {
      MasterDetailsComponent.MyNode globalLibrariesNode = new MasterDetailsComponent.MyNode(
        new SharedLibraryParentNodeConfigurable(GLOBAL_LIBRARIES, "Global libraries", "Global Libraries",
                                     "Use global libraries to share code between many projects"));
      projectConfig.getModulesConfig().addNode(globalLibrariesNode, parent);

      LibrariesModifiableModel globalLibrariesModel =
        ProjectStructureConfigurable.getInstance(project).getContext().getGlobalLibrariesProvider().getModifiableModel();
      addLibrariesNodes(globalLibrariesModel, globalLibrariesNode, treeUpdater, project);
    }

    {
      MasterDetailsComponent.MyNode projectLibrariesNode = new MasterDetailsComponent.MyNode(
        new SharedLibraryParentNodeConfigurable(PROJECT_LIBRARIES, "Project libraries", "Project Libraries",
                                     "Use project libraries to share code between modules in the project"));
      projectConfig.getModulesConfig().addNode(projectLibrariesNode, parent);

      LibrariesModifiableModel projectLibrariesModel =
        ProjectStructureConfigurable.getInstance(project).getContext().getProjectLibrariesProvider().getModifiableModel();
      addLibrariesNodes(projectLibrariesModel, projectLibrariesNode, treeUpdater, project);
    }
  }

  private static void addLibrariesNodes(final LibrariesModifiableModel model,
                                        final MasterDetailsComponent.MyNode parentNode,
                                        final Runnable treeUpdater, final Project project) {
    final List<Library> libraries = ContainerUtil.filter(model.getLibraries(), new Condition<Library>() {
      public boolean value(final Library library) {
        return FlexProjectRootsUtil.isFlexLibrary(library);
      }
    });

    Collections.sort(libraries, new Comparator<Library>() {
      public int compare(final Library o1, final Library o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });

    StructureConfigurableContext context = ProjectStructureConfigurable.getInstance(project).getContext();
    for (Library library : libraries) {
      parentNode.add(new LibraryNode(library, context, treeUpdater));
    }
  }

  public boolean removeObject(final Object editableObject) {
    if (editableObject instanceof Library) {
      final Library library = (Library)editableObject;
      if (library.getTable() == null) return false;
      final StructureConfigurableContext context = ProjectStructureConfigurable.getInstance(myProject).getContext();
      LibraryTable.ModifiableModel modifiableModel = context.getModifiableLibraryTable(library.getTable());
      modifiableModel.removeLibrary(library);
      return true;
    }
    return false;
  }

  public Collection<AnAction> createAddActions(final NullableComputable<MasterDetailsComponent.MyNode> selectedNodeRetriever,
                                               final Runnable treeNodeNameUpdater,
                                               final Project project,
                                               final MasterDetailsComponent.MyNode root) {
    final Collection<AnAction> actions = new ArrayList<AnAction>(2);
    actions.add(
      new CreateLibraryAction(project, treeNodeNameUpdater, "Global Library", "Create global library to share code between projects",
                              LibraryTablesRegistrar.APPLICATION_LEVEL, GLOBAL_LIBRARIES));
    actions.add(new CreateLibraryAction(project, treeNodeNameUpdater, "Project Library",
                                        "Create project library to share code between modules in the project",
                                        LibraryTablesRegistrar.PROJECT_LEVEL, PROJECT_LIBRARIES));
    return actions;
  }


  public Comparator<MasterDetailsComponent.MyNode> getNodeComparator() {
    return new Comparator<MasterDetailsComponent.MyNode>() {
      public int compare(final MasterDetailsComponent.MyNode o1, final MasterDetailsComponent.MyNode o2) {
        final Object object1 = o1.getConfigurable().getEditableObject();
        final Object object2 = o2.getConfigurable().getEditableObject();
        if (object1 == GLOBAL_LIBRARIES && object2 == PROJECT_LIBRARIES) return -1;
        if (object2 == GLOBAL_LIBRARIES && object1 == PROJECT_LIBRARIES) return 1;
        if (object1 == GLOBAL_LIBRARIES || object1 == PROJECT_LIBRARIES) return -1;
        if (object2 == GLOBAL_LIBRARIES || object2 == PROJECT_LIBRARIES) return 1;
        return 0;
      }
    };
  }

  public boolean isModified() {
    final ProjectLibrariesConfigurable projectLibrariesConfig =
      ProjectStructureConfigurable.getInstance(myProject).getProjectLibrariesConfig();
    if (projectLibrariesConfig.isModified()) {
      return true;
    }
    final GlobalLibrariesConfigurable globalLibrariesConfig =
      ProjectStructureConfigurable.getInstance(myProject).getGlobalLibrariesConfig();
    if (globalLibrariesConfig.isModified()) {
      return true;
    }
    return false;
  }

  public void apply() throws ConfigurationException {
    final ProjectLibrariesConfigurable projectLibrariesConfig =
      ProjectStructureConfigurable.getInstance(myProject).getProjectLibrariesConfig();
    if (projectLibrariesConfig.isModified()) {
      projectLibrariesConfig.apply();
    }
    final GlobalLibrariesConfigurable globalLibrariesConfig =
      ProjectStructureConfigurable.getInstance(myProject).getGlobalLibrariesConfig();
    if (globalLibrariesConfig.isModified()) {
      globalLibrariesConfig.apply();
    }
  }

  private static class CreateLibraryAction extends DumbAwareAction {
    private final Project myProject;
    private final Runnable myTreeUpdater;
    private final String myLevel;
    private final Object myParentNodeValue;

    private CreateLibraryAction(final Project project,
                                final Runnable treeUpdater,
                                String text,
                                String description,
                                final String level,
                                final Object parentNodeValue) {
      super(text, description, PlatformIcons.LIBRARY_ICON);
      myProject = project;
      myTreeUpdater = treeUpdater;
      myLevel = level;
      myParentNodeValue = parentNodeValue;
    }

    public void actionPerformed(final AnActionEvent e) {
      final ModuleStructureConfigurable modulesConfig = ProjectStructureConfigurable.getInstance(myProject).getModulesConfig();
      final Tree tree = modulesConfig.getTree();
      StructureConfigurableContext context = ProjectStructureConfigurable.getInstance(myProject).getContext();
      LibrariesModifiableModel modifiableModel = context.createModifiableModelProvider(myLevel).getModifiableModel();
      Library library = CreateNewLibraryAction.createLibrary(FlexLibraryType.getInstance(), tree, myProject, modifiableModel);

      TreeNode root = (TreeNode)tree.getModel().getRoot();
      MasterDetailsComponent.MyNode globalLibrariesNode = MasterDetailsComponent.findNodeByObject(root, myParentNodeValue);
      LibraryNode libraryNode = new LibraryNode(library, context, myTreeUpdater);
      modulesConfig.addNode(libraryNode, globalLibrariesNode);
      modulesConfig.selectNodeInTree(libraryNode);
    }
  }

  private static class LibraryNode extends MasterDetailsComponent.MyNode {
    public LibraryNode(Library library, StructureConfigurableContext c, final Runnable treeUpdater) {
      super(new FlexLibraryConfigurable(library, c, treeUpdater));
    }
  }

  private static class SharedLibraryParentNodeConfigurable extends TextConfigurable<Object> {

    public SharedLibraryParentNodeConfigurable(final Object object,
                                               final String displayName,
                                               final String bannerSlogan,
                                               final String descriptionText) {
      super(object, displayName, bannerSlogan, descriptionText, PlatformIcons.LIBRARY_ICON);
    }

    public String getHelpTopic() {
      return "reference.settingsdialog.project.structure.library";
    }
  }
}
