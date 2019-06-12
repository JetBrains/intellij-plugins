/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package jetbrains.communicator.idea.viewFiles;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.idea.IDEAFacade;
import jetbrains.communicator.util.KirTree;
import jetbrains.communicator.util.KirTreeNode;
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.TreeUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Kir
 */
class ViewFilesPanel extends JPanel implements DataProvider {
  static final String NON_PROJECT_NODE = CommunicatorStrings.getMsg("non.project.files");

  private final FileTypeManager myFileTypeManager;
  private final IDEFacade myIdeFacade;
  private final Tree myTree;

  private ProjectsData myProjectsData;
  private User myUser;

  private boolean myShowReadOnly;
  @SuppressWarnings({"HardCodedStringLiteral"})
  public static final String SHOW_READ_ONLY_KEY = "SHOW_READ_ONLY_KEY";
  private OpenFileAction myOpenFileAction;

  ViewFilesPanel(FileTypeManager fileTypeManager, ActionManager actionManager, IDEFacade facade) {
    super(new BorderLayout(2, 2));
    setOpaque(false);

    myFileTypeManager = fileTypeManager;
    myIdeFacade = facade;
    myShowReadOnly = Pico.getOptions().isSet(SHOW_READ_ONLY_KEY, true);

    myTree = new KirTree(){
      @Override
      protected void onEnter() {
        super.onEnter();
        if (myOpenFileAction.isEnabled()) {
          myOpenFileAction.perform();
        }
      }
    };
    IDEAFacade.installIdeaTreeActions(myTree);
    add(ScrollPaneFactory.createScrollPane(myTree));
    add(createActionsToolbar(actionManager), BorderLayout.NORTH);

    IDEAFacade.installPopupMenu(createActionGroup(), myTree, actionManager);

    myTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
    setMinimumSize(new Dimension(200, 0));
  }

  @Override
  public Object getData(@NotNull String dataId) {
    if (DiffAction.USER.equals(dataId)) {
      return myUser;
    }
    return null;
  }

  private Component createActionsToolbar(ActionManager actionManager) {
    DefaultActionGroup actionGroup = createActionGroup();

    if (actionManager != null)
      return actionManager.createActionToolbar("TOOLBAR", actionGroup, true).getComponent();

    return new JLabel();
  }

  private DefaultActionGroup createActionGroup() {
    DefaultActionGroup actionGroup = new DefaultActionGroup();
    if (ApplicationManager.getApplication() == null || Pico.isUnitTest()) return actionGroup;

    addRefreshAction(actionGroup);
    myOpenFileAction = new OpenFileAction(myTree, myIdeFacade);
    myOpenFileAction.registerCustomShortcutSet(
        new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0)),
        myTree);

    AnAction diffAction = new DiffAction(myTree) {
      @Override
      protected User getUser() {
        return myUser;
      }
    };

    diffAction.registerCustomShortcutSet(CommonShortcuts.getDiff(), myTree);

    actionGroup.add(myOpenFileAction);
    actionGroup.add(diffAction);

    addToggleReadOnlyAction(actionGroup);
    return actionGroup;
  }

  private void addRefreshAction(DefaultActionGroup actionGroup) {
    actionGroup.add(new AnAction(CommunicatorStrings.getMsg("refresh.file.list"), "", AllIcons.Actions.Refresh) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        refreshData(myUser, myUser.getProjectsData(myIdeFacade));
      }
    });
  }

  private void addToggleReadOnlyAction(DefaultActionGroup actionGroup) {
    actionGroup.add(new ToggleAction(
      CommunicatorStrings.getMsg("idea.show_read_only.text"),
      CommunicatorStrings.getMsg("idea.show_read_only.description"),
      AllIcons.Ide.Readonly
        ) {
      @Override
      public boolean isSelected(@NotNull AnActionEvent e) {
        return myShowReadOnly;
      }

      @Override
      public void setSelected(@NotNull AnActionEvent e, boolean state) {
        showReadOnly(state);
      }
    });
  }

  public void refreshData(User user, ProjectsData data) {
    assert data != null;
    assert user != null;

    myUser = user;
    myProjectsData = data;

    ((DefaultTreeModel) myTree.getModel()).setRoot(new MyRootNode());
    TreeUtils.expandAll(myTree);
  }

  public void showReadOnly(boolean showReadOnlyFiles) {
    if (myShowReadOnly != showReadOnlyFiles) {
      myShowReadOnly = showReadOnlyFiles;
      Pico.getOptions().setOption(SHOW_READ_ONLY_KEY, showReadOnlyFiles);
      if (myProjectsData != null) {
        refreshData(myUser, myProjectsData);
      }
    }
  }

  JTree getTree() {
    return myTree;
  }

  public boolean isReadOnlyShown() {
    return myShowReadOnly;
  }

  private class MyRootNode extends KirTreeNode {
    private List<KirTreeNode> myChildren;

    MyRootNode() {
      super(null);
    }

    @Override
    protected Component renderIn(JLabel label, boolean selected, boolean hasFocus) {
      return label;
    }

    @Override
    protected List getChildNodes() {
      if (myChildren == null) {
        myChildren = new ArrayList<>();
        String[] projects = myProjectsData.getProjects();
        Arrays.sort(projects);
        for (String project : projects) {
          myChildren.add(new ProjectNode(this, project));
        }

        if (myProjectsData.getNonProjectFiles().length > 0) {
          myChildren.add(new ProjectNode(this, NON_PROJECT_NODE, myProjectsData.getNonProjectFiles()));
        }
      }
      return myChildren;
    }
  }

  private class ProjectNode extends KirTreeNode {
    private final String myName;
    private List<FileNode> myChildren;
    private final VFile[] myProjectFiles;

    ProjectNode(TreeNode parent, String name, VFile[] projectFiles) {
      super(parent);
      myName = name;
      myProjectFiles = projectFiles;
    }

    ProjectNode(TreeNode parent, String name) {
      super(parent);
      myName = name;
      myProjectFiles = myProjectsData.getProjectFiles(myName);
    }

    @Override
    protected List getChildNodes() {
      if (myChildren == null) {
        myChildren = new ArrayList<>();
        VFile[] projectFiles = myProjectFiles;
        Arrays.sort(projectFiles);
        for (VFile projectFile : projectFiles) {
          if (myShowReadOnly || projectFile.isWritable()) {
            myChildren.add(new FileNode(this, projectFile));
          }
        }
      }
      return myChildren;
    }

    @Override
    protected Component renderIn(JLabel label, boolean selected, boolean hasFocus) {
      if (NON_PROJECT_NODE.equals(myName)) {
        label.setIcon(AllIcons.Nodes.Unknown);
      }
      else {
        label.setIcon(AllIcons.Nodes.IdeaProject);
      }
      return label;
    }

    public String toString() {
      return myName;
    }
  }

  public class FileNode extends KirTreeNode {
    private final VFile myVFile;

    public FileNode(ProjectNode projectNode, VFile projectFile) {
      super(projectNode);
      myVFile = projectFile;
    }

    @Override
    protected List getChildNodes() {
      return Collections.emptyList();
    }

    @Override
    protected Component renderIn(JLabel label, boolean selected, boolean hasFocus) {
      if (myVFile.isWritable()) {
        label.setIcon(getIconByExtension());
      }
      else {
        LayeredIcon layeredIcon = new LayeredIcon(2);
        layeredIcon.setIcon(getIconByExtension(), 0);
        layeredIcon.setIcon(AllIcons.Nodes.Locked, 1);
        label.setIcon(layeredIcon);
      }
      return label;
    }

    private Icon getIconByExtension() {
      if (myFileTypeManager != null) {
        FileType fileType = myFileTypeManager.getFileTypeByExtension(getExtension());
        return fileType.getIcon();
      }
      return AllIcons.Nodes.Unknown;
    }

    private String getExtension() {
      final String contentPath = myVFile.getFullPath();
      if (contentPath != null) {
        int i = contentPath.lastIndexOf('.');
        if (i > 0) {
          return contentPath.substring(i + 1);
        }
      }
      return "none";
    }

    public String toString() {
      String path = myVFile.getContentPath();
      return path == null ? myVFile.getFullPath() : path;
    }

    public VFile getVFile() {
      return myVFile;
    }
  }
}
