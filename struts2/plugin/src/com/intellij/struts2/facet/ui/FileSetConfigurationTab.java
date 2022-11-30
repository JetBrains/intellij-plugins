/*
 * Copyright 2019 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.facet.ui;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ide.TreeExpander;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.facet.StrutsFacetConfiguration;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Struts2 facet tab "File Sets".
 *
 * @author Yann C&eacute;bron
 */
public class FileSetConfigurationTab extends FacetEditorTab implements Disposable {

  private final StructureTreeModel<SimpleTreeStructure> myModel;
  // GUI components -----------------------
  private JPanel myPanel;

  private final SimpleTree myTree;
  private final AnActionButton myRemoveButton;
  private final AnActionButton myEditButton;
  private JPanel myTreePanel;

  // GUI helpers
  private final SimpleNode myRootNode = new SimpleNode() {
    @Override
    public SimpleNode @NotNull [] getChildren() {
      final List<SimpleNode> nodes = new ArrayList<>(myBuffer.size());
      for (final StrutsFileSet entry : myBuffer) {
        if (!entry.isRemoved()) {
          final FileSetNode setNode = new FileSetNode(entry);
          nodes.add(setNode);
        }
      }
      return nodes.toArray(new SimpleNode[0]);
    }

    @Override
    public boolean isAutoExpandNode() {
      return true;
    }
  };

  private final TreeExpander myTreeExpander;

  private final StrutsConfigsSearcher myConfigsSearcher;

  // original config
  private final StrutsFacetConfiguration originalConfiguration;
  private final Module module;

  // local config
  private final Set<StrutsFileSet> myBuffer = new LinkedHashSet<>();
  private boolean myModified;

  public FileSetConfigurationTab(@NotNull final StrutsFacetConfiguration strutsFacetConfiguration,
                                 @NotNull final FacetEditorContext facetEditorContext) {
    originalConfiguration = strutsFacetConfiguration;
    module = facetEditorContext.getModule();
    myConfigsSearcher = new StrutsConfigsSearcher(module);

    // init tree
    final SimpleTreeStructure structure = new SimpleTreeStructure() {
      @NotNull
      @Override
      public Object getRootElement() {
        return myRootNode;
      }
    };

    myTree = new SimpleTree();
    myTree.setRootVisible(false);
    myTree.setShowsRootHandles(true); // show expand/collapse handles
    myTree.getEmptyText().setText(StrutsBundle.message("facet.fileset.no.filesets.defined"), SimpleTextAttributes.ERROR_ATTRIBUTES);
    myTreeExpander = new DefaultTreeExpander(myTree);

    myModel = new StructureTreeModel<>(structure, this);
    myTree.setModel(new AsyncTreeModel(myModel, this));

    final DumbService dumbService = DumbService.getInstance(facetEditorContext.getProject());
    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(final TreeSelectionEvent e) {
        final StrutsFileSet fileSet = getCurrentFileSet();
        myEditButton.setEnabled(fileSet != null && !dumbService.isDumb());
        myRemoveButton.setEnabled(fileSet != null);
      }
    });

    final CommonActionsManager actionManager = CommonActionsManager.getInstance();
    myTreePanel.add(
      ToolbarDecorator.createDecorator(myTree)
        .setAddAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            final StrutsFileSet fileSet =
              new StrutsFileSet(StrutsFileSet.getUniqueId(myBuffer),
                                StrutsFileSet.getUniqueName(StrutsBundle.message("facet.fileset.my.fileset"), myBuffer),
                                originalConfiguration) {
                @Override
                public boolean isNew() {
                  return true;
                }
              };

            final FileSetEditor editor = new FileSetEditor(myPanel,
                                                           fileSet,
                                                           facetEditorContext,
                                                           myConfigsSearcher);
            editor.show();
            if (editor.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
              final StrutsFileSet editedFileSet = editor.getEditedFileSet();
              Disposer.register(strutsFacetConfiguration, editedFileSet);
              myBuffer.add(editedFileSet);
              myModified = true;
              myModel.invalidateAsync().thenRun(() -> selectFileSet(editedFileSet));
            }
            IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(myTree, true));
          }
        })
        .setRemoveAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            remove();
            myModified = true;
            myModel.invalidateAsync();
            IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(myTree, true));
          }
        })
        .setEditAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton button) {
            final StrutsFileSet fileSet = getCurrentFileSet();
            if (fileSet != null) {
              final FileSetEditor editor = new FileSetEditor(myPanel,
                                                             fileSet,
                                                             facetEditorContext,
                                                             myConfigsSearcher);
              editor.show();
              if (editor.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                myModified = true;
                myBuffer.remove(fileSet);
                final StrutsFileSet edited = editor.getEditedFileSet();
                Disposer.register(strutsFacetConfiguration, edited);
                myBuffer.add(edited);
                edited.setAutodetected(false);
                myModel.invalidateAsync();
                selectFileSet(edited);
              }
              IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(() -> IdeFocusManager.getGlobalInstance().requestFocus(myTree, true));
            }
          }
        })
        .addExtraAction(AnActionButton.fromAction(actionManager.createExpandAllAction(myTreeExpander, myTree)))
        .addExtraAction(AnActionButton.fromAction(actionManager.createCollapseAllAction(myTreeExpander, myTree)))
        .addExtraAction(new AnActionButton(StrutsBundle.messagePointer("action.AnActionButton.text.open.struts.2.plugin.documentation"),
                                           AllIcons.Actions.Help) {
          @Override
          public void actionPerformed(@NotNull AnActionEvent e) {
            BrowserUtil.browse("https://confluence.jetbrains.com/pages/viewpage.action?pageId=35367");
          }

          @Override
          public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
          }
        })

        .disableUpDownActions()
        .createPanel());

    myEditButton = ToolbarDecorator.findEditButton(myTreePanel);
    myRemoveButton = ToolbarDecorator.findRemoveButton(myTreePanel);

    AnActionButton addButton = ToolbarDecorator.findAddButton(myTreePanel);
    assert addButton != null;
    dumbService.makeDumbAware(addButton.getContextComponent(), this);
    dumbService.makeDumbAware(myEditButton.getContextComponent(), this);
  }

  @Nullable
  private StrutsFileSet getCurrentFileSet() {
    final FileSetNode currentFileSetNode = getCurrentFileSetNode();
    return currentFileSetNode == null ? null : currentFileSetNode.mySet;
  }

  @Nullable
  private FileSetNode getCurrentFileSetNode() {
    final SimpleNode selectedNode = myTree.getSelectedNode();
    if (selectedNode == null) {
      return null;
    }
    if (selectedNode instanceof FileSetNode) {
      return (FileSetNode)selectedNode;
    }
    else if (selectedNode.getParent() instanceof FileSetNode) {
      return (FileSetNode)selectedNode.getParent();
    }
    else {
      final SimpleNode parent = selectedNode.getParent();
      if (parent != null && parent.getParent() instanceof FileSetNode) {
        return (FileSetNode)selectedNode.getParent().getParent();
      }
    }
    return null;
  }

  private void selectFileSet(final StrutsFileSet fileSet) {
    SimpleNode simpleNode = ContainerUtil.find(myRootNode.getChildren(), node -> ((FileSetNode)node).mySet == fileSet);
    assert simpleNode != null;
    myModel.select(simpleNode, myTree, path -> {});
  }

  private void remove() {
    final SimpleNode[] nodes = myTree.getSelectedNodesIfUniform();
    for (final SimpleNode node : nodes) {

      if (node instanceof FileSetNode) {
        final StrutsFileSet fileSet = ((FileSetNode)node).mySet;
        if (fileSet.getFiles().isEmpty()) {
          myBuffer.remove(fileSet);
          return;
        }

        final int result = Messages.showYesNoDialog(myPanel,
                                                    StrutsBundle.message("facet.fileset.remove.fileset.question",
                                                                         fileSet.getName()),
                                                    StrutsBundle.message("facet.fileset.remove.fileset.title"),
                                                    Messages.getQuestionIcon());
        if (result == Messages.YES) {
          if (fileSet.isAutodetected()) {
            fileSet.setRemoved(true);
            myBuffer.add(fileSet);
          }
          else {
            myBuffer.remove(fileSet);
          }
        }
      }
      else if (node instanceof ConfigFileNode) {
        final VirtualFilePointer filePointer = ((ConfigFileNode)node).myFilePointer;
        final StrutsFileSet fileSet = ((FileSetNode)node.getParent()).mySet;
        fileSet.removeFile(filePointer);
      }
    }
  }

  @Override
  @Nls
  public String getDisplayName() {
    return StrutsBundle.message("facet.fileset.title");
  }

  @Override
  @NotNull
  public JComponent createComponent() {
    return myPanel;
  }

  @Override
  public boolean isModified() {
    return myModified;
  }

  @Override
  public void apply() {
    final Set<StrutsFileSet> fileSets = originalConfiguration.getFileSets();
    fileSets.clear();
    for (final StrutsFileSet fileSet : myBuffer) {
      if (!fileSet.isAutodetected() || fileSet.isRemoved()) {
        fileSets.add(fileSet);
      }
    }
    originalConfiguration.setModified();
    myModified = false;
  }

  @Override
  public void reset() {
    myBuffer.clear();
    final Set<StrutsFileSet> sets = StrutsManager.getInstance(module.getProject()).getAllConfigFileSets(module);
    /*new StrutsFileSet(fileSet)*/
    myBuffer.addAll(sets);

    myModel.invalidateAsync();
    myTree.setSelectionRow(0);
  }

  @Override
  public void disposeUIResources() {
    Disposer.dispose(this);
  }

  @Override
  public void dispose() {
  }

  private class FileSetNode extends SimpleNode {

    protected final StrutsFileSet mySet;

    FileSetNode(final StrutsFileSet fileSet) {
      super(myRootNode);
      mySet = fileSet;

      final PresentationData presentationData = getPresentation();
      final String name = mySet.getName(); //NON-NLS

      if (fileSet.getFiles().isEmpty()) {
        presentationData.addText(name, getErrorAttributes());
        presentationData.setTooltip(StrutsBundle.message("facet.fileset.no.files.attached"));
      }
      else {
        presentationData.addText(name, getPlainAttributes());
        presentationData.setLocationString(Integer.toString(fileSet.getFiles().size()));
      }
    }

    @Override
    public SimpleNode @NotNull [] getChildren() {
      final List<SimpleNode> nodes = new ArrayList<>();

      for (final VirtualFilePointer file : mySet.getFiles()) {
        nodes.add(new ConfigFileNode(file, this));
      }
      return nodes.toArray(new SimpleNode[0]);
    }

    @Override
    public boolean isAutoExpandNode() {
      return true;
    }

    @Override
    public Object @NotNull [] getEqualityObjects() {
      return new Object[]{mySet, mySet.getName(), mySet.getFiles()};
    }
  }


  private static final class ConfigFileNode extends SimpleNode {

    private final VirtualFilePointer myFilePointer;

    ConfigFileNode(final VirtualFilePointer name, final SimpleNode parent) {
      super(parent);
      myFilePointer = name;
      getTemplatePresentation().setIcon(StrutsIcons.STRUTS_CONFIG_FILE);
    }

    @Override
    public boolean isAlwaysLeaf() {
      return true;
    }

    @Override
    protected void doUpdate(@NotNull PresentationData presentation) {
      final VirtualFile file = myFilePointer.getFile();
      if (file != null) {
        renderFile(presentation, file, getPlainAttributes(), null);
      }
      else {
        renderFile(presentation, null, getErrorAttributes(), StrutsBundle.message("facet.fileset.file.not.found"));
      }
    }

    private void renderFile(@NotNull PresentationData presentation,
                            final VirtualFile file,
                            final SimpleTextAttributes textAttributes,
                            @NlsContexts.Tooltip @Nullable final String toolTip) {
      presentation.setTooltip(toolTip);
      presentation.clearText();
      presentation.addText(myFilePointer.getFileName(), textAttributes); //NON-NLS

      if (file != null) {
        presentation.setLocationString(file.getPath());
      }
    }

    @Override
    public SimpleNode @NotNull [] getChildren() {
      return NO_CHILDREN;
    }
  }

  @Override
  public String getHelpTopic() {
    return "reference.settings.project.structure.facets.struts2.facet";
  }
}
