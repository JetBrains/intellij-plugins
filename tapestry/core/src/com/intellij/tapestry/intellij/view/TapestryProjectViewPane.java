package com.intellij.tapestry.intellij.view;

import com.intellij.ProjectTopics;
import com.intellij.ide.PsiCopyPasteManager;
import com.intellij.ide.SelectInTarget;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.projectView.impl.ProjectViewTree;
import com.intellij.ide.ui.customization.CustomizationUtil;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.events.FileSystemListener;
import com.intellij.tapestry.core.events.TapestryModelChangeListener;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.actions.safedelete.SafeDeleteProvider;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.toolwindow.TapestryToolWindow;
import com.intellij.tapestry.intellij.toolwindow.TapestryToolWindowFactory;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.intellij.view.actions.GroupElementFilesToggleAction;
import com.intellij.tapestry.intellij.view.actions.ShowLibrariesTogleAction;
import com.intellij.tapestry.intellij.view.actions.StartInBasePackageAction;
import com.intellij.tapestry.intellij.view.nodes.*;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.ui.treeStructure.actions.CollapseAllAction;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.EditSourceOnEnterKeyHandler;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.tree.TreeUtil;
import icons.TapestryIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.Collections;
import java.util.List;

/**
 * The Tapestry view pane.
 */
public class TapestryProjectViewPane extends AbstractProjectViewPane implements FileSystemListener, TapestryModelChangeListener {

  private static final String VIEW_TITLE = "Tapestry";
  private static final String ID = "TapestryProjectView";
  private static final String COMPONENT_NAME = "TAPESTRY_PROJECT_VIEW";


  private final ModuleListener myModuleListener;
  private final TapestryIdeView myIdeView;
  private JScrollPane myComponent;
  private boolean myShown;
  private boolean myGroupElementFiles = true;
  private boolean myShowLibraries = true;
  private boolean myFromBasePackage;
  private final MessageBusConnection myMessageBusConnection;


  public TapestryProjectViewPane(final Project project) {
    super(project);

    myIdeView = new TapestryIdeView(this);

    myModuleListener = new ModuleListener() {
      @Override
      public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
        reload();
      }

      @Override
      public void moduleAdded(@NotNull Project project, @NotNull Module module) {
        reload();
      }
    };

    myMessageBusConnection = project.getMessageBus().connect();
    myMessageBusConnection.subscribe(ProjectTopics.MODULES, myModuleListener);

    for (Module module : ModuleManager.getInstance(myProject).getModules()) {
      TapestryModuleSupportLoader.getTapestryProject(module).getEventsManager().addFileSystemListener(this);
      TapestryModuleSupportLoader.getTapestryProject(module).getEventsManager().addTapestryModelListener(this);
    }
  }//Constructor

  @Override
  public boolean isInitiallyVisible() {
    myShown = false;
    for (Module module : ModuleManager.getInstance(myProject).getModules()) {
      if (TapestryUtils.isTapestryModule(module)) {
        myShown = true;
        break;
      }
    }
    return myShown;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addToolbarActions(@NotNull DefaultActionGroup defaultactiongroup) {
    for (AnAction action : defaultactiongroup.getChildren(null)) {
      if (action.getTemplatePresentation().getText().equals("Autoscroll to Source")) {
        continue;
      }

      defaultactiongroup.remove(action);
    }

    defaultactiongroup.addAction(new StartInBasePackageAction() {

      @Override
      public boolean isSelected(@NotNull AnActionEvent e) {
        return myFromBasePackage;
      }

      @Override
      public void setSelected(@NotNull AnActionEvent e, boolean state) {
        myFromBasePackage = state;

        updateFromRoot(false);
      }
    }).setAsSecondary(true);

    defaultactiongroup.addAction(new GroupElementFilesToggleAction() {

      @Override
      public boolean isSelected(@NotNull AnActionEvent e) {
        return myGroupElementFiles;
      }

      @Override
      public void setSelected(@NotNull AnActionEvent e, boolean state) {
        myGroupElementFiles = state;

        updateFromRoot(false);
      }
    }).setAsSecondary(true);

    defaultactiongroup.addAction(new ShowLibrariesTogleAction() {
      @Override
      public boolean isSelected(@NotNull AnActionEvent e) {
        return myShowLibraries;
      }

      @Override
      public void setSelected(@NotNull AnActionEvent e, boolean state) {
        myShowLibraries = state;

        updateFromRoot(false);
      }
    }).setAsSecondary(true);
    defaultactiongroup.add(new CollapseAllAction(myTree));
  }//addToolbarActions

  /**
   * Returns the project instance of this view pane.
   *
   * @param project the project that contains this view pane.
   * @return the project instance of this view pane.
   */
  public static TapestryProjectViewPane getInstance(@NotNull final Project project) {
    return (TapestryProjectViewPane) ProjectView.getInstance(project).getProjectViewPaneById(ID);
  }//getInstance

  /**
   * Reloads the view pane.
   */
  public void reload() {
    modulesChanged();
    updateFromRoot(true);
  }//reload

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public String getTitle() {
    return VIEW_TITLE;
  }//setTitle

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public Icon getIcon() {
    return TapestryIcons.Tapestry_logo_small;
  }//getIcon

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  public String getId() {
    return ID;
  }//getId

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public JComponent createComponent() {
    initTree();

    return myComponent;
  }//createComponent

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  public ActionCallback updateFromRoot(boolean b) {
    if (myTree != null) ((SimpleTreeBuilder)getTreeBuilder()).updateFromRoot(b);
    return ActionCallback.DONE;
  }//updateFromRoot


  /**
   * {@inheritDoc}
   */
  @Override
  public void select(Object object, VirtualFile virtualFile, boolean b) {
    //do nothing
  }//select

  /**
   * {@inheritDoc}
   */
  @Override
  public int getWeight() {
    return 5;
  }//getWeight

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public SelectInTarget createSelectInTarget() {
    return new TapestryProjectSelectInTarget(myProject);
  }//createSelectInTarget

  /**
   * {@inheritDoc}
   */
  @Override
  public void fileCreated(String path) {
    updateFromRoot(true);
  }//fieCreated

  /**
   * {@inheritDoc}
   */
  @Override
  public void fileDeleted(String path) {
    updateFromRoot(true);
  }//fileDeleted

  /**
   * {@inheritDoc}
   */
  @Override
  public void classCreated(String classFqn) {
    updateFromRoot(true);
  }//classCreated

  /**
   * {@inheritDoc}
   */
  @Override
  public void classDeleted(String classFqn) {
    updateFromRoot(true);
  }//classDeleted

  /**
   * {@inheritDoc}
   */
  @Override
  public void fileContentsChanged(IResource changedFile) {
    //do nothing
  }//fileContentsChanged

  /**
   * {@inheritDoc}
   */
  @Override
  public void modelChanged() {
    reload();
  }//modelChanged

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    if (myModuleListener != null) {
      myMessageBusConnection.disconnect();
    }
    super.dispose();
  }

  /**
   * Check if a file can be selected.
   *
   * @return {@code true} if the file can be selected, {@code false} otherwise.
   */
  public boolean canSelect() {
    return !getPathToSelect().isEmpty();
  }//canSelect

  public boolean isGroupElementFiles() {
    return myGroupElementFiles;
  }//isGroupElementFiles

  public boolean isShowLibraries() {
    return myShowLibraries;
  }//isShowLibraries

  public boolean isFromBasePackage() {
    return myFromBasePackage;
  }//isFromBasePackage

  private List<Object> getPathToSelect() {
    return Collections.emptyList();
  }//getPathToSelect

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getData(@NotNull String dataId) {
    if (CommonDataKeys.PROJECT.is(dataId)) {
      return myProject;
    }

    if (LangDataKeys.IDE_VIEW.is(dataId)) {
      if (getSelectedDescriptor() == null) {
        return null;
      }

      Object element = getSelectedDescriptor().getElement();

      if (!(element instanceof PsiDirectory) && !(element instanceof PsiFile) ||
          IdeaUtils.findFirstParent(getSelectedNode(), ExternalLibraryNode.class) != null) {
        return null;
      }
      return myIdeView;
    }

    if (PlatformCoreDataKeys.MODULE.is(dataId)) {
      final NodeDescriptor nodeDescriptor = getSelectedDescriptor();
      if (nodeDescriptor != null) {
        if (nodeDescriptor instanceof TapestryNode) {
          return ((TapestryNode)nodeDescriptor).getModule();
        }
        if (nodeDescriptor instanceof ModuleNode) {
          return ((ModuleNode)nodeDescriptor).getModule();
        }
      }
    }

    if (CommonDataKeys.NAVIGATABLE.is(dataId)) {
      if (getSelectedDescriptor() == null) {
        return null;
      }

      if (getSelectedDescriptor().getElement() instanceof PresentationLibraryElement) {
        return ((IntellijResource)((PresentationLibraryElement)getSelectedDescriptor().getElement()).getElementClass().getFile())
            .getPsiFile();
      }
    }

    if (PlatformDataKeys.DELETE_ELEMENT_PROVIDER.is(dataId)) {
      return new SafeDeleteProvider();
    }
    if (PlatformCoreDataKeys.SELECTED_ITEM.is(dataId)) {
      return getSelectedNode();
    }
    return null;
  }//getData

  Project getProject() {
    return myProject;
  }//getProject

  private void initTree() {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
    DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

    myTree = new ProjectViewTree(treeModel) {
      public String toString() {
        return getTitle() + " " + super.toString();
      }
    };

    setTreeBuilder(new TapestryViewTreeBuilder(myTree, myProject));
    ((SimpleTreeBuilder)getTreeBuilder()).initRoot();

    myTree.setRootVisible(false);
    myTree.setShowsRootHandles(true);
    myTree.expandPath(new TreePath(myTree.getModel().getRoot()));
    TreeUtil.expandRootChildIfOnlyOne(myTree);

    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    EditSourceOnDoubleClickHandler.install(myTree);
    EditSourceOnEnterKeyHandler.install(myTree);
    TreeUtil.installActions(myTree);

    myTree.setTransferHandler(new ViewTransferHandler(this));
    MouseInputAdapter mouseListener = new ViewMouseListener(this);

    myTree.addMouseListener(mouseListener);
    myTree.addMouseMotionListener(mouseListener);

    addTreeListeners();

    new TreeSpeedSearch(myTree);

    myTreeStructure = getTreeBuilder().getTreeStructure();

    myComponent = ScrollPaneFactory.createScrollPane(myTree);
    myComponent.setBorder(BorderFactory.createEmptyBorder());
    CustomizationUtil.installPopupHandler(myTree, IdeActions.GROUP_PROJECT_VIEW_POPUP, "TapestryProjectViewPopup");
  }


  private void addTreeListeners() {

    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent event) {
        if (event.getNewLeadSelectionPath() != null) {
          TapestryToolWindow toolWindow = TapestryToolWindowFactory.getToolWindow(getProject());

          if (toolWindow != null) {
            SimpleNode selectedNode =
                ((SimpleNode)((DefaultMutableTreeNode)event.getNewLeadSelectionPath().getLastPathComponent()).getUserObject());

            if (!(selectedNode instanceof TapestryNode)) {
              toolWindow.update(null, null, null);

              return;
            }

            if (selectedNode instanceof PageNode || selectedNode instanceof ComponentNode || selectedNode instanceof MixinNode) {
              toolWindow.update((Module)getData(PlatformCoreDataKeys.MODULE.getName()), selectedNode.getElement(),
                                Collections.singletonList(((PresentationLibraryElement)selectedNode.getElement()).getElementClass()));
            }

            if (selectedNode instanceof ClassNode || selectedNode instanceof FileNode) {
              TapestryNode parentSelectedNode =
                  ((TapestryNode)((DefaultMutableTreeNode)((DefaultMutableTreeNode)event.getNewLeadSelectionPath().getLastPathComponent())
                      .getParent()).getUserObject());

              if (parentSelectedNode.getElement() instanceof PresentationLibraryElement) {
                toolWindow.update((Module)getData(PlatformCoreDataKeys.MODULE.getName()), parentSelectedNode.getElement(),
                                  Collections
                                    .singletonList(((PresentationLibraryElement)parentSelectedNode.getElement()).getElementClass()));
              }
              else {

                IJavaClassType elementClass = null;
                PresentationLibraryElement component = null;

                Module module = ((TapestryNode)selectedNode).getModule();
                TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

                if (selectedNode instanceof ClassNode) {
                  elementClass = new IntellijJavaClassType(module, ((PsiFile)selectedNode.getElement()));

                  try {
                    component = PresentationLibraryElement.createProjectElementInstance(elementClass, tapestryProject);
                  }
                  catch (NotTapestryElementException ex) {
                    // the selected class is not a Tapestry element
                  }
                }

                if (selectedNode instanceof FileNode) {

                  elementClass = tapestryProject.findElementByTemplate((PsiFile)selectedNode.getElement()).getElementClass();

                  if (elementClass != null) {
                    component = PresentationLibraryElement.createProjectElementInstance(elementClass, tapestryProject);
                  }
                }

                if (component != null) {
                  toolWindow.update((Module)getData(PlatformCoreDataKeys.MODULE.getName()), component,
                                    Collections.singletonList(component.getElementClass()));
                }
              }
            }
            if (!(selectedNode instanceof PageNode ||
                  selectedNode instanceof ComponentNode ||
                  selectedNode instanceof MixinNode ||
                  selectedNode instanceof ClassNode ||
                  selectedNode instanceof FileNode)) {

              toolWindow.update(null, null, null);
            }
          }
        }
      }
    });
    myTree.addKeyListener(new PsiCopyPasteManager.EscapeHandler());
  }//addTreeListeners

  private void modulesChanged() {
    boolean shouldShow = false;
    for (Module module : ModuleManager.getInstance(myProject).getModules()) {
      TapestryModuleSupportLoader.getTapestryProject(module).getEventsManager().removeFileSystemListener(this);
      TapestryModuleSupportLoader.getTapestryProject(module).getEventsManager().removeTapestryModelListener(this);
      TapestryModuleSupportLoader.getTapestryProject(module).getEventsManager().addFileSystemListener(this);
      TapestryModuleSupportLoader.getTapestryProject(module).getEventsManager().addTapestryModelListener(this);

      if (TapestryUtils.isTapestryModule(module)) {
        shouldShow = true;
      }
    }

    if (shouldShow && !myShown) {
      addMe();
    }

    if (!shouldShow && myShown) {
      removeMe();
    }
  }//modulesChanged

  private void addMe() {
    final ProjectView projectView = ProjectView.getInstance(myProject);
    projectView.addProjectPane(this);
    myShown = true;
  }//addMe

  private void removeMe() {
    final ProjectView projectView = ProjectView.getInstance(myProject);
    projectView.removeProjectPane(this);
    myShown = false;
  }//removeMe

}//TapestryProjectViewPane