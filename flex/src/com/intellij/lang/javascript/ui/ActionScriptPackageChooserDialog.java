// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.ui;

import com.intellij.CommonBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.IdeCoreBundle;
import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.lang.javascript.refactoring.ui.JSReferenceEditor;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Objects;

public class ActionScriptPackageChooserDialog extends DialogWrapper {
  private static final Logger LOG = Logger.getInstance(ActionScriptPackageChooserDialog.class.getName());

  private Tree myTree;
  private DefaultTreeModel myModel;
  private final Project myProject;
  private final @NlsContexts.DialogTitle String myTitle;
  private final GlobalSearchScope mySearchScope;

  public ActionScriptPackageChooserDialog(@NlsContexts.DialogTitle String title, Project project, GlobalSearchScope searchScope) {
    super(project, true);
    mySearchScope = searchScope;
    setTitle(title);
    myTitle = title;
    myProject = project;
    init();
  }

  @Override
  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());


    myModel = new DefaultTreeModel(new DefaultMutableTreeNode());
    createTreeModel();
    myTree = new Tree(myModel);
    myTree.setCellRenderer(
      new DefaultTreeCellRenderer() {
        @Override
        public Component getTreeCellRendererComponent(
          JTree tree, Object value,
          boolean sel,
          boolean expanded,
          boolean leaf, int row,
          boolean hasFocus
        ) {
          super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
          setIcon(AllIcons.Nodes.Package);

          if (value instanceof DefaultMutableTreeNode node) {
            Object object = node.getUserObject();
            if (object instanceof VirtualFile) {
              String name = PlatformPackageUtil.getPackageName((VirtualFile)object, myProject);
              setText(name.length() > 0 ? StringUtil.getShortName(name) : IdeCoreBundle.message("node.default"));
            }
          }
          return this;
        }
      }
    );

    myTree.setBorder(BorderFactory.createEtchedBorder());
    JScrollPane scrollPane = new JScrollPane(myTree);
    scrollPane.setPreferredSize(JBUI.size(500, 300));

    TreeSpeedSearch.installOn(myTree, false, path -> {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
      Object object = node.getUserObject();
      if (object instanceof VirtualFile) {
        return ((VirtualFile)object).getName();
      }
      else {
        return "";
      }
    });

    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        VirtualFile selection = getTreeSelection();
        if (selection != null) {
          @NlsSafe String name = PlatformPackageUtil.getPackageName(selection, myProject);
          setTitle(myTitle + " - " + ("".equals(name) ? IdeBundle.message("node.default.package") : name));
        }
        else {
          setTitle(myTitle);
        }
      }
    });

    panel.add(scrollPane, BorderLayout.CENTER);
    DefaultActionGroup group = createActionGroup(myTree);

    ActionToolbar toolBar = ActionManager.getInstance().createActionToolbar("ASPackageChooser", group, true);
    panel.add(toolBar.getComponent(), BorderLayout.NORTH);
    toolBar.getComponent().setAlignmentX(JComponent.LEFT_ALIGNMENT);

    return panel;
  }

  private DefaultActionGroup createActionGroup(JComponent component) {
    final DefaultActionGroup group = new DefaultActionGroup();
    final DefaultActionGroup temp = new DefaultActionGroup();
    NewPackageAction newPackageAction = new NewPackageAction();
    newPackageAction.enableInModalConext();
    newPackageAction
      .registerCustomShortcutSet(ActionManager.getInstance().getAction(IdeActions.ACTION_NEW_ELEMENT).getShortcutSet(), component);
    temp.add(newPackageAction);
    group.add(temp);
    return group;
  }

  @Override
  public String getDimensionServiceKey() {
    return "#com.intellij.ide.util.PackageChooserDialog";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTree;
  }

  @Nullable
  public String getSelectedPackage() {
    VirtualFile file = getTreeSelection();
    return file != null ? PlatformPackageUtil.getPackageName(file, myProject) : null;
  }

  //public List<VirtualFile> getSelectedPackages() {
  //  return TreeUtil.collectSelectedObjectsOfType(myTree, VirtualFile.class);
  //}

  public void selectPackage(final String qualifiedName) {
    /*ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {*/
    DefaultMutableTreeNode node = findNodeForPackage(qualifiedName);
    if (node != null) {
      TreePath path = new TreePath(node.getPath());
      TreeUtil.selectPath(myTree, path);
    }
    /* }
   }, ModalityState.stateForComponent(getRootPane()));*/
  }

  @Nullable
  private VirtualFile getTreeSelection() {
    if (myTree == null) return null;
    TreePath path = myTree.getSelectionPath();
    if (path == null) return null;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
    return (VirtualFile)node.getUserObject();
  }

  private void createTreeModel() {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
    fileIndex.iterateContent(
      fileOrDir -> {
        if (fileOrDir.isDirectory() && mySearchScope.contains(fileOrDir) && fileIndex.isInSourceContent(fileOrDir)) {
          addPackage(fileOrDir);
        }
        return true;
      }
    );

    TreeUtil.sort(myModel, (o1, o2) -> {
      DefaultMutableTreeNode n1 = (DefaultMutableTreeNode)o1;
      DefaultMutableTreeNode n2 = (DefaultMutableTreeNode)o2;
      VirtualFile element1 = (VirtualFile)n1.getUserObject();
      VirtualFile element2 = (VirtualFile)n2.getUserObject();
      return element1.getName().compareToIgnoreCase(element2.getName());
    });
  }

  @NotNull
  private DefaultMutableTreeNode addPackage(VirtualFile aPackage) {
    final String qualifiedPackageName = PlatformPackageUtil.getPackageName(aPackage, myProject);
    final VirtualFile parentPackage = aPackage.getParent();
    if (parentPackage == null || PlatformPackageUtil.getPackageName(parentPackage, myProject) == null) {
      final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)myModel.getRoot();
      if (qualifiedPackageName.length() == 0) {
        rootNode.setUserObject(aPackage);
        return rootNode;
      }
      else {
        DefaultMutableTreeNode packageNode = findPackageNode(rootNode, qualifiedPackageName);
        if (packageNode != null) return packageNode;
        packageNode = new DefaultMutableTreeNode(aPackage);
        rootNode.add(packageNode);
        return packageNode;
      }
    }
    else {
      final DefaultMutableTreeNode parentNode = addPackage(parentPackage);
      DefaultMutableTreeNode packageNode = findPackageNode(parentNode, qualifiedPackageName);
      if (packageNode != null) {
        return packageNode;
      }
      packageNode = new DefaultMutableTreeNode(aPackage);
      parentNode.add(packageNode);
      return packageNode;
    }
  }

  @Nullable
  private DefaultMutableTreeNode findPackageNode(DefaultMutableTreeNode rootNode, String qualifiedName) {
    for (int i = 0; i < rootNode.getChildCount(); i++) {
      final DefaultMutableTreeNode child = (DefaultMutableTreeNode)rootNode.getChildAt(i);
      final VirtualFile nodePackage = (VirtualFile)child.getUserObject();
      if (nodePackage != null) {
        if (Objects.equals(PlatformPackageUtil.getPackageName(nodePackage, myProject), qualifiedName)) return child;
      }
    }
    return null;
  }

  @Nullable
  private DefaultMutableTreeNode findNodeForPackage(String qualifiedPackageName) {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)myModel.getRoot();
    Enumeration<TreeNode> enumeration = root.depthFirstEnumeration();
    while (enumeration.hasMoreElements()) {
      TreeNode o = enumeration.nextElement();
      if (o instanceof DefaultMutableTreeNode node) {
        VirtualFile nodePackage = (VirtualFile)node.getUserObject();
        if (nodePackage != null) {
          if (Objects.equals(PlatformPackageUtil.getPackageName(nodePackage, myProject), qualifiedPackageName)) return node;
        }
      }
    }
    return null;
  }

  private void createNewPackage() {
    final VirtualFile selectedPackage = getTreeSelection();
    if (selectedPackage == null) return;

    final String newPackageName = Messages
      .showInputDialog(myProject, IdeBundle.message("prompt.enter.a.new.package.name"), IdeBundle.message("title.new.package"),
                       Messages.getQuestionIcon(), "",
                       new InputValidator() {
                         @Override
                         public boolean checkInput(final String inputString) {
                           return inputString != null && inputString.length() > 0;
                         }

                         @Override
                         public boolean canClose(final String inputString) {
                           return checkInput(inputString);
                         }
                       });
    if (newPackageName == null) return;

    WriteCommandAction.runWriteCommandAction(myProject, IdeBundle.message("command.create.new.package"), null, new Runnable() {
      @Override
      public void run() {
        try {
          String newQualifiedName = PlatformPackageUtil.getPackageName(selectedPackage, myProject);
          if (!Comparing.strEqual(newQualifiedName, "")) newQualifiedName += ".";
          newQualifiedName += newPackageName;
          final VirtualFile dir = selectedPackage.createChildDirectory(this, newPackageName);

          DefaultMutableTreeNode node = (DefaultMutableTreeNode)myTree.getSelectionPath().getLastPathComponent();
          final DefaultMutableTreeNode newChild = new DefaultMutableTreeNode();
          newChild.setUserObject(dir);
          node.add(newChild);

          final DefaultTreeModel model = (DefaultTreeModel)myTree.getModel();
          model.nodeStructureChanged(node);

          final TreePath selectionPath = myTree.getSelectionPath();
          TreePath path;
          if (selectionPath == null) {
            path = new TreePath(newChild.getPath());
          }
          else {
            path = selectionPath.pathByAddingChild(newChild);
          }
          myTree.setSelectionPath(path);
          myTree.scrollPathToVisible(path);
          myTree.expandPath(path);
        }
        catch (IncorrectOperationException | IOException e) {
          Messages.showMessageDialog(
            getContentPane(),
            StringUtil.getMessage(e),
            CommonBundle.getErrorTitle(),
            Messages.getErrorIcon()
          );
          LOG.debug(e);
        }
      }
    });
  }

  public static @NotNull JSReferenceEditor createPackageReferenceEditor(final String text,
                                                                        final @NotNull Project project,
                                                                        @Nullable final String recentsKey,
                                                                        GlobalSearchScope scope,
                                                                        @NlsContexts.DialogTitle @NotNull String chooserTitle) {
    return new JSReferenceEditor(text, project, recentsKey, scope, null, null, chooserTitle, true, null) {
      @Override
      protected ActionListener createActionListener() {
        return new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            ActionScriptPackageChooserDialog chooser = new ActionScriptPackageChooserDialog(chooserTitle, project, getScope());
            chooser.selectPackage(getText());
            chooser.show();
            String aPackage = chooser.getSelectedPackage();
            if (aPackage != null) {
              setText(aPackage);
            }
          }
        };
      }
    };
  }

  private class NewPackageAction extends AnAction {
    NewPackageAction() {
      super(IdeBundle.messagePointer("action.new.package"), IdeBundle.messagePointer("action.description.create.new.package"),
            AllIcons.Actions.NewFolder);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.EDT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      createNewPackage();
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
      Presentation presentation = event.getPresentation();
      presentation.setEnabled(getTreeSelection() != null);
    }

    public void enableInModalConext() {
      setEnabledInModalContext(true);
    }
  }

}

