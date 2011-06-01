package com.intellij.tapestry.intellij.view;

import com.intellij.ProjectTopics;
import com.intellij.ide.DataManager;
import com.intellij.ide.PsiCopyPasteManager;
import com.intellij.ide.SelectInTarget;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.projectView.impl.ProjectViewTree;
import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleAdapter;
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
import com.intellij.tapestry.intellij.util.Icons;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.intellij.view.actions.GroupElementFilesToggleAction;
import com.intellij.tapestry.intellij.view.actions.ShowLibrariesTogleAction;
import com.intellij.tapestry.intellij.view.actions.StartInBasePackageAction;
import com.intellij.tapestry.intellij.view.nodes.*;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.ui.treeStructure.actions.CollapseAllAction;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
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

    myModuleListener = new ModuleAdapter() {
      public void moduleRemoved(Project project, Module module) {
        reload();
      }

      public void moduleAdded(Project project, Module module) {
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
  public void addToolbarActions(DefaultActionGroup defaultactiongroup) {
    for (AnAction action : defaultactiongroup.getChildren(null)) {
      if (action.getTemplatePresentation().getText().equals("Autoscroll to Source")) {
        continue;
      }

      defaultactiongroup.remove(action);
    }

    defaultactiongroup.add(new StartInBasePackageAction() {

      public boolean isSelected(AnActionEvent e) {
        return myFromBasePackage;
      }

      public void setSelected(AnActionEvent e, boolean state) {
        myFromBasePackage = state;

        updateFromRoot(false);
      }
    });

    defaultactiongroup.add(new GroupElementFilesToggleAction() {

      public boolean isSelected(AnActionEvent e) {
        return myGroupElementFiles;
      }

      public void setSelected(AnActionEvent e, boolean state) {
        myGroupElementFiles = state;

        updateFromRoot(false);
      }
    });

    defaultactiongroup.add(new ShowLibrariesTogleAction() {
      public boolean isSelected(AnActionEvent e) {
        return myShowLibraries;
      }

      public void setSelected(AnActionEvent e, boolean state) {
        myShowLibraries = state;

        updateFromRoot(false);
      }
    });
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
  public String getTitle() {
    return VIEW_TITLE;
  }//setTitle

  /**
   * {@inheritDoc}
   */
  public Icon getIcon() {
    return Icons.TAPESTRY_LOGO_SMALL;
  }//getIcon

  /**
   * {@inheritDoc}
   */
  @NotNull
  public String getId() {
    return ID;
  }//getId

  /**
   * {@inheritDoc}
   */
  public JComponent createComponent() {
    initTree();

    return myComponent;
  }//createComponent

  /**
   * {@inheritDoc}
   */
  public ActionCallback updateFromRoot(boolean b) {
    if (myTree != null) ((SimpleTreeBuilder)getTreeBuilder()).updateFromRoot(b);
    return new ActionCallback.Done();
  }//updateFromRoot


  /**
   * {@inheritDoc}
   */
  public void select(Object object, VirtualFile virtualFile, boolean b) {
    //do nothing
  }//select

  /**
   * {@inheritDoc}
   */
  public int getWeight() {
    return 5;
  }//getWeight

  /**
   * {@inheritDoc}
   */
  public SelectInTarget createSelectInTarget() {
    return new TapestryProjectSelectInTarget(myProject);
  }//createSelectInTarget

  /**
   * {@inheritDoc}
   */
  public void fileCreated(String path) {
    updateFromRoot(true);
  }//fieCreated

  /**
   * {@inheritDoc}
   */
  public void fileDeleted(String path) {
    updateFromRoot(true);
  }//fileDeleted

  /**
   * {@inheritDoc}
   */
  public void classCreated(String classFqn) {
    updateFromRoot(true);
  }//classCreated

  /**
   * {@inheritDoc}
   */
  public void classDeleted(String classFqn) {
    updateFromRoot(true);
  }//classDeleted

  /**
   * {@inheritDoc}
   */
  public void fileContentsChanged(IResource changedFile) {
    //do nothing
  }//fileContentsChanged

  /**
   * {@inheritDoc}
   */
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
   * @return <code>true</code> if the file can be selected, <code>false</code> otherwise.
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
  public Object getData(String dataId) {
    if (DataKeys.PROJECT.is(dataId)) {
      return myProject;
    }

    if (DataKeys.IDE_VIEW.is(dataId)) {
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

    if (DataKeys.MODULE.is(dataId)) {
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

    if (DataKeys.NAVIGATABLE.is(dataId)) {
      if (getSelectedDescriptor() == null) {
        return null;
      }

      if (getSelectedDescriptor().getElement() instanceof PresentationLibraryElement) {
        return ((IntellijResource)((PresentationLibraryElement)getSelectedDescriptor().getElement()).getElementClass().getFile())
            .getPsiFile();
      }
    }

    if (DataKeys.DELETE_ELEMENT_PROVIDER.is(dataId)) {
      return new SafeDeleteProvider();
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

      public DefaultMutableTreeNode getSelectedNode() {
        return TapestryProjectViewPane.this.getSelectedNode();
      }
    };

    setTreeBuilder(new TapestryViewTreeBuilder(myTree, myProject));
    ((SimpleTreeBuilder)getTreeBuilder()).initRoot();

    myTree.setRootVisible(false);
    myTree.setShowsRootHandles(true);
    UIUtil.setLineStyleAngled(myTree);
    myTree.expandPath(new TreePath(myTree.getModel().getRoot()));
    TreeUtil.expandRootChildIfOnlyOne(myTree);

    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    EditSourceOnDoubleClickHandler.install(myTree);
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
    installTreePopupHandler(ActionPlaces.PROJECT_VIEW_POPUP, IdeActions.GROUP_PROJECT_VIEW_POPUP);
  }

  protected void installTreePopupHandler(final String place, final String groupName) {
    if (ApplicationManager.getApplication() == null) return;
    PopupHandler popupHandler = new PopupHandler() {
      public void invokePopup(java.awt.Component comp, int x, int y) {
        ActionGroup group = (ActionGroup)CustomActionsSchema.getInstance().getCorrectedAction(groupName);
        final ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(place, group);
        popupMenu.getComponent().show(comp, x, y);
      }
    };
    myTree.addMouseListener(popupHandler);
  }


  private void addTreeListeners() {

    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent event) {
        fireTreeChangeListener();

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
              toolWindow.update((Module)getData(DataKeys.MODULE.getName()), selectedNode.getElement(),
                                Arrays.asList(((PresentationLibraryElement)selectedNode.getElement()).getElementClass()));
            }

            if (selectedNode instanceof ClassNode || selectedNode instanceof FileNode) {
              TapestryNode parentSelectedNode =
                  ((TapestryNode)((DefaultMutableTreeNode)((DefaultMutableTreeNode)event.getNewLeadSelectionPath().getLastPathComponent())
                      .getParent()).getUserObject());

              if (parentSelectedNode.getElement() instanceof PresentationLibraryElement) {
                toolWindow.update((Module)getData(DataKeys.MODULE.getName()), parentSelectedNode.getElement(),
                                  Arrays.asList(((PresentationLibraryElement)parentSelectedNode.getElement()).getElementClass()));
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
                  toolWindow.update((Module)getData(DataKeys.MODULE.getName()), component, Arrays.asList(component.getElementClass()));
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
    myTree.getModel().addTreeModelListener(new TreeModelListener() {
      public void treeNodesChanged(TreeModelEvent e) {
        fireTreeChangeListener();
      }

      public void treeNodesInserted(TreeModelEvent e) {
        fireTreeChangeListener();
      }

      public void treeNodesRemoved(TreeModelEvent e) {
        fireTreeChangeListener();
      }

      public void treeStructureChanged(TreeModelEvent e) {
        fireTreeChangeListener();
      }
    });
    myTree.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (KeyEvent.VK_ENTER == e.getKeyCode()) {
          DataContext dataContext = DataManager.getInstance().getDataContext(myTree);
          OpenSourceUtil.openSourcesFrom(dataContext, false);
        }
        else if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
          if (e.isConsumed()) {
            return;
          }
          PsiCopyPasteManager copyPasteManager = PsiCopyPasteManager.getInstance();
          boolean[] isCopied = new boolean[1];
          if (copyPasteManager.getElements(isCopied) != null && !isCopied[0]) {
            copyPasteManager.clear();
            e.consume();
          }
        }
      }
    });
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
